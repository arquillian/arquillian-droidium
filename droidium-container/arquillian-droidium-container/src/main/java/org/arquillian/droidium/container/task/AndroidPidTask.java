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
package org.arquillian.droidium.container.task;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.task.Task;
import org.arquillian.spacelift.task.os.CommandTool;


/**
 * Returns PID of some Android process according to its name appeared in Android "ps" output.
 *
 * It returns PID of value -1 if there is not such process with chained name.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidPidTask extends Task<String, Integer> {

    private AndroidSDK androidSdk;

    public AndroidPidTask androidSdk(AndroidSDK androidSdk) {
        Validate.notNull(androidSdk, "Android SDK is null object.");
        this.androidSdk = androidSdk;
        return this;
    }

    @Override
    protected Integer process(String androidProcessName) throws Exception {
        Validate.notNullOrEmpty(androidProcessName, "Android process name to get PID of is a null object or an empty string.");
        Validate.notNull(androidSdk, "Android SDK is a null object!");

        ProcessResult psResult = Spacelift.task(CommandTool.class).programName(androidSdk.getAdbPath())
            .addEnvironment(androidSdk.getPlatformConfiguration().getAndroidSystemEnvironmentProperties())
            .parameter("shell")
            .parameter("ps")
            .execute().await();

        int pid = -1;

        for (String psLine : psResult.output()) {
            if (psLine != null && psLine.trim().endsWith(androidProcessName)) {
                pid = parsePid(psLine.trim(), androidProcessName);
                break;
            }
        }

        return pid;
    }

    private int parsePid(String outputLine, String androidProcessName) {

        String[] splitting = outputLine.split(" +");

        if (splitting.length < 2) {
            throw new IllegalStateException(String.format("Unable to get PID from output line of Android 'ps' command for '%s' process.", androidProcessName));
        }

        try {
            return Integer.parseInt(splitting[1]);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException(String.format("Unable to get PID from output line of Android 'ps' command for '%s' process.", androidProcessName));
        }
    }
}
