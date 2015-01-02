/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.container.impl;

import java.util.List;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * Checks if some process on Android device is running or not, looking at its output from 'ps' command on Android.
 *
 * Returns true if process is running (found in ps' output), false otherwise.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidProcessRunningTask extends Task<String, Boolean> {

    private AndroidSDK androidSdk;

    public AndroidProcessRunningTask androidSdk(AndroidSDK androidSdk) {
        Validate.notNull(androidSdk, "AndroidSDK is null object!");
        this.androidSdk = androidSdk;
        return this;
    }

    @Override
    protected Boolean process(String processName) throws Exception {
        Validate.notNullOrEmpty(processName, "Process name to get running status of is a null object or it is an empty string!");
        Validate.notNull(androidSdk, "AndroidSDK is null object!");

        List<String> psOutput = Tasks.prepare(CommandTool.class).programName(androidSdk.getAdbPath())
            .addEnvironment(androidSdk.getPlatformConfiguration().getAndroidSystemEnvironmentProperties())
            .parameter("shell")
            .parameter("ps")
            .execute().await()
            .output();

        boolean running = false;

        for (String outputLine : psOutput) {
            if (outputLine != null) {
                if (outputLine.trim().endsWith(processName)) {
                    running = true;
                    break;
                }
            }
        }

        return running;
    }

}
