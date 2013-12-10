/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.droidium.container.execution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executor service which is able to execute external process as well as callables
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProcessExecutor {

    private final Map<String, String> environment;
    private final ShutDownThreadHolder shutdownThreads;
    private final ExecutorService service;
    private final ScheduledExecutorService scheduledService;

    // create a singleton instance
    private static final ProcessExecutor INSTANCE;
    static {
        INSTANCE = new ProcessExecutor(System.getenv());
    }

    public static ProcessExecutor getInstance() {
        return INSTANCE;
    }

    public ProcessExecutor(Map<String, String> environmentProperies) {
        if (environmentProperies == null || environmentProperies.containsValue("")) {
            throw new IllegalStateException(
                    "All entries in environment properies map have to have values which are not null objects nor empty strings!");
        }

        this.shutdownThreads = new ShutDownThreadHolder();
        this.service = Executors.newCachedThreadPool();
        this.scheduledService = Executors.newScheduledThreadPool(1);
        this.environment = environmentProperies;
    }

    public ProcessExecutor() {
        this(new HashMap<String, String>());
    }

    /**
     * Submit callable to be executed
     *
     * @param callable to be executed
     * @return future
     */
    public <T> Future<T> submit(Callable<T> callable) {
        return service.submit(callable);
    }

    /**
     * Schedules a callable to be executed in regular intervals
     *
     * @param callable Callable
     * @param timeout Total timeout
     * @param step delay before next execution
     * @param unit time unit
     * @return {@code true} if executed successfully, false otherwise
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Boolean scheduleUntilTrue(Callable<Boolean> callable, long timeout, long step, TimeUnit unit)
            throws InterruptedException, ExecutionException {

        CountDownWatch countdown = new CountDownWatch(timeout, unit);
        while (countdown.timeLeft() > 0) {
            // delay by step
            ScheduledFuture<Boolean> future = scheduledService.schedule(callable, step, unit);
            Boolean result = false;
            try {
                // wait for true up to timeLeft
                // this means we might get less steps then timeout/step
                result = future.get(countdown.timeLeft(), unit);
                if (result == true) {
                    return true;
                }
            } catch (TimeoutException e) {
                continue;
            }
        }

        return false;
    }

    /**
     * Spawns a process defined by command. Process output is consumed by {@link ProcessInteraction}.
     *
     * @param interaction command interaction
     * @param command command to be execution
     * @return spawned process execution
     */
    public ProcessExecution spawn(ProcessInteraction interaction, String[] command) throws ProcessExecutionException {
        try {
            Future<Process> processFuture = service.submit(new SpawnedProcess(environment, true, command));
            Process process = processFuture.get();
            ProcessExecution execution = new ProcessExecution(process, command[0], System.out, System.err);
            service.submit(new ProcessOutputConsumer(execution, interaction));
            shutdownThreads.addHookFor(process);
            return execution;
        } catch (InterruptedException e) {
            throw new ProcessExecutionException(e, "Unable to spawn {0}, interrupted", new Object[] { command });
        } catch (ExecutionException e) {
            throw new ProcessExecutionException(e, "Unable to spawn {0}, failed", new Object[] { command });
        }
    }

    /**
     * Spawns a process defined by command. Process output is discarded.
     *
     * @param command command to be execution
     * @return spawned process execution
     * @throws ProcessExecutionException if anything goes wrong
     */
    public ProcessExecution spawn(String... command) throws ProcessExecutionException {
        return spawn(ProcessInteractionBuilder.NO_INTERACTION, command);
    }

    /**
     * Executes a process defined by command. Process output is consumed by {@link ProcessInteraction}. Waits for process to
     * finish and checks if process finished with status code 0
     *
     * @param interaction command interaction
     * @param command command to be execution
     * @return spawned process execution
     * @throws ProcessExecutionException if anything goes wrong
     */
    public ProcessExecution execute(ProcessInteraction interaction, String[] command) throws ProcessExecutionException {
        Process process = null;
        try {
            Future<Process> processFuture = service.submit(new SpawnedProcess(environment, true, command));
            process = processFuture.get();
            Future<ProcessExecution> executionFuture = service.submit(new ProcessOutputConsumer(new ProcessExecution(process,
                    command[0], System.out, System.err), interaction));
            // wait for process to finish
            process.waitFor();
            // wait for process to finish IO
            ProcessExecution execution = executionFuture.get();
            if (execution.executionFailed()) {
                throw new ProcessExecutionException("Invocation of {0} failed with {1}", new Object[] { command,
                        execution.getExitCode() });
            }
            return execution;
        } catch (InterruptedException e) {
            throw new ProcessExecutionException(e, "Unable to execute {0}, interrupted", new Object[] { command });
        } catch (ExecutionException e) {
            throw new ProcessExecutionException(e, "Unable to execute {0}, failed", new Object[] { command });
        } finally {
            // cleanup
            if (process != null) {
                InputStream in = process.getInputStream();
                InputStream err = process.getErrorStream();
                OutputStream out = process.getOutputStream();
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignore) {

                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException ignore) {

                    }
                }
                if (err != null) {
                    try {
                        err.close();
                    } catch (IOException ignore) {

                    }
                }
                // just in case, something went wrong
                process.destroy();
            }

        }
    }

    /**
     * Executes a process defined by command. Process output is discarded. Waits for process to finish and checks if process
     * finished with status code 0
     *
     * @param command command to be execution
     * @return spawned process execution
     * @throws ProcessExecutionException if anything goes wrong
     */
    public ProcessExecution execute(String... command) throws ProcessExecutionException {
        return execute(ProcessInteractionBuilder.NO_INTERACTION, command);
    }

    public ProcessExecutor removeShutdownHook(Process p) {
        shutdownThreads.removeHookFor(p);
        return this;
    }

    private static class ShutDownThreadHolder {

        private final Map<Process, Thread> shutdownThreads;

        public ShutDownThreadHolder() {
            this.shutdownThreads = Collections.synchronizedMap(new HashMap<Process, Thread>());
        }

        public void addHookFor(final Process p) {
            Thread shutdownThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (p != null) {
                        p.destroy();
                        try {
                            p.waitFor();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);
            shutdownThreads.put(p, shutdownThread);
        }

        public void removeHookFor(final Process p) {
            shutdownThreads.remove(p);
        }
    }

    private static class SpawnedProcess implements Callable<Process> {

        private final String[] command;
        private boolean redirectErrorStream;
        private Map<String, String> env;

        public SpawnedProcess(Map<String, String> env, boolean redirectErrorStream, String[] command) {
            this.env = env;
            this.redirectErrorStream = redirectErrorStream;
            this.command = command;
        }

        @Override
        public Process call() throws Exception {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.environment().putAll(env);
            builder.redirectErrorStream(redirectErrorStream);
            return builder.start();
        }

    }

    /**
     * Runnable that consumes the output of the process.
     *
     * @author Stuart Douglas
     * @author Karel Piwko
     */
    static class ProcessOutputConsumer implements Callable<ProcessExecution> {

        private static final Logger log = Logger.getLogger(ProcessOutputConsumer.class.getName());

        private final ProcessExecution execution;
        private final ProcessInteraction interaction;

        public ProcessOutputConsumer(ProcessExecution execution, ProcessInteraction interaction) {
            this.execution = execution;
            this.interaction = interaction;
        }

        @Override
        public ProcessExecution call() throws Exception {
            final InputStream stream = execution.getProcess().getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

            // close process input stream if we don't need it
            // closed input stream is a requirement for process not to hang on windows
            if (!interaction.requiresInputInteraction()) {
                try {
                    execution.getProcess().getOutputStream().close();
                } catch (IOException ignore) {
                }
            }

            try {
                // read character by character
                int i;
                boolean reachedEOF = false;
                Sentence sentence = new Sentence();
                // we have an extra check to figure out whether EOF was reached - using last expected response
                while (!reachedEOF && (i = reader.read()) != -1) {
                    // add the character
                    sentence.append((char) i);

                    Answer answer = interaction.repliesTo(sentence);
                    switch (answer.getType()) {
                        case NONE:
                            break;
                        case EOF:
                            // some processes does not correctly close
                            // for instance, android.bat on windows does exactly that
                            reachedEOF = true;
                        case TEXT:
                            log.log(Level.FINEST, "({0}): {1} <= {2}", new Object[] { execution.getProcessId(), sentence,
                                    answer });
                            sentence.append(answer);
                            execution.replyWith(answer);
                            break;
                    }

                    // save and print output
                    if (sentence.isFinished()) {
                        sentence.trim();
                        log.log(Level.FINEST, "({0}): {1}", new Object[] { execution.getProcessId(), sentence });

                        // propagate output/error to user
                        if (interaction.shouldOutput(sentence)) {
                            execution.getStdout().println("(" + execution.getProcessId() + "):" + sentence);
                        }
                        if (interaction.shouldOutputToErr(sentence)) {
                            execution.getStderr().println("ERROR (" + execution.getProcessId() + "):" + sentence);
                        }

                        execution.appendOutput(sentence);
                        sentence.reset();
                    }
                }

                // handle last line
                if (!sentence.isEmpty()) {
                    log.log(Level.FINEST, "{0} outputs: {1}", new Object[] { execution.getProcessId(), sentence });

                    // propagate output/error to user
                    if (interaction.shouldOutput(sentence)) {
                        execution.getStdout().println("(" + execution.getProcessId() + "):" + sentence);
                    }
                    if (interaction.shouldOutputToErr(sentence)) {
                        execution.getStderr().println("ERROR (" + execution.getProcessId() + "):" + sentence);
                    }

                    execution.appendOutput(sentence);
                }
            } catch (IOException ignore) {
            }

            try {
                OutputStream os = execution.getProcess().getOutputStream();
                if (os != null) {
                    os.close();
                }
            } catch (IOException ignore) {
            }

            return execution;
        }
    }

}
