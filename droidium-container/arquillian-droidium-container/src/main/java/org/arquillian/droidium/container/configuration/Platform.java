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

import org.arquillian.droidium.container.configuration.target.Target;

/**
 * Abstraction of a platform in Android SDK.
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
    private final int apiLevel;
    private final File path;

    private Platform(String name, int apiLevel, File path) {
        this.name = name;
        this.path = path;
        this.apiLevel = apiLevel;
    }

    public String getName() {
        return name;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public File getPath() {
        return path;
    }

    /**
     *
     * @param sdkPath
     * @param target
     * @return
     */
    public static Platform findPlatformByTarget(File sdkPath, Target target) {

        for (Platform platform : getAvailablePlatforms(sdkPath)) {
            if (platform.getApiLevel() == target.getApiLevel()) {
                return platform;
            }
        }

        return null;
    }

    public static List<Platform> getAvailablePlatforms(String sdkPath) {
        return getAvailablePlatforms(new File(sdkPath));
    }

    /**
     * Initialize the maps matching platform and api levels from the source properties files.
     *
     * @param sdkPath Path to Android SDK
     * @return
     * @throws AndroidContainerConfigurationException
     */
    public static List<Platform> getAvailablePlatforms(File sdkPath) throws AndroidContainerConfigurationException {
        List<Platform> platforms = new ArrayList<Platform>();

        for (File platformDir : getPlatformDirs(sdkPath)) {
            Properties properties = loadProperties(new File(platformDir, SOURCE_PROPERTIES_FILENAME));

            if (properties.containsKey(PLATFORM_VERSION_PROPERTY) && properties.containsKey(API_LEVEL_PROPERTY)) {
                String platformVersion = properties.getProperty(PLATFORM_VERSION_PROPERTY);
                String apiLevel = properties.getProperty(API_LEVEL_PROPERTY);
                Platform platform = new Platform(platformVersion, Integer.parseInt(apiLevel), platformDir);
                platforms.add(platform);
                log.log(Level.FINE, "Found available platform {0}", platform);
            }
        }

        // sort platforms according to apiLevel value, the latest is the best
        Collections.sort(platforms, new Comparator<Platform>() {
            @Override
            public int compare(Platform o1, Platform o2) {
                Integer current = Integer.valueOf(o1.apiLevel);
                Integer other = Integer.valueOf(o2.apiLevel);
                return other.compareTo(current);
            }
        });

        return platforms;
    }

    private static Properties loadProperties(File propertyFile) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new IllegalStateException(
                "Unable to read platform directory details from its configuration file " + propertyFile.getAbsoluteFile());
        }

        return properties;
    }

    private static List<File> getPlatformDirs(File sdkPath) {
        List<File> foundPlatformDirs = new ArrayList<File>();

        final File platformDirs = new File(sdkPath, AndroidSDK.PLATFORMS_FOLDER_NAME);
        if (!Validate.isReadableDirectory(platformDirs)) {
            throw new IllegalArgumentException("Unable to read Android SDK Platforms directory from directory "
                + platformDirs);
        }

        for (File platformDir : platformDirs.listFiles()) {
            if (platformDir.isDirectory() && platformDir.canRead() && platformDir.getName().startsWith("android-")) {
                foundPlatformDirs.add(platformDir);
            }
        }
        return foundPlatformDirs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Platform: ");
        sb.append(name).append("/API level ").append(apiLevel).append(" at ").append(path);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + apiLevel;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        Platform other = (Platform) obj;
        if (apiLevel != other.apiLevel)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
        return true;
    }

}