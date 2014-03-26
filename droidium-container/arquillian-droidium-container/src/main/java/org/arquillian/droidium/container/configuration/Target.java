/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecutor;

/**
 * Representation of an Android Target
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class Target {

    private final String name;

    private final String apiLevel;

    private final String fullName;

    private final String abi;

    public Target(String fullName) throws AndroidContainerConfigurationException {

        // form Google Inc.:Google APIs:15
        if (fullName.contains(":")) {
            this.name = fullName.substring(0, fullName.lastIndexOf(":"));
            this.apiLevel = fullName.substring(fullName.lastIndexOf(":") + 1);
        } else {
            // form android-15
            this.name = fullName.substring(0, fullName.lastIndexOf("-"));
            this.apiLevel = fullName.substring(fullName.lastIndexOf("-") + 1);
        }

        this.fullName = fullName;
        this.abi = parseAbi();
    }

    public Target(String name, String apiLevel, String fullName) throws AndroidContainerConfigurationException {
        this.name = name;
        this.apiLevel = apiLevel;
        this.fullName = fullName;
        this.abi = parseAbi();
    }

    public String getName() {
        return name;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public String getAbi() {
        return abi;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return fullName;
    }

    public boolean matches(String targetLabel) {
        return fullName.equals(targetLabel) || getName().equals(targetLabel) || getApiLevel().equals(targetLabel);
    }

    public static Target findMatchingTarget(ProcessExecutor executor, String androidToolPath, String targetLabel)
        throws AndroidContainerConfigurationException {

        // target from config can be like "19", "android-19",
        // "Google Inc.:Google APIs:19" or "Google Inc.:Google APIs x86:19"
        for (Target target : getAvailableTargets(executor, androidToolPath)) {
            if (target.matches(targetLabel)) {
                return target;
            }
        }

        throw new AndroidContainerConfigurationException(String.format("There is not any target with target name '%s'",
            targetLabel));
    }

    /**
     * Returns all targets available in given Android SDK installation. Executed an external process to
     * discover the details.
     *
     * @param executor Process executor
     * @param androidToolPath Path to {@code android} tool
     * @return
     * @throws AndroidContainerConfigurationException if there are no available targets
     */
    public static List<Target> getAvailableTargets(ProcessExecutor executor, String androidToolPath)
        throws AndroidContainerConfigurationException {

        List<String> targetsOutput = executor.execute(
            new CommandBuilder()
                .add(androidToolPath)
                .add("list")
                .add("target")
                .add("-c")
                .build()).getOutput();

        Collections.sort(targetsOutput);
        Collections.reverse(targetsOutput);

        List<Target> targets = new ArrayList<Target>();

        for (String target : targetsOutput) {
            if (target != null && !"".equals(target.trim())) {
                targets.add(new Target(target.trim()));
            }
        }

        if (targets.size() == 0) {
            throw new AndroidContainerConfigurationException("There are not any available targets found on your system!");
        }

        return targets;
    }

    private String parseAbi() throws AndroidContainerConfigurationException {
        // Starting with Android 4.4, there is x86 in image name
        // Google Inc.:Google APIs x86:19
        for (SystemImage abi : SystemImage.values()) {
            if (fullName.contains(abi.getName())) {
                return abi.getName();
            }
        }

        int apiLevelInt = -1;
        try {
            apiLevelInt = Integer.parseInt(apiLevel);
        } catch (NumberFormatException e) {
            throw new AndroidContainerConfigurationException("Android API Level is not a number (" + apiLevel + ")");
        }

        // Google Inc.:Google APIs:18
        // Starting with Android 4.0, there is ARMv7a by default
        if (fullName.contains("Google Inc.") && apiLevelInt > 14) {
            return SystemImage.ARMEABIV7A.getName();
        }
        // we do not support Android 3.x
        // Android 2.3, however contains X86 only
        else if (fullName.contains("Google Inc.") && apiLevelInt == 10) {
            return SystemImage.X86.getName();
        }

        // android-x
        return SystemImage.X86.getName();
    }

}