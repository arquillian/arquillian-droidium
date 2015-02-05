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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.Task;
import org.arquillian.spacelift.task.os.CommandTool;

/**
 * Unlocks emulator.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class UnlockEmulatorTask extends Task<Object, Void> {

    private static final Logger logger = Logger.getLogger(UnlockEmulatorTask.class.getName());

    private String serialNumber;

    private AndroidSDK sdk;

    public UnlockEmulatorTask serialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    public UnlockEmulatorTask sdk(AndroidSDK sdk) {
        this.sdk = sdk;
        return this;
    }

    @Override
    protected Void process(Object input) throws Exception {

        try {
            CommandTool ct = Spacelift.task(CommandTool.class)
                .addEnvironment(sdk.getPlatformConfiguration().getAndroidSystemEnvironmentProperties());

            ct.command(new CommandBuilder(sdk.getAdbPath())
                .parameters("-s", serialNumber, "shell", "input", "keyevent", "82"))
                .execute()
                .await();

            ct.command(new CommandBuilder(sdk.getAdbPath())
                .parameters("-s", serialNumber, "shell", "input", "keyevent", "4"))
                .execute()
                .await();
        } catch (ExecutionException ex) {
            logger.log(Level.WARNING, "Unlocking device failed", ex);
        }

        return null;
    }

}
