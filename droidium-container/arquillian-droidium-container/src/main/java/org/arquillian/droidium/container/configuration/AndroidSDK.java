/*
 * JBoss, Home of Professional Open Source
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.arquillian.droidium.container.configuration.target.TargetParser;
import org.arquillian.droidium.container.configuration.target.TargetPicker;
import org.arquillian.droidium.container.configuration.target.TargetRegistry;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;

/**
 * Represents an Android SDK.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public final class AndroidSDK {

    /**
     * folder name for the SDK sub folder that contains the different platform versions
     */
    public static final String PLATFORMS_FOLDER_NAME = "platforms";

    /**
     * folder name for the SDK sub folder that contains the platform tools
     */
    public static final String PLATFORM_TOOLS_FOLDER_NAME = "platform-tools";

    /**
     * folder name of the SDK sub folder that contains build tools
     */
    private static final String BUILD_TOOLS_FOLDER_NAME = "build-tools";

    /**
     * folder name of system images in SDK
     */
    public static final String SYSTEM_IMAGES_FOLDER_NAME = "system-images";

    /**
     * folder name of add-ons in SDK
     */
    public static final String ADD_ONS_FOLDER_NAME = "add-ons";

    private final TargetRegistry targetRegistry;

    private final DroidiumPlatformConfiguration platformConfiguration;

    private AndroidContainerConfiguration containerConfiguration;

    private Platform currentPlatform;

    public AndroidSDK(DroidiumPlatformConfiguration platformConfiguration) {
        Validate.notNull(platformConfiguration, "Droidium platform configuration can not be a null object!");

        this.platformConfiguration = platformConfiguration;

        List<Platform> platforms = Platform.getAvailablePlatforms(platformConfiguration.getAndroidHome());

        if (platforms.size() > 0) {
            currentPlatform = platforms.iterator().next();
        }

        targetRegistry = new TargetRegistry();
    }

    public void setupWith(AndroidContainerConfiguration containerConfiguration) {
        Validate.notNull(containerConfiguration, "Container configuration must be provided");
        this.containerConfiguration = containerConfiguration;

        if (this.containerConfiguration.getSerialId() != null) {
            return;
        }

        initializeTargetRegistry();
        new TargetPicker(targetRegistry, getAndroidContainerConfiguration()).pick();

        System.out.println("Droidium runtime configuration: ");
        System.out.println(getAndroidContainerConfiguration().toString());
    }

    private void initializeTargetRegistry() {
        if (targetRegistry.getTargets().size() == 0) {
            targetRegistry.addTargets(new TargetParser(this, getPlatformConfiguration()).parse());
        }
    }

    /**
     *
     * @return current container configuration
     * @throws IllegalStateException if you have not called {@link AndroidSDK#setupWith(AndroidContainerConfiguration)} yet.
     */
    public AndroidContainerConfiguration getAndroidContainerConfiguration() {
        if (containerConfiguration == null) {
            throw new IllegalStateException("You have not called AndroidSDK.setupWith(AndroidContainerConfiguration) method");
        }
        return containerConfiguration;
    }

    public DroidiumPlatformConfiguration getPlatformConfiguration() {
        return platformConfiguration;
    }

    public String getPathForJavaTool(String tool) {

        File javaPath = new File(platformConfiguration.getJavaHome());

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

        File sdkPath = new File(getPlatformConfiguration().getAndroidHome());

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
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}", PLATFORM_TOOLS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.exe", PLATFORM_TOOLS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}.bat", PLATFORM_TOOLS_FOLDER_NAME, tool)),
        };

        List<File> paths = new ArrayList<File>(Arrays.asList(possiblePaths));

        File platformDir = getPlatformDirectory();

        String platformDirName = null;

        if (platformDir != null) {
            platformDirName = platformDir.getName();
        }

        if (platformDirName != null) {

            File[] possiblePlatformDirPaths = {
                new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/tools/{1}", platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/tools/{1}.exe", platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/tools/{1}.bat", platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", platformDirName, tool)) };

            paths.addAll(Arrays.asList(possiblePlatformDirPaths));
        }

        for (File candidate : paths) {
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
        return new File(currentPlatform.getPath(), "framework.aidl").getAbsolutePath();
    }

    /**
     *
     * @return directory for current platform or null if there is not any platform
     */
    public File getPlatformDirectory() {
        if (currentPlatform != null) {
            return currentPlatform.getPath();
        }

        return null;
    }

    private String getBuildTool(String tool) {

        File sdkPath = new File(getPlatformConfiguration().getAndroidHome());

        File platformDirectory = getPlatformDirectory();

        String platformDirName = null;

        if (platformDirectory != null) {
            platformDirName = getPlatformDirectory().getName();
        }

        if (platformDirName != null) {
            // look only into android-sdks/platforms/android-{number}/tools/aapt
            File[] possiblePlatformPaths = {
                new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}", PLATFORMS_FOLDER_NAME, platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.exe", PLATFORMS_FOLDER_NAME, platformDirName, tool)),
                new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.bat", PLATFORMS_FOLDER_NAME, platformDirName, tool)) };

            for (File candidate : possiblePlatformPaths) {
                if (candidate.exists() && candidate.isFile() && candidate.canExecute()) {
                    return candidate.getAbsolutePath();
                }
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

}