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
package org.arquillian.droidium.container.tool;

import java.io.File;

import org.arquillian.droidium.container.api.ScreenrecordOptions;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ScreenRecordToolBuilder {

    private AndroidSDK androidSdk;

    private ScreenrecordOptions options;

    private String remoteFilePath;

    public ScreenRecordToolBuilder androidSdk(AndroidSDK androidSdk) {
        this.androidSdk = androidSdk;
        return this;
    }

    /**
     * The set value of timeLimit field will be ignored and timeLimit will default to 180 seconds.
     *
     * @param options
     * @return this
     */
    public ScreenRecordToolBuilder options(ScreenrecordOptions options) {
        this.options = options;
        return this;
    }

    /**
     *
     * @param remoteFilePath file path of video on Android device
     * @return this
     */
    public ScreenRecordToolBuilder remoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
        return this;
    }

    /**
     *
     * @param remoteFile file of video on Android device
     * @return this
     */
    public ScreenRecordToolBuilder remoteFile(File remoteFile) {
        return remoteFilePath(remoteFile.getAbsolutePath());
    }

    /**
     * The set value of timeLimit field will be ignored and timelimit will default to 180 seconds.
     *
     * @return prepared command tool which records videos of Android device
     */
    public CommandTool build() {
        Validate.notNull(androidSdk, "You have not set AndroidSDK.");
        Validate.notNull(options, "You have not set options.");
        Validate.notNullOrEmpty(remoteFilePath, "You have not set remote file path or it is empty string.");

        CommandTool screenRecorderTool = Tasks.prepare(CommandTool.class)
            .programName(androidSdk.getAdbPath())
            .addEnvironment(androidSdk.getPlatformConfiguration().getAndroidSystemEnvironmentProperties())
            .parameter("shell")
            .parameter("screenrecord");

        if (options.width > 0 && options.height > 0) {
            screenRecorderTool.parameters("--size", options.width + "x" + options.height);
        }

        if (options.bitrate > 0) {
            screenRecorderTool.parameters("--bit-rate", Long.toString(options.bitrate));
        }

        screenRecorderTool.parameters("--time-limit", "180"); // maximum value of screenrecord command

        screenRecorderTool.parameter(remoteFilePath);

        return screenRecorderTool;
    }
}