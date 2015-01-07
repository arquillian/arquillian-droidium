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
package org.arquillian.droidium.container.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceOutputReciever;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Validate;

/**
 * Checks if output lines from command on Android device contains package name of application.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class Monkey implements AndroidDeviceOutputReciever {

    private static final Logger logger = Logger.getLogger(Monkey.class.getName());

    private final Writer output;

    private boolean active = false;

    private boolean contains = false;

    private final String waitForString;

    /**
     *
     * @param output where to write output from receiver
     * @param waitForString for which string to wait
     * @param contains set to true if we are waiting for the presence of the {@code waitForString} in the {@code output}, set to
     *        false when we are waiting until {@code waitForString} will be not present in the {@code output}
     * @throws IOException
     */
    public Monkey(File output, String waitForString, boolean contains) throws IOException {
        Validate.notNull(output, "File to write logs for Monkey can't be null!");
        Validate.notNullOrEmpty(waitForString, "String to wait for in Monkey can't be null nor empty!");
        this.output = new FileWriter(output);
        this.waitForString = waitForString;
        this.contains = contains;
    }

    @Override
    public void processNewLines(String[] lines) {
        for (String line : lines) {
            logger.finest(line);
            try {
                output.append(line).append("\n").flush();
                if (contains) {
                    if (line.contains(waitForString)) {
                        this.active = true;
                        return;
                    }
                } else {
                    if (!line.contains(waitForString)) {
                        this.active = true;
                        return;
                    }
                }
            } catch (IOException e) {
                // ignore output
            }
        }
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public boolean isActive() {
        return active;
    }

    public static void wait(AndroidDevice device, Monkey monkey, String command) {

        final int retries = 5;

        for (int i = 0; i < retries; i++) {
            device.executeShellCommand(command, monkey);
            if (monkey.isActive()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new AndroidExecutionException(String.format("Waiting for monkey executing command \"%s\" timeouted in %d seconds.",
            command, retries));
    }
}
