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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecutor;

/**
 * Representation of an Android Target
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class Target {
    private static final Logger log = Logger.getLogger(Target.class.getName());

    protected static final Pattern ANDROID_LEVEL_PATTERN = Pattern.compile("[0-9]+");

    protected static final Pattern ANDROID_PATTERN = Pattern.compile("android-([0-9]+)");

    protected static final Pattern GOOGLE_ADDON_PATTERN = Pattern.compile("Google Inc.:([\\w ]+):([0-9]+)");

    private final String name;

    private final boolean isGoogleAddon;

    private final String imagesSubdirectory;

    private final int apiLevel;

    protected Target(String name) throws AndroidContainerConfigurationException, IllegalArgumentException {

        if (name == null) {
            throw new IllegalArgumentException("Android target was null");
        }

        // get additional information about target
        Matcher m = null;
        if ((m = ANDROID_LEVEL_PATTERN.matcher(name)).matches()) {
            this.name = "android-" + name;
            this.apiLevel = Integer.parseInt(m.group());
            this.isGoogleAddon = false;
            this.imagesSubdirectory = "android-" + apiLevel;
        }

        else if ((m = ANDROID_PATTERN.matcher(name)).matches()) {
            this.name = name;
            this.apiLevel = Integer.parseInt(m.group(1));
            this.isGoogleAddon = false;
            this.imagesSubdirectory = "android-" + apiLevel;
        }
        else if ((m = GOOGLE_ADDON_PATTERN.matcher(name)).matches()) {
            this.name = name;
            this.apiLevel = Integer.parseInt(m.group(2));
            this.isGoogleAddon = true;
            String type = m.group(1);
            // here, we do parsing of type
            if (type.startsWith("Google APIs") && type.contains("x86")) {
                this.imagesSubdirectory = "addon-google_apis_x86-google-" + apiLevel + "/images";
            }
            else if (type.startsWith("Google APIs")) {
                this.imagesSubdirectory = "addon-google_apis-google-" + apiLevel + "/images";
            }
            else if (type.startsWith("Google TV")) {
                this.imagesSubdirectory = "addon-google_tv_addon-google-" + apiLevel + "/images";
            }
            else if (type.startsWith("Glass")) {
                throw new AndroidContainerConfigurationException("Glass does not represent a valid Droidium target, no emulator is available: "
                    + name);
            }
            else {
                throw new AndroidContainerConfigurationException("Invalid Android target name: " + name);
            }
        }
        else {
            throw new AndroidContainerConfigurationException("Invalid Android target name: " + name);
        }
    }

    public String getName() {
        return name;
    }

    public boolean isGoogleAddon() {
        return isGoogleAddon;
    }

    public String getImagesSubdirectory() {
        return imagesSubdirectory;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    @Override
    public String toString() {
        return name;
    }

    public static Target findMatchingTarget(ProcessExecutor executor, String androidToolPath, int apiLevel) {
        return findMatchingTarget(executor, androidToolPath, String.valueOf(apiLevel));
    }

    public static Target findMatchingTarget(ProcessExecutor executor, String androidToolPath, String targetLabel)
        throws AndroidContainerConfigurationException {

        Target t = new Target(targetLabel);
        List<Target> availableTargets = getAvailableTargets(executor, androidToolPath);
        if (availableTargets.contains(t)) {
            return t;
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
            try {
                if (target != null && !"".equals(target.trim())) {
                    targets.add(new Target(target.trim()));
                }
            } catch (AndroidContainerConfigurationException e) {
                log.log(Level.FINE, "Unknown target \"{0}\", Droidium will ignore it", target);
            }
        }

        if (targets.size() == 0) {
            throw new AndroidContainerConfigurationException("There are not any available targets found on your system!");
        }

        return targets;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + apiLevel;
        result = prime * result + ((imagesSubdirectory == null) ? 0 : imagesSubdirectory.hashCode());
        result = prime * result + (isGoogleAddon ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Target other = (Target) obj;
        if (apiLevel != other.apiLevel)
            return false;
        if (imagesSubdirectory == null) {
            if (other.imagesSubdirectory != null)
                return false;
        } else if (!imagesSubdirectory.equals(other.imagesSubdirectory))
            return false;
        if (isGoogleAddon != other.isGoogleAddon)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
