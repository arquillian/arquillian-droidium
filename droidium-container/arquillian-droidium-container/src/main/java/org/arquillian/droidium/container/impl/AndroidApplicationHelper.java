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
        return getAAPTBadgingOutput(apk).getSingleProperty("launchable-activity", "name");
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
        return getAAPTBadgingOutput(apk).getSingleProperty("package", "name");
    }

    /**
     *
     * @param apk apk file to get the name of application base package of
     * @return name of application base package
     */
    public String getApplicationVersion(File apk) {
        return getAAPTBadgingOutput(apk).getSingleProperty("package", "versionName");
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

    private BadgingOutput getAAPTBadgingOutput(File apk) {
        final Command command = new CommandBuilder(sdk.getAaptPath())
            .parameter("dump")
            .parameter("badging")
            .parameter(apk.getAbsolutePath())
            .build();

        return new BadgingOutput(Tasks.prepare(CommandTool.class)
            .command(command)
            .execute()
            .await()
            .output());
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
     * A wrapper on top of aapt d badging output
     * @author kpiwko
     *
     */
    static final class BadgingOutput {

        private final List<String> badgingOutput;

        public BadgingOutput(List<String> output) {
            this.badgingOutput = output;
        }

        /**
         * Retrieves all properties that start with given propertyName. Output is expected in format:
         * propertyName:? value
         * @param propertyName
         * @return Returns a collection with all values that matched output
         */
        public List<String> getProperty(String propertyName) {

            List<String> output = new ArrayList<String>();

            Pattern propertyPattern = Pattern.compile("^(\\s*)" + propertyName + "(:?)(.*)$");
            for (String line : badgingOutput) {
                Matcher m = propertyPattern.matcher(line);
                if (m.find()) {
                    output.add(m.group(3));
                }
            }

            return output;

        }

        /**
         * Retrieves all properties that start with given propertyName and parses all values there that start with propertySubName. Output is expected in format:
         * propertyName:? propertySubName='value'
         * @param propertyName
         * @param propertySubName
         * @return Returns a collection with all values that matched output
         */
        public List<String> getProperty(String propertyName, String propertySubName) {
            List<String> partialResults = getProperty(propertyName);
            List<String> output = new ArrayList<String>(partialResults.size());

            Pattern subPropertyPattern = Pattern.compile(propertySubName + "=[\'\"]([^\'\"]+)[\'\"]");
            for (String line : partialResults) {
                Matcher m = subPropertyPattern.matcher(line);
                if (m.find()) {
                    output.add(m.group(1));
                }
            }

            return output;
        }

        /**
         * Transforms list of properties to a single list or returns empty string if the list was empty
         * @param propertyName
         * @return
         */
        public String getSingleProperty(String propertyName) {
            List<String> output = getProperty(propertyName);
            if (output.size() > 0) {
                return output.iterator().next();
            }

            logger.log(Level.SEVERE, "Unable to get {0} from aapt output: {1}", new Object[] {propertyName, badgingOutput});
            return "";
        }

        /**
         * Transforms list of properties to a single list or returns empty string if the list was empty
         * @param propertyName
         * @param propertySubName
         * @return
         */
        public String getSingleProperty(String propertyName, String propertySubName) {
            List<String> output = getProperty(propertyName, propertySubName);
            if (output.size() > 0) {
                return output.iterator().next();
            }

            logger.log(Level.SEVERE, "Unable to get {0}:{1} from aapt output: {2}", new Object[] {propertyName, propertySubName, badgingOutput});
            return "";
        }
    }

}