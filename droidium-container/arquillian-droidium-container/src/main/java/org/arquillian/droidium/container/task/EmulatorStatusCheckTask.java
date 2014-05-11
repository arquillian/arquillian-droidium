/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.container.task;

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.spacelift.execution.Execution;
import org.arquillian.spacelift.execution.ExecutionCondition;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.process.ProcessResult;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class EmulatorStatusCheckTask extends Task<Object, Void> {

    public static final ExecutionCondition<ProcessResult> isBootedCondition = new EmulatorStatusCheckTask.EmulatorIsBootedExecutionCondition();

    private Execution<ProcessResult> emulatorExecution;

    public Task<Object, Void> execution(Execution<ProcessResult> emulatorExecution) {
        this.emulatorExecution = emulatorExecution;
        return this;
    }

    @Override
    protected Void process(Object input) throws Exception {

        if (emulatorExecution.isFinished() && emulatorExecution.hasFailed()) {

            ProcessResult processDetails = emulatorExecution.await();

            if (processDetails != null) {
                StringBuilder sb = new StringBuilder();

                for (String line : processDetails.output()) {
                    sb.append(line).append("\n");
                }

                throw new AndroidExecutionException(String.format("Starting of emulator failed with exit value {0} and output {1}",
                    processDetails.exitValue(), sb.toString()));
            } else {
                throw new IllegalStateException("Execution of emulator process failed.");
            }
        }

        return null;
    }

    private static class EmulatorIsBootedExecutionCondition implements ExecutionCondition<ProcessResult> {

        @Override
        public boolean satisfiedBy(ProcessResult processResult) throws ExecutionException {

            for (String line : processResult.output()) {
                if (line.contains("[ro.runtime.firstboot]")) {
                    return true;
                }
            }

            return false;
        }
    }

}
