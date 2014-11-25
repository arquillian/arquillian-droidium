/*
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
package org.arquillian.droidium.container.configuration.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TargetParser {

    private AndroidSDK androidSdk;

    private DroidiumPlatformConfiguration platformConfiguration;

    public TargetParser() {
        // only for testing purposes
    }

    public TargetParser(AndroidSDK androidSdk, DroidiumPlatformConfiguration platformConfiguration) {
        Validate.notNull(androidSdk, "Android SDK is null!");
        Validate.notNull(platformConfiguration, "Droidium platform configuration is null!");

        this.androidSdk = androidSdk;
        this.platformConfiguration = platformConfiguration;
    }

    public List<Target> parse() {

        Map<String, String> androidEnvironment = new HashMap<String, String>();
        androidEnvironment.put("ANDROID_HOME", platformConfiguration.getAndroidHome());
        androidEnvironment.put("ANDROID_SDK_HOME", platformConfiguration.getAndroidSdkHome());

        List<String> output = Tasks.prepare(CommandTool.class)
            .addEnvironment(androidEnvironment)
            .programName(androidSdk.getAndroidPath())
            .parameters("list", "target")
            .execute().await()
            .output();

        return parseAndroidListTargetOutput(new LinkedList<String>(output));
    }

    public List<Target> parseAndroidListTargetOutput(List<String> outputLines) {
        List<Target> targets = new ArrayList<Target>();

        // if we hit just "Available Android targets:"
        if (outputLines.size() < 1) {
            return targets;
        }

        // remove first line
        if (outputLines.get(0).contains("Available Android targets:")) {
            outputLines.remove(0);
        }

        List<List<String>> targetLinesList = new ArrayList<List<String>>();

        List<String> targetLines = new ArrayList<String>();

        for (String line : outputLines) {
            if (line.contains("----------")) {
                if (!targetLines.isEmpty()) {
                    targetLinesList.add(targetLines);
                    targetLines = new ArrayList<String>();
                }
            } else {
                targetLines.add(line.trim());
            }
        }

        if (!targetLines.isEmpty()) {
            targetLinesList.add(targetLines);
        }

        for (List<String> tList : targetLinesList) {
            targets.add(TargetBuilder.build(tList));
        }

        // target with the lowest id will be first
        Collections.sort(targets);

        return targets;
    }

}
