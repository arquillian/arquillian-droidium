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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    private final ShutDownThreadHolder shutdownThreads;
    private final ExecutorService service;
    private final ScheduledExecutorService scheduledService;

    public ProcessExecutor() {
        this.shutdownThreads = new ShutDownThreadHolder();
        this.service = Executors.newCachedThreadPool();
        this.scheduledService = Executors.newScheduledThreadPool(1);
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
     * Spawns a process defined by command. Process output is discarded.
     *
     * @param command
     * @return spawned process
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Process spawn(List<String> command) throws InterruptedException, ExecutionException {
        return spawn(command.toArray(new String[0]));
    }

    /**
     * Spawns a process defined by command. Process output is discarded
     *
     * @param command the command to be executed
     * @return spawned process
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public Process spawn(String... command) throws InterruptedException, ExecutionException {
        Future<Process> processFuture = service.submit(new SpawnedProcess(true, command));
        Process process = processFuture.get();
        service.submit(new ProcessOutputConsumer(new ProcessWithId(process, command[0])));
        shutdownThreads.addHookFor(process);

        return process;
    }

    /**
     *
     * @param input
     * @param command
     * @return pending results of the task
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<String> execute(Map<String, String> input, String... command) throws InterruptedException,
        ExecutionException {
        Future<Process> processFuture = service.submit(new SpawnedProcess(true, command));
        Process process = processFuture.get();
        return service.submit(new ProcessOutputConsumer(new ProcessWithId(process, command[0]), input)).get();
    }

    /**
     *
     * @param command
     * @return pending results of the task
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<String> execute(String... command) throws InterruptedException, ExecutionException {
        return execute(Collections.<String, String> emptyMap(), command);
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

    private static class InputSanitizer {
        public static List<String> sanitizeArguments(String... command) {
            List<String> cmd = new ArrayList<String>(command.length);
            for (String c : command) {
                if (c != null && c.length() > 0) {
                    cmd.add(c);
                }
            }

            return cmd;
        }
    }

    private static class SpawnedProcess implements Callable<Process> {

        private final String[] command;
        private boolean redirectErrorStream;

        public SpawnedProcess(boolean redirectErrorStream, String... command) {
            this.redirectErrorStream = redirectErrorStream;
            this.command = command;
        }

        @Override
        public Process call() throws Exception {
            ProcessBuilder builder = new ProcessBuilder(InputSanitizer.sanitizeArguments(command));
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
    private static class ProcessOutputConsumer implements Callable<List<String>> {

        private static final Logger log = Logger.getLogger(ProcessOutputConsumer.class.getName());
        private static final String NL = System.getProperty("line.separator");

        private final Process process;
        private final Map<String, String> inputOutputMap;

        public ProcessOutputConsumer(ProcessWithId process, Map<String, String> inputOutputMap) {
            this.process = process;
            this.inputOutputMap = inputOutputMap;
        }

        public ProcessOutputConsumer(ProcessWithId process) {
            this(process, Collections.<String, String> emptyMap());
        }

        @Override
        public List<String> call() throws Exception {
            final InputStream stream = process.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            final List<String> output = new ArrayList<String>();

            try {
                // read character by character
                int i;
                StringBuilder line = new StringBuilder();
                while ((i = reader.read()) != -1) {
                    char c = (char) i;

                    // add the character
                    line.append(c);

                    // check if we are have to respond with an input
                    String key = line.toString();
                    if (inputOutputMap.containsKey(key)) {

                        if (log.isLoggable(Level.FINEST)) {
                            log.log(Level.FINEST, "{0} outputs: {1}, responded with: ",
                                new Object[] { process, line.toString(), inputOutputMap.get(key) });
                        }
                        OutputStream ostream = process.getOutputStream();
                        ostream.write(inputOutputMap.get(key).getBytes());
                        ostream.flush();

                    }

                    // save output
                    // adb command writes its output with ends of lines as "\\n"
                    // ignoring Windows conventions which recognize "\r\n" as the
                    // end of the line
                    if (line.indexOf("\n") != -1 || line.indexOf(NL) != -1) {
                        String wholeLine = line.toString();
                        if (log.isLoggable(Level.FINEST)) {
                            log.log(Level.FINEST, "{0} outputs: {1}", new Object[] { process, wholeLine });
                        } else if (wholeLine.toLowerCase().startsWith("error")) {
                            log.log(Level.SEVERE, "{0} outputs: {1}", new Object[] { process, wholeLine });
                        }
                        output.add(wholeLine);
                        line = new StringBuilder();
                    }
                }
                if (line.length() > 1) {
                    String wholeLine = line.toString();
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "{0} outputs: {1}", new Object[] { process, wholeLine });
                    } else if (wholeLine.toLowerCase().startsWith("error")) {
                        log.log(Level.SEVERE, "{0} outputs: {1}", new Object[] { process, wholeLine });
                    }
                    output.add(wholeLine);
                }
            } catch (IOException e) {
            }

            return output;
        }
    }

    /**
     * Represents a proccess with id
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     *
     */
    private class ProcessWithId extends Process {

        private final Process process;
        private final String id;

        public ProcessWithId(Process process, String id) {
            this.id = id;
            this.process = process;
        }

        @Override
        public OutputStream getOutputStream() {
            return process.getOutputStream();
        }

        @Override
        public InputStream getInputStream() {
            return process.getInputStream();
        }

        @Override
        public InputStream getErrorStream() {
            return process.getErrorStream();
        }

        @Override
        public int waitFor() throws InterruptedException {
            return process.waitFor();
        }

        @Override
        public int exitValue() {
            return process.exitValue();
        }

        @Override
        public void destroy() {
            process.destroy();
        }

        @Override
        public String toString() {
            return "Process: " + id;
        }

    }

}
