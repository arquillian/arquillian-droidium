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
package org.arquillian.droidium.container.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * Provides various helper methods for Android packages.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidApplicationHelper {

    private static final Logger logger = Logger.getLogger(AndroidApplicationHelper.class.getName());

    private final AndroidSDK sdk;

    /**
     *
     * @param sdk
     * @throws IllegalArgumentException if either {@code Executor} or {@code sdk} is a null object
     */
    public AndroidApplicationHelper(AndroidSDK sdk) {
        Validate.notNull(sdk, "Android SDK to set can not be a null object!");
        this.sdk = sdk;
    }

    /**
     *
     * @param apk apk file to get the name of main activity from
     * @return name of main activity of application
     */
    public String getApplicationMainActivity(File apk) {
        try {
            return parseProperty(getAAPTBadgingOutput(apk), "launchable-activity");
        } catch (AndroidExecutionException e) {
            logger.log(Level.SEVERE, "Execution exception while getting name of main application activity occured.", e);
            return null;
        }
    }

    /**
     *
     * @param apk apk file name to get the name of main activity from
     * @return name of main activity of application
     */
    public String getApplicationMainActivity(String apk) {
        return getApplicationMainActivity(new File(apk));
    }

    /**
     *
     * @param apk apk file to get the name of application base package of
     * @return name of application base package
     */
    public String getApplicationBasePackage(File apk) {
        try {
            return parseProperty(getAAPTBadgingOutput(apk), "package");
        } catch (AndroidExecutionException e) {
            logger.log(Level.SEVERE, "Execution exception while getting name of main application package occured.", e);
            return null;
        }
    }

    /**
     *
     * @param apk file to get the list of activities of
     * @return list of activities which are in the package with fully qualified class name
     */
    public List<String> getActivities(File apk) {
        List<String> activities = new ArrayList<String>();

        try {
            activities = filterActivities(getAAPTXmlTreeOutput(apk));
        } catch (AndroidExecutionException e) {
            logger.log(Level.SEVERE, "Unable to get list of activities for file: " + apk.getAbsolutePath(), e);
        }
        return activities;
    }

    /**
     *
     * @param apkFile file to get the list of activities of
     * @return list of activities which are in the package with fully qualified class name
     */
    public List<String> getActivities(String apkFile) {
        return getActivities(new File(apkFile));
    }

    /**
     *
     * @param apk apk file name to get the name of application base package of
     * @return name of application base package
     */
    public String getApplicationBasePackage(String apk) {
        return getApplicationBasePackage(new File(apk));
    }

    private List<String> getAAPTBadgingOutput(File apk) {
        final Command command = new CommandBuilder(sdk.getAaptPath())
            .parameter("dump")
            .parameter("badging")
            .parameter(apk.getAbsolutePath())
            .build();

        return Tasks.prepare(CommandTool.class)
            .command(command)
            .execute()
            .await()
            .output();
    }

    private List<String> getAAPTXmlTreeOutput(File apkFile) {
        final Command command = new CommandBuilder(sdk.getAaptPath())
            .parameter("dump")
            .parameter("xmltree")
            .parameter(apkFile.getAbsolutePath())
            .parameter("AndroidManifest.xml")
            .build();

        return Tasks.prepare(CommandTool.class)
            .command(command)
            .execute()
            .await()
            .output();
    }

    private List<String> filterActivities(List<String> output) {
        String packageName;
        List<String> activities = new ArrayList<String>();

        packageName = getPackageName(output);

        for (int i = 0; i < output.size(); i++) {
            if (output.get(i).trim().startsWith("E: activity")) {
                while (!output.get(++i).trim().contains("A: android:name")) {
                }

                String activityName = getActivityName(output.get(i));

                if (activityName.startsWith(".")) {
                    activities.add(packageName + activityName);
                } else if (!activityName.startsWith(".") && !activityName.contains(".")) {
                    activities.add(packageName + "." + activityName);
                } else {
                    activities.add(activityName);
                }
            }
        }

        return activities;
    }

    private String getPackageName(List<String> output) {
        for (String line : output) {
            if (line.contains("package")) {
                Pattern packagePattern = Pattern.compile("package=[\'\"]([^\'\"]+)[\'\"]");
                Matcher m = packagePattern.matcher(line);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        throw new IllegalStateException("no package name found in dump xmltree for AndroidManifest.xml");
    }

    private String getActivityName(String line) {
        Pattern activityPattern = Pattern.compile("=[\"]([^\"]+)[\"]");
        Matcher m = activityPattern.matcher(line);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalStateException("no activity name found in AndroidManifest.xml on line: " + line);
    }

    /**
     * Parses property value from list of lines, returns the first parsed result. Property to parse is somewhere at line in form
     * {@code property .... name=stringToGet}
     *
     * @param lines list of lines to get property value from
     * @param property property to get, is situated at the beginning of line
     * @return stringToGet
     */
    private String parseProperty(List<String> lines, String property) {
        if (lines == null || lines.size() == 0) {
            return null;
        }

        Pattern packagePattern = Pattern.compile("name=[\'\"]([^\'\"]+)[\'\"]");
        Matcher m;

        for (String line : lines) {
            if (line.contains(property)) {
                m = packagePattern.matcher(line);
                while (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }

}