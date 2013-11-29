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
package org.arquillian.droidium.container.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.configuration.Validate;

/**
 * Executor service which is able to execute external process as well as callables
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProcessExecutor {

    public static Map<String, String> ENVIRONMENT_PROPERTIES = null;
    private final ShutDownThreadHolder shutdownThreads;
    private final ExecutorService service;
    private final ScheduledExecutorService scheduledService;

    public ProcessExecutor(Map<String, String> environmentProperies) {
        Validate.notNull(environmentProperies, "Environment properties to set for ProcessExecutor is backed by null object!");
        Validate.notAllNullsOrEmpty(environmentProperies.values().toArray(new String[0]), "All entries in "
            + "environment properies map have to have values which are not null objects nor empty strings!");

        this.shutdownThreads = new ShutDownThreadHolder();
        this.service = Executors.newCachedThreadPool();
        this.scheduledService = Executors.newScheduledThreadPool(1);
        ENVIRONMENT_PROPERTIES = environmentProperies;
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
     * @throws AndroidExecutionException if anything goes wrong
     */
    public ProcessExecution spawn(ProcessInteraction interaction, Command command) throws AndroidExecutionException {
        try {
            Future<Process> processFuture = service.submit(new SpawnedProcess(true, command));
            Process process = processFuture.get();
            ProcessExecution execution = new ProcessExecution(process, command.get(0));
            service.submit(new ProcessOutputConsumer(execution, interaction));
            shutdownThreads.addHookFor(process);
            return execution;
        } catch (InterruptedException e) {
            throw new AndroidExecutionException(e, "Unable to spawn {0}, interrupted", command);
        } catch (ExecutionException e) {
            throw new AndroidExecutionException(e, "Unable to spawn {0}, failed", command);
        }
    }

    /**
     * Spawns a process defined by command. Process output is discarded.
     *
     * @param command command to be execution
     * @return spawned process execution
     * @throws AndroidExecutionException if anything goes wrong
     */
    public ProcessExecution spawn(Command command) throws AndroidExecutionException {
        return spawn(ProcessInteractionBuilder.NO_INTERACTION, command);
    }

    /**
     * Executes a process defined by command. Process output is consumed by {@link ProcessInteraction}. Waits for process to
     * finish and checks if process finished with status code 0
     *
     * @param interaction command interaction
     * @param command command to be execution
     * @return spawned process execution
     * @throws AndroidExecutionException if anything goes wrong
     */
    public ProcessExecution execute(ProcessInteraction interaction, Command command) throws AndroidExecutionException {
        try {
            Future<Process> processFuture = service.submit(new SpawnedProcess(true, command));
            Process process = processFuture.get();
            ProcessExecution execution = service.submit(
                    new ProcessOutputConsumer(new ProcessExecution(process, command.get(0)), interaction)).get();
            process.waitFor();
            if (execution.executionFailed()) {
                throw new AndroidExecutionException("Invocation of {0} failed with {1}", command, execution.getExitCode());
            }
            return execution;
        } catch (InterruptedException e) {
            throw new AndroidExecutionException(e, "Unable to execute {0}, interrupted", command);
        } catch (ExecutionException e) {
            throw new AndroidExecutionException(e, "Unable to execute {0}, failed", command);
        }
    }

    /**
     * Executes a process defined by command. Process output is discarded. Waits for process to finish and checks if process
     * finished with status code 0
     *
     * @param interaction command interaction
     * @param command command to be execution
     * @return spawned process execution
     * @throws AndroidExecutionException if anything goes wrong
     */
    public ProcessExecution execute(Command command) throws AndroidExecutionException {
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

        private final Command command;
        private boolean redirectErrorStream;

        public SpawnedProcess(boolean redirectErrorStream, Command command) {
            this.redirectErrorStream = redirectErrorStream;
            this.command = command;
        }

        @Override
        public Process call() throws Exception {
            ProcessBuilder builder = new ProcessBuilder(command.getAsArray());
            builder.environment().putAll(ENVIRONMENT_PROPERTIES);
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
    private static class ProcessOutputConsumer implements Callable<ProcessExecution> {

        private static final Logger log = Logger.getLogger(ProcessOutputConsumer.class.getName());
        private static final String NL = System.getProperty("line.separator");

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

            try {
                // read character by character
                int i;
                StringBuilder line = new StringBuilder();
                while ((i = reader.read()) != -1) {
                    char c = (char) i;

                    // add the character
                    line.append(c);

                    // check if we are have to respond with an input
                    String question = line.toString();
                    String answer = interaction.repliesTo(question);
                    if (answer != null) {
                        log.log(Level.FINEST, "{0} outputs: {1}, responded with: ", new Object[] { execution.getProcessId(),
                                question, answer });
                        execution.replyWith(answer);
                    }

                    // save output
                    // adb command writes its output with ends of lines as "\\n"
                    // ignoring Windows conventions which recognize "\r\n" as the
                    // end of the line
                    if (line.indexOf("\n") != -1 || line.indexOf(NL) != -1) {
                        String wholeLine = line.toString();
                        log.log(Level.FINEST, "{0} outputs: {1}", new Object[] { execution.getProcessId(), wholeLine });

                        // propagate output/error to user
                        if (interaction.shouldOutput(wholeLine)) {
                            System.out.print(wholeLine);
                        }
                        if (interaction.shouldOutputToErr(wholeLine)) {
                            System.err.print("ERROR (" + execution.getProcessId() + "):" + wholeLine);
                        }

                        execution.appendOutput(wholeLine);
                        line = new StringBuilder();
                    }
                }
                // handle last line
                if (line.length() > 1) {
                    String wholeLine = line.toString();
                    log.log(Level.FINEST, "{0} outputs: {1}", new Object[] { execution.getProcessId(), wholeLine });

                    // propagate output/error to user
                    if (interaction.shouldOutput(wholeLine)) {
                        System.out.println(wholeLine);
                    }
                    if (interaction.shouldOutputToErr(wholeLine)) {
                        System.err.println("ERROR (" + execution.getProcessId() + "):" + wholeLine);
                    }

                    execution.appendOutput(wholeLine);
                }
            } catch (IOException e) {
            }

            return execution;
        }
    }
}
