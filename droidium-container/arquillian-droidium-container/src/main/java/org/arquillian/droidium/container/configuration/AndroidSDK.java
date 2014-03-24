/*
s * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
 *
 * Copyright (C) 2009, 2010 Jayway AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.arquillian.droidium.container.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecutor;

/**
 * Represents an Android SDK.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * @author hugo.josefson@jayway.com
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class AndroidSDK {

    private AndroidContainerConfiguration configuration;

    private ProcessExecutor executor;

    private static final Logger logger = Logger.getLogger(AndroidSDK.class.getName());

    /**
     * property file in each platform folder with details about platform
     */
    private static final String SOURCE_PROPERTIES_FILENAME = "source.properties";

    /**
     * property name for platform version in sdk source.properties file
     */
    private static final String PLATFORM_VERSION_PROPERTY = "Platform.Version";

    /**
     * property name for API level version in SDK source.properties file
     */
    private static final String API_LEVEL_PROPERTY = "AndroidVersion.ApiLevel";

    /**
     * folder name for the SDK sub folder that contains the different platform versions
     */
    private static final String PLATFORMS_FOLDER_NAME = "platforms";

    /**
     * folder name for the SDK sub folder that contains the platform tools
     */
    private static final String PLATFORM_TOOLS_FOLDER_NAME = "platform-tools";

    /**
     * folder name of the SDK sub folder that contains build tools
     */
    private static final String BUILD_TOOLS_FOLDER_NAME = "build-tools";

    /**
     * folder name of system images in SDK
     */
    private static final String SYSTEM_IMAGES_FOLDER_NAME = "system-images";

    private static final class Platform implements Comparable<Platform> {
        final String name;
        final String apiLevel;
        final String path;
        final List<String> systemImages;

        public Platform(String name, String apiLevel, String path, List<String> systemImages) {
            super();
            this.name = name;
            this.apiLevel = apiLevel;
            this.path = path;
            this.systemImages = systemImages;
        }

        public boolean hasSystemImage(String systemImage) {
            for (String tmp : systemImages) {
                if (tmp.equals(systemImage)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public int compareTo(Platform o) {

            // try to do a numeric comparison
            try {
                Integer current = Integer.parseInt(apiLevel);
                Integer other = Integer.parseInt(o.apiLevel);
                return current.compareTo(other);
            } catch (NumberFormatException e) {
                logger.log(Level.INFO, "Unable to compare platforms taking their api level as Integers, "
                    + "comparison as Strings follows");
            }

            // failed, try to compare as strings
            return apiLevel.compareTo(o.apiLevel);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((apiLevel == null) ? 0 : apiLevel.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((path == null) ? 0 : path.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Platform other = (Platform) obj;
            if (apiLevel == null) {
                if (other.apiLevel != null) {
                    return false;
                }
            } else if (!apiLevel.equals(other.apiLevel)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (path == null) {
                if (other.path != null) {
                    return false;
                }
            } else if (!path.equals(other.path)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Platform: ");
            sb.append(name).append("/API level ").append(apiLevel).append(" at ").append(path);
            return sb.toString();
        }
    }

    /**
     * Enumeration of all (up to November 2013) possible types of system images.
     *
     * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    private enum SystemImage {

        X86("x86"),
        ARMEABIV7A("armeabi-v7a"),
        MIPS("mips");

        private String name;

        private SystemImage(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * @return all system images concatenated to one string separated only by one space from each other
         */
        public static String getAll() {
            StringBuilder sb = new StringBuilder();

            for (SystemImage systemImage : SystemImage.values()) {
                sb.append(systemImage.toString());
                sb.append(" ");
            }

            return sb.toString().trim();
        }

    }

    private static final class Target {

        private final SystemImage[] abis = SystemImage.values();

        private String name;

        private String apiLevel;

        private String fullName;

        private String abi;

        public Target(String name, String apiLevel, String fullName) {
            this.name = name;
            this.apiLevel = apiLevel;
            this.fullName = fullName;
            abi = parseAbi();
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

        public void setAbi(String abi) {
            this.abi = abi;
        }

        @Override
        public String toString() {
            return fullName;
        }

        public boolean matches(String targetLabel) {
            return toString().equals(targetLabel) || getName().equals(targetLabel) || getApiLevel().equals(targetLabel);
        }

        private String parseAbi() {
            // Google Inc.:Google APIs x86:19
            for (SystemImage abi : abis) {
                if (fullName.contains(abi.name)) {
                    return abi.name;
                }
            }

            // Google Inc.:Google APIs:18
            if (fullName.contains("Google Inc.")) {
                return SystemImage.ARMEABIV7A.name;
            }

            // android-x
            return SystemImage.X86.name;
        }

    }

    private List<Target> getTargets() {

        List<String> targetsOutput = executor.execute(
            new CommandBuilder()
                .add(getAndroidPath())
                .add("list")
                .add("target")
                .add("-c")
                .build()).getOutput();

        Collections.sort(targetsOutput);
        Collections.reverse(targetsOutput);

        List<Target> targets = new ArrayList<AndroidSDK.Target>();

        for (String target : targetsOutput) {
            if (!target.trim().isEmpty()) {
                targets.add(parseTarget(target.trim()));
            }
        }

        return targets;
    }

    private Target parseTarget(String target) {
        String name = null;
        String apiLevel = null;

        // form Google Inc.:Google APIs:15
        if (target.contains(":")) {
            name = target.substring(0, target.lastIndexOf(":"));
            apiLevel = target.substring(target.lastIndexOf(":") + 1);
        } else {
            // form android-15
            name = target.substring(0, target.lastIndexOf("-"));
            apiLevel = target.substring(target.lastIndexOf("-") + 1);
        }

        return new Target(name, apiLevel, target);
    }

    private final File sdkPath;
    private final File javaPath;
    private Platform platform;
    private Target target;

    private List<Platform> availablePlatforms;
    private List<Target> availableTargets;

    /**
     *
     * @param configuration
     * @throws AndroidContainerConfigurationException
     */
    public AndroidSDK(AndroidContainerConfiguration configuration, ProcessExecutor executor) throws AndroidContainerConfigurationException {

        Validate.notNull(configuration, "AndroidSDK configuration must be provided");
        Validate.notNull(executor, "Process executor for AndroidSDK can no be null object.");
        Validate.isReadableDirectory(configuration.getAndroidHome(), "Unable to read Android SDK from directory "
            + configuration.getAndroidHome());
        Validate.isReadableDirectory(configuration.getJavaHome(), "Unable to determine JAVA_HOME");

        this.executor = executor;
        this.sdkPath = new File(configuration.getAndroidHome());
        this.javaPath = new File(configuration.getJavaHome());
        availablePlatforms = getPlatforms();
        availableTargets = getTargets();

        if (availableTargets.size() == 0 && configuration.getSerialId() == null) {
            throw new AndroidContainerConfigurationException("There are not any available targets found on your system!");
        }

        if (availablePlatforms.size() == 0 && configuration.getSerialId() == null) {
            throw new AndroidContainerConfigurationException("There are not any available platforms found on your system!");
        }

        if (configuration.getSerialId() == null) {

            Platform platform = null;
            Target target = null;

            if (configuration.getTarget() == null) {
                platform = availablePlatforms.get(availablePlatforms.size() - 1); // the latest one
                String targetLabel = new StringBuilder().append("android-").append(platform.apiLevel).toString();
                target = findMatchingTarget(targetLabel);
                configuration.setTarget(target.fullName);
            } else {
                target = findMatchingTarget(configuration.getTarget());
                platform = findPlatformByTarget(target);
                configuration.setTarget(target.fullName);
            }

            if (platform.systemImages.size() != 0) {
                if (configuration.getAbi() == null) {
                    if (platform.hasSystemImage(target.getAbi())) {
                        configuration.setAbi(target.getAbi());
                    } else {
                        logger.log(Level.INFO, "ABI property in configuration was not specified and parsed ABI from target "
                            + "({0}) property is not present for specified platform", new Object[] { target.getAbi() });
                    }
                } else {
                    if (platform.hasSystemImage(configuration.getAbi())) {
                        if (!target.getAbi().equals(configuration.getAbi())) {
                            logger.log(Level.INFO, "ABI property from configuration ({0}) does not match parsed abi from "
                                + "parsed target ({1}). ABI of target will be the same as ABI from configuration",
                                new Object[] { configuration.getAbi(), target.getAbi() });
                            target.setAbi(configuration.getAbi());
                        }
                    } else if (platform.hasSystemImage(target.getAbi())) {
                        logger.log(Level.WARNING, "There is not ABI of name {0}. Setting ABI to {1}.",
                            new Object[] { configuration.getAbi(), target.getAbi() });
                        configuration.setAbi(target.getAbi());
                    } else {
                        throw new AndroidContainerConfigurationException("Selected platform does not have system images for "
                            + configuration.getAbi() + " nor " + target.getAbi());
                    }
                }
            } else {
                throw new AndroidContainerConfigurationException("There are not any available system images for platform " +
                    platform.toString());
            }

            this.platform = platform;
            this.target = target;
        }

        this.configuration = configuration;
        System.out.println("Droidium runtime configuration:");
        System.out.println(this.configuration.toString());
    }

    public AndroidContainerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AndroidContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    public enum Layout {
        LAYOUT_1_5,
        LAYOUT_2_3
    }

    public Layout getLayout() throws AndroidContainerConfigurationException {

        Validate.isReadableDirectory(sdkPath, "Unable to read Android SDK from directory " + sdkPath);

        final File platformTools = new File(sdkPath, PLATFORM_TOOLS_FOLDER_NAME);
        if (platformTools.exists() && platformTools.isDirectory()) {
            return Layout.LAYOUT_2_3;
        }

        final File platforms = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        if (platforms.exists() && platforms.isDirectory()) {
            return Layout.LAYOUT_1_5;
        }

        throw new AndroidContainerConfigurationException("Android SDK could not be identified from path \"" + sdkPath + "\". ");
    }

    public String getPathForJavaTool(String tool) {

        File[] possiblePaths = { new File(javaPath, MessageFormat.format("bin/{0}", tool)),
            new File(javaPath, MessageFormat.format("bin/{0}.exe", tool)),
            new File(javaPath, MessageFormat.format("../bin/{0}", tool)),
            new File(javaPath, MessageFormat.format("../bin/{0}.exe", tool)), };

        for (File candidate : possiblePaths) {
            if (candidate.exists() && candidate.isFile() && candidate.canExecute()) {
                return candidate.getAbsolutePath();
            }
        }

        // construct error message
        StringBuilder exception = new StringBuilder("Could not find tool '")
            .append(tool)
            .append("'. Please ensure you've set JAVA_HOME environment property properly and that it points to your Java installation directory. ")
            .append("Searching at locations: ");
        String delimiter = "";
        for (File candidate : possiblePaths) {
            exception.append(delimiter).append(candidate.getAbsolutePath());
            delimiter = ", ";
        }

        throw new RuntimeException(exception.toString());
    }

    /**
     * Returns the complete path for a tool, based on this SDK.
     *
     * @param tool which tool, for example <code>adb</code> or <code>dx.jar</code>.
     * @return the complete path as a <code>String</code>, including the tool's filename.
     */
    public String getPathForTool(String tool) {

        File[] possiblePaths = { new File(sdkPath, MessageFormat.format("tools/{0}", tool)),
            new File(sdkPath, MessageFormat.format("tools/{0}.exe", tool)),
            new File(sdkPath, MessageFormat.format("tools/{0}.bat", tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/lib/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/lib/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/lib/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.exe", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.bat", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", getPlatform().getName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}", PLATFORM_TOOLS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.exe", PLATFORM_TOOLS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.bat", PLATFORM_TOOLS_FOLDER_NAME, tool)), };

        for (File candidate : possiblePaths) {
            if (candidate.exists() && candidate.isFile() && candidate.canExecute()) {
                return candidate.getAbsolutePath();
            }
        }

        // construct error message
        StringBuilder exception = new StringBuilder("Could not find tool '")
            .append(tool)
            .append("'. Please ensure you've set ANDROID_HOME environment property or androidHome property in arquillian.xml and this location contains all required packages")
            .append("Searching at locations: ");
        String delimiter = "";
        for (File candidate : possiblePaths) {
            exception.append(delimiter).append(candidate.getAbsolutePath());
            delimiter = ", ";
        }

        throw new RuntimeException(exception.toString());
    }

    private String getBuildTool(String tool) {

        // look only into android-sdks/platforms/android-{number}/tools/aapt
        File[] possiblePlatformPaths = {
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}", PLATFORMS_FOLDER_NAME, getPlatform(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.exe", PLATFORMS_FOLDER_NAME, getPlatform(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.bat", PLATFORMS_FOLDER_NAME, getPlatform(), tool)) };

        for (File candidate : possiblePlatformPaths) {
            if (candidate.exists() && candidate.isFile() && candidate.canExecute()) {
                return candidate.getAbsolutePath();
            }
        }

        // go into android-sdks/build-tools/
        File possibleBuildPath = new File(sdkPath, BUILD_TOOLS_FOLDER_NAME);

        File[] dirs = possibleBuildPath.listFiles();
        Arrays.sort(dirs);

        for (File dir : dirs) {
            for (File candidate : new File[] { new File(dir, tool), new File(dir, tool + ".exe"), new File(dir, tool + ".bat") }) {
                if (candidate.exists() && candidate.isFile() && candidate.canExecute()) {
                    return candidate.getAbsolutePath();
                }
            }
        }

        throw new RuntimeException("Could not find tool '" + tool + ".");
    }

    /**
     * Get the emulator path.
     *
     * @return path to {@code emulator} command
     */
    public String getEmulatorPath() {
        return getPathForTool("emulator");
    }

    public String getMakeSdCardPath() {
        return getPathForTool("mksdcard");
    }

    /**
     * Get the android debug tool path (adb).
     *
     * @return path to {@code adb} command
     */
    public String getAdbPath() {
        return getPathForTool("adb");
    }

    public String getAaptPath() {
        return getBuildTool("aapt");
    }

    /**
     * Get the android tool path
     *
     * @return path to {@code android} command
     */
    public String getAndroidPath() {
        return getPathForTool("android");
    }

    /**
     * Returns the complete path for <code>framework.aidl</code>, based on this SDK.
     *
     * @return the complete path as a <code>String</code>, including the filename.
     * @throws AndroidConfigurationException
     */
    public String getPathForFrameworkAidl() throws AndroidContainerConfigurationException {
        final Layout layout = getLayout();
        switch (layout) {
            case LAYOUT_1_5: // intentional fall-through
            case LAYOUT_2_3:
                return getPlatform() + "/framework.aidl";
            default:
                throw new AndroidContainerConfigurationException("Unsupported layout \"" + layout + "\"!");
        }
    }

    public File getPlatform() {
        Validate.isReadableDirectory(sdkPath, "Unable to read Android SDK from directory " + sdkPath);

        final File platformsDirectory = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        Validate.isReadableDirectory(platformsDirectory, "Unable to read Android SDK Platforms directory from directory "
            + platformsDirectory);

        if (platform == null) {
            final File[] platformDirectories = platformsDirectory.listFiles();
            Arrays.sort(platformDirectories);
            return platformDirectories[platformDirectories.length - 1];
        } else {
            final File platformDirectory = new File(platform.path);
            Validate.isReadableDirectory(platformsDirectory, "Unable to read Android SDK Platforms directory from directory "
                + platformsDirectory);
            return platformDirectory;
        }
    }

    /**
     * Initialize the maps matching platform and api levels form the source properties files.
     *
     * @throws AndroidConfigurationException
     *
     */
    private List<Platform> getPlatforms() throws AndroidContainerConfigurationException {
        List<Platform> platforms = new ArrayList<Platform>();

        List<File> platformDirectories = getPlatformDirectories();
        for (File pDir : platformDirectories) {
            File propFile = new File(pDir, SOURCE_PROPERTIES_FILENAME);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(propFile));
            } catch (IOException e) {
                throw new AndroidContainerConfigurationException(
                    "Unable to read platform directory details from its configuration file " + propFile.getAbsoluteFile());
            }
            if (properties.containsKey(PLATFORM_VERSION_PROPERTY) && properties.containsKey(API_LEVEL_PROPERTY)) {
                String platform = properties.getProperty(PLATFORM_VERSION_PROPERTY);
                String apiLevel = properties.getProperty(API_LEVEL_PROPERTY);
                List<String> systemImages = getSystemImages("android-" + apiLevel);

                Platform p = new Platform(platform, apiLevel, pDir.getAbsolutePath(), systemImages);
                platforms.add(p);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Found available platform: " + p.toString());
                }
            }
        }

        Collections.sort(platforms);
        return platforms;
    }

    /**
     * Gets the source properties files from all locally installed platforms.
     *
     * @return list of platform directories
     */
    private List<File> getPlatformDirectories() {
        List<File> foundPlatformDirs = new ArrayList<File>();

        final File platformsDirectory = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        Validate.isReadableDirectory(platformsDirectory, "Unable to read Android SDK Platforms directory from directory "
            + platformsDirectory);

        final File[] platformDirectories = platformsDirectory.listFiles();
        for (File file : platformDirectories) {
            // only looking in android- folder so only works on reasonably new
            // sdk revisions..
            if (file.isDirectory() && file.getName().startsWith("android-")) {
                foundPlatformDirs.add(file);
            }
        }
        return foundPlatformDirs;
    }

    private List<File> getSystemImageDirectories(String systemImageDirName) {

        List<File> systemImages = new ArrayList<File>();

        File dir = new File(new File(sdkPath, SYSTEM_IMAGES_FOLDER_NAME), systemImageDirName);

        if (!dir.exists()) {
            return systemImages;
        }

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                systemImages.add(file);
            }
        }

        return systemImages;
    }

    private List<String> getSystemImages(String systemImageDirName) {

        List<File> systemImageDirs = getSystemImageDirectories(systemImageDirName);

        List<String> images = new ArrayList<String>();

        for (File dir : systemImageDirs) {
            images.add(dir.getName());
        }

        Collections.sort(images);
        Collections.reverse(images); // to have x86 as the first one

        return images;
    }

    private Target findMatchingTarget(String targetLabel) throws AndroidContainerConfigurationException {
        // target from config can be like "19", "android-19",
        // "Google Inc.:Google APIs:19" or "Google Inc.:Google APIs x86:19"

        for (Target target : availableTargets) {
            if (target.matches(targetLabel)) {
                return target;
            }
        }

        throw new AndroidContainerConfigurationException(String.format("There is not any target with target name '%s'", targetLabel));
    }

    private Platform findPlatformByTarget(Target target) {

        for (Platform platform : availablePlatforms) {
            if (platform.apiLevel.equals(target.apiLevel)) {
                return platform;
            }
        }

        throw new AndroidContainerConfigurationException(
            String.format("Platform you are trying to find for target '%s' is unknown.", target));
    }

}
