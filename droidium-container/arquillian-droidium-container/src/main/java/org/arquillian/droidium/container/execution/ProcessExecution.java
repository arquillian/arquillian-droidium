/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a process execution
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProcessExecution {

    private final String processId;

    private final Process process;

    private final List<String> output;

    private final OutputStream ostream;

    private final PrintStream stdout;

    private final PrintStream stderr;

    /**
     * Creates a process execution, add an id to the process
     *
     * @param process
     * @param processId
     */
    public ProcessExecution(Process process, String processId, PrintStream stdout, PrintStream stderr) {
        this.process = process;
        this.processId = processId;
        this.output = new ArrayList<String>();
        this.ostream = new BufferedOutputStream(process.getOutputStream());
        this.stdout = stdout;
        this.stderr = stderr;
    }

    /**
     *
     * @return process
     */
    public Process getProcess() {
        return process;
    }

    /**
     *
     * @return process id
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Adds a line to output of this process
     *
     * @param line
     * @return this
     */
    public ProcessExecution appendOutput(CharSequence line) {
        output.add(line.toString());
        return this;
    }

    /**
     *
     * @return current output of the process
     */
    public List<String> getOutput() {
        return output;
    }

    /**
     * Writes {@code reply} into process input stream. Type of answer might result into closing the stream itself
     *
     * @param reply
     * @return this
     * @throws IOException
     */
    public ProcessExecution replyWith(Answer reply) throws IOException {

        switch (reply.getType()) {
            case NONE:
                return this;
            case TEXT:
                ostream.flush();
                ostream.write(reply.getBytes());
                ostream.flush();
                break;
            case EOF:
                ostream.flush();
                ostream.close();
                break;
        }

        return this;
    }

    /**
     * Checks whether process has finished
     *
     * @return true if process has finished, false otherwise
     */
    public boolean isFinished() {
        try {
            process.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }

    /**
     * Returns exit code
     *
     * @return exit code
     * @throws IllegalStateException thrown if process is not finished
     */
    public int getExitCode() throws IllegalStateException {

        if (!isFinished()) {
            throw new IllegalStateException("Process " + processId + " is not yet finished");
        }

        return process.exitValue();
    }

    /**
     * Checks whether {@link ProcessExecution#getExitCode()} was {@code 0}
     *
     * @return true if exit code is not 0, false otherwise
     */
    public boolean executionFailed() {
        return getExitCode() != 0;
    }

    public PrintStream getStderr() {
        return stderr;
    }

    public PrintStream getStdout() {
        return stdout;
    }

}
