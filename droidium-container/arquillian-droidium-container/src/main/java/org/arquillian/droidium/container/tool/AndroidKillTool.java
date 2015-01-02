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

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.Tool;

/**
 * Calls Android's kill binary and kills processes on Android side.
 *
 * Default signal sent to a process is SIGKILL (9) when not specified otherwise.
 *
 * When using in chain mode, chained value represents PID of Android process to kill.
 *
 * Returned {@link ProcessResult} after the execution of this tool is a process result of adb invocation on a host side and
 * tells nothing about the result of kill process on Android side.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidKillTool extends Tool<Integer, ProcessResult> {

    private static final Logger logger = Logger.getLogger(AndroidKillTool.class.getName());

    private AndroidSDK androidSdk;

    private int signum = 9; // default is SIGKILL

    public AndroidKillTool androidSdk(AndroidSDK androidSdk) {
        Validate.notNull(androidSdk, "Android Sdk is null object.");
        this.androidSdk = androidSdk;
        return this;
    }

    public AndroidKillTool signum(int signum) {
        if (signum > 0) {
            this.signum = signum;
        } else {
            logger.log(Level.INFO, String.format("Given signal number '%s' is lower then 0 and will be ignored.", signum));
        }

        return this;
    }

    public AndroidKillTool signum(String signum) {
        Validate.notNullOrEmpty(signum, "Signal number is a null object or an empty string!");

        try {
            signum(Integer.parseInt(signum));
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, String.format("Unable to convert given signal number '%s' into a number.", signum));
        }

        return this;
    }

    @Override
    protected Collection<String> aliases() {
        return Arrays.asList(new String[] { "android_kill" });
    }

    @Override
    protected ProcessResult process(Integer pid) throws Exception {
        Validate.notNull(pid, "PID for process to kill is a null object!");
        Validate.notNull(androidSdk, "Android SDK is a null object!");

        if (pid <= 0) {
            throw new IllegalStateException("PID to kill is lower then 0.");
        }

        ProcessResult processResult = Tasks.prepare(CommandTool.class)
            .programName(androidSdk.getAdbPath())
            .addEnvironment(androidSdk.getPlatformConfiguration().getAndroidSystemEnvironmentProperties())
            .parameters("shell", "kill", "-" + signum, pid.toString())
            .execute().await();

        return processResult;
    }
}
