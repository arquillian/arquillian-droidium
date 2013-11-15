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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        X86 {
            @Override
            public String toString() {
                return "x86";
            }
        },
        ARMEABIV7A {
            @Override
            public String toString() {
                return "armeabi-v7a";
            }
        },
        MIPS {
            @Override
            public String toString() {
                return "mips";
            }
        };

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

    private final File sdkPath;
    private final File javaPath;
    private final Platform platform;

    private List<Platform> availablePlatforms;

    /**
     *
     * @param configuration
     * @throws AndroidContainerConfigurationException
     */
    public AndroidSDK(AndroidContainerConfiguration configuration) throws AndroidContainerConfigurationException {

        Validate.notNull(configuration, "AndroidSdk configuration must be provided");
        Validate.isReadableDirectory(configuration.getHome(), "Unable to read Android SDK from directory "
            + configuration.getHome());
        Validate.isReadableDirectory(configuration.getJavaHome(), "Unable to determine JAVA_HOME");

        this.sdkPath = new File(configuration.getHome());
        this.javaPath = new File(configuration.getJavaHome());
        availablePlatforms = findAvailablePlatforms();

        if (availablePlatforms.size() == 0) {
            throw new AndroidContainerConfigurationException("There are not any available platforms found on your system!");
        }

        Platform foundPlatform = null;

        if (configuration.getApiLevel() == null) {
            foundPlatform = availablePlatforms.get(availablePlatforms.size() - 1); // the latest one
            configuration.setApiLevel(foundPlatform.apiLevel);
        } else {
            foundPlatform = findPlatformByApiLevel(configuration.getApiLevel());
        }

        if (foundPlatform == null) {
            logger.log(Level.INFO, "API level {0} you specified in configuration via 'apiLevel' property "
                + "is not present on your system. In such case, Droidium tries to find the highest API level "
                + "available and sets it as the default one. When your emulator of some AVD name is not present "
                + "in the system, Droidium will create it dynamically and this API level will be used when emulator "
                + "will be created. All available platforms are: {1}",
                new Object[] { configuration.getApiLevel(), getAllPlatforms() });
            foundPlatform = availablePlatforms.get(availablePlatforms.size() - 1);
            configuration.setApiLevel(foundPlatform.apiLevel);
        }

        if (foundPlatform.systemImages.size() == 0) {
            logger.log(Level.INFO, "There are not any system images found for your API level. You can use Droidium "
                + "only with physical devices connected until you specify such API level which has system images "
                + "available to use. Your current API level is: {0}", new Object[] { configuration.getApiLevel() } );
        } else {
            if (configuration.getAbi() == null) {
                configuration.setAbi(foundPlatform.systemImages.get(0));
            } else {
                if (!foundPlatform.hasSystemImage(configuration.getAbi())) {
                    logger.log(Level.INFO, "ABI you want to use ({1}), is not present in the system for API level {0}. "
                        + "Droidium uses whatever comes first among {2} and it is available for your API level." ,
                        new Object[] { configuration.getApiLevel(), configuration.getAbi(), SystemImage.getAll() });
                    configuration.setAbi(foundPlatform.systemImages.get(0));
                }
            }
        }

        platform = foundPlatform;
        this.configuration = configuration;
    }

    private String getAllPlatforms() {
        StringBuilder sb = new StringBuilder();
        for (Platform p : availablePlatforms) {
            sb.append("API level: ").append(p.apiLevel).append("(").append(p.name).append("), ");
        }
        if (sb.length() > 0) {
            sb.delete(sb.lastIndexOf(","), sb.length());
        }

        return sb.toString();
    }

    public AndroidContainerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AndroidContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    private Platform findPlatformByApiLevel(String apiLevel) {
        for (Platform p : availablePlatforms) {
            if (p.apiLevel.equals(apiLevel)) {
                return p;
            }
        }
        return null;
    }

    public enum Layout {
        LAYOUT_1_5, LAYOUT_2_3
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

        throw new AndroidContainerConfigurationException("Android SDK could not be identified from path \"" + sdkPath
            + "\". ");
    }

    public String getPathForJavaTool(String tool) {
        String[] possiblePaths = { javaPath + "/bin/" + tool, javaPath + "/bin/" + tool + ".exe" };

        for (String possiblePath : possiblePaths) {
            File file = new File(possiblePath);
            if (file.exists() && !file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }

        throw new RuntimeException("Could not find tool '" + tool + "'.Please ensure you've set JAVA_HOME environment " +
            "property properly and that it points to your Java installation directory.");
    }

    /**
     * Returns the complete path for a tool, based on this SDK.
     *
     * @param tool which tool, for example <code>adb</code> or <code>dx.jar</code>.
     * @return the complete path as a <code>String</code>, including the tool's filename.
     */
    public String getPathForTool(String tool) {

        String[] possiblePaths = { sdkPath + "/" + PLATFORMS_FOLDER_NAME + "/" + tool,
            sdkPath + "/" + PLATFORMS_FOLDER_NAME + "/" + tool + ".exe",
            sdkPath + "/" + PLATFORMS_FOLDER_NAME + "/" + tool + ".bat",
            sdkPath + "/" + PLATFORMS_FOLDER_NAME + "/lib/" + tool, getPlatform() + "/tools/" + tool,
            getPlatform() + "/tools/" + tool + ".exe", getPlatform() + "/tools/" + tool + ".bat",
            getPlatform() + "/tools/lib/" + tool, sdkPath + "/tools/" + tool, sdkPath + "/tools/" + tool + ".exe",
            sdkPath + "/tools/" + tool + ".bat", sdkPath + "/tools/lib/" + tool,
            sdkPath + "/" + PLATFORM_TOOLS_FOLDER_NAME + "/" + tool };

        for (String possiblePath : possiblePaths) {
            File file = new File(possiblePath);
            if (file.exists() && !file.isDirectory()) {
                return file.getAbsolutePath();
            }
        }

        throw new RuntimeException("Could not find tool '" + tool
            + "'. Please ensure you've set it properly in Arquillian configuration");
    }

    private String getBuildTool(String tool) {

        // look only into android-sdks/platforms/android-{number}/tools/aapt

        File possiblePlatformPath =
            new File(sdkPath + "/" + PLATFORMS_FOLDER_NAME + getPlatform() + "/tools/" + tool);

        if (possiblePlatformPath.exists() && !possiblePlatformPath.isDirectory()) {
            return possiblePlatformPath.getAbsolutePath();
        }

        // go into android-sdks/build-tools/

        File possibleBuildPath = new File(sdkPath + "/" + BUILD_TOOLS_FOLDER_NAME);

        File[] dirs = possibleBuildPath.listFiles();
        Arrays.sort(dirs);

        for (File dir : dirs) {
            File tmpTool = new File(dir, tool);
            if (tmpTool.exists() && !tmpTool.isDirectory())
                return tmpTool.getAbsolutePath();
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
        Validate.isReadableDirectory(platformsDirectory,
            "Unable to read Android SDK Platforms directory from directory " + platformsDirectory);

        if (platform == null) {
            final File[] platformDirectories = platformsDirectory.listFiles();
            Arrays.sort(platformDirectories);
            return platformDirectories[platformDirectories.length - 1];
        } else {
            final File platformDirectory = new File(platform.path);
            Validate.isReadableDirectory(platformsDirectory,
                "Unable to read Android SDK Platforms directory from directory " + platformsDirectory);
            return platformDirectory;
        }
    }

    /**
     * Initialize the maps matching platform and api levels form the source properties files.
     *
     * @throws AndroidConfigurationException
     *
     */
    private List<Platform> findAvailablePlatforms() throws AndroidContainerConfigurationException {
        List<Platform> availablePlatforms = new ArrayList<Platform>();

        List<File> platformDirectories = getPlatformDirectories();
        for (File pDir : platformDirectories) {
            File propFile = new File(pDir, SOURCE_PROPERTIES_FILENAME);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(propFile));
            } catch (IOException e) {
                throw new AndroidContainerConfigurationException(
                    "Unable to read platform directory details from its configuration file "
                        + propFile.getAbsoluteFile());
            }
            if (properties.containsKey(PLATFORM_VERSION_PROPERTY) && properties.containsKey(API_LEVEL_PROPERTY)) {
                String platform = properties.getProperty(PLATFORM_VERSION_PROPERTY);
                String apiLevel = properties.getProperty(API_LEVEL_PROPERTY);
                List<String> systemImages = getSystemImages("android-" + apiLevel);

                Platform p = new Platform(platform, apiLevel, pDir.getAbsolutePath(), systemImages);
                availablePlatforms.add(p);
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Found available platform: " + p.toString());
                }
            }
        }

        Collections.sort(availablePlatforms);
        return availablePlatforms;
    }

    /**
     * Gets the source properties files from all locally installed platforms.
     *
     * @return list of platform directories
     */
    private List<File> getPlatformDirectories() {
        List<File> sourcePropertyFiles = new ArrayList<File>();

        final File platformsDirectory = new File(sdkPath, PLATFORMS_FOLDER_NAME);
        Validate.isReadableDirectory(platformsDirectory,
            "Unable to read Android SDK Platforms directory from directory " + platformsDirectory);

        final File[] platformDirectories = platformsDirectory.listFiles();
        for (File file : platformDirectories) {
            // only looking in android- folder so only works on reasonably new
            // sdk revisions..
            if (file.isDirectory() && file.getName().startsWith("android-")) {
                sourcePropertyFiles.add(file);
            }
        }
        return sourcePropertyFiles;
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

}
