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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstraction of a platform in Android SDK
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
class Platform {

    private static final Logger log = Logger.getLogger(Platform.class.getName());

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

    private final String name;
    private final String apiLevel;
    private final File path;
    private final List<SystemImage> systemImages;

    public Platform(String name, String apiLevel, File path, List<SystemImage> systemImages) {
        this.name = name;
        this.apiLevel = apiLevel;
        this.path = path;
        this.systemImages = systemImages;
    }

    public String getName() {
        return name;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public File getPath() {
        return path;
    }

    public boolean hasSystemImage(String systemImageName) {

        try {
            SystemImage img = SystemImage.valueOf(systemImageName);
            return systemImages.contains(img);
        } catch (Exception e) {
            return false;
        }

    }

    public static Platform findPlatformByTarget(File sdkPath, Target target) {

        for (Platform platform : getAvailablePlatforms(sdkPath)) {
            if (platform.getApiLevel().equals(target.getApiLevel())) {
                return platform;
            }
        }

        throw new AndroidContainerConfigurationException(
            String.format("Platform you are trying to find for target '%s' is unknown.", target));
    }

    /**
     * Initialize the maps matching platform and api levels form the source properties files.
     *
     * @param sdkPath Path to Android SDK
     * @return
     * @throws AndroidContainerConfigurationException
     */
    public static List<Platform> getAvailablePlatforms(File sdkPath) throws AndroidContainerConfigurationException {
        List<Platform> platforms = new ArrayList<Platform>();

        List<File> platformDirectories = getPlatformDirectories(sdkPath);
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
                Platform p = new Platform(platform, apiLevel, pDir, SystemImage.getSystemImagesForPlatform(sdkPath, apiLevel));
                platforms.add(p);
                log.log(Level.FINE, "Found available platform {0}", p);

            }
        }

        // sort platforms according to apiLevel value, the latest is the best
        Collections.sort(platforms, new Comparator<Platform>() {
            @Override
            public int compare(Platform o1, Platform o2) {

                // try to do a numeric comparison
                try {
                    Integer current = Integer.parseInt(o1.apiLevel);
                    Integer other = Integer.parseInt(o2.apiLevel);
                    return other.compareTo(current);
                } catch (NumberFormatException e) {
                    log.log(Level.INFO,
                        "Unable to compare platforms taking their api level as Integers, comparison as Strings follows");
                }

                // failed, try to compare as strings
                return o2.apiLevel.compareTo(o1.apiLevel);
            }
        });

        if (platforms.size() == 0) {
            throw new AndroidContainerConfigurationException("There are not any available platforms found on your system!");
        }

        return platforms;
    }

    /**
     * Gets the source properties files from all locally installed platforms.
     *
     * @return list of platform directories
     */
    private static List<File> getPlatformDirectories(File sdkPath) {
        List<File> foundPlatformDirs = new ArrayList<File>();

        final File platformsDirectory = new File(sdkPath, AndroidSDK.PLATFORMS_FOLDER_NAME);
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