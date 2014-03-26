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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(AndroidSDK.class.getName());

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

    private AndroidContainerConfiguration configuration;

    private Platform currentPlatform;
    private final File sdkPath;
    private final File javaPath;

    /**
     *
     * @param configuration
     * @throws AndroidContainerConfigurationException
     */
    public AndroidSDK(AndroidContainerConfiguration configuration, ProcessExecutor executor)
        throws AndroidContainerConfigurationException {

        Validate.notNull(configuration, "AndroidSDK configuration must be provided");
        Validate.notNull(executor, "Process executor for AndroidSDK can no be null object.");
        Validate.isReadableDirectory(configuration.getAndroidHome(), "Unable to read Android SDK from directory "
            + configuration.getAndroidHome());
        Validate.isReadableDirectory(configuration.getJavaHome(), "Unable to determine JAVA_HOME");

        this.configuration = configuration;
        this.sdkPath = new File(configuration.getAndroidHome());
        this.javaPath = new File(configuration.getJavaHome());

        // get latest available platform by default
        this.currentPlatform = Platform.getAvailablePlatforms(sdkPath).iterator().next();

        // if serialID is not defined, let's try figure out by target
        if (configuration.getSerialId() == null) {

            Target currentTarget = null;

            // check whether there was target defined in configuration
            String targetId = configuration.getTarget();
            if (targetId != null && !"".equals(targetId)) {
                currentPlatform = Platform.findPlatformByTarget(sdkPath, currentTarget);
                currentTarget = Target.findMatchingTarget(executor, getAndroidPath(), targetId);
                // update runtime configuration
                configuration.setTarget(currentTarget.getFullName());
            }
            // get target based on latest platform
            else {
                currentTarget = Target.findMatchingTarget(executor,
                    getAndroidPath(),
                    "android-" + currentPlatform.getApiLevel());
                // update runtime configuration
                configuration.setTarget(currentTarget.getFullName());
            }

            // we have select target and platform, lets try to get system image
            String abiID = configuration.getAbi();
            if (abiID != null && !"".equals(abiID)) {
                // platform has ABI defined by user in configuration
                if (currentPlatform.hasSystemImage(abiID)) {
                    if (!abiID.equals(currentTarget.getAbi())) {
                        logger.log(Level.WARNING, "ABI property from configuration ({0}) does not match parsed abi from "
                            + "parsed target ({1}). ABI of target will be the same as ABI from configuration",
                            new Object[] { abiID, currentTarget.getAbi() });
                    }
                }
                // platform has ABI defined by target
                else if (currentPlatform.hasSystemImage(currentTarget.getAbi())) {
                    logger.log(Level.WARNING, "There is not ABI of name {0}, specified in configuration. Setting ABI to {1}.",
                        new Object[] { abiID, currentTarget.getAbi() });
                    configuration.setAbi(currentTarget.getAbi());
                }
                // there is no ABI we can use
                else {
                    throw new AndroidContainerConfigurationException("Selected platform does not have system images for "
                        + configuration.getAbi() + " nor " + currentTarget.getAbi());
                }
            }
            // ABI was not selected, let's try to guess it by target
            else {
                if (currentPlatform.hasSystemImage(currentTarget.getAbi())) {
                    configuration.setAbi(currentTarget.getAbi());
                } else {
                    logger.log(Level.WARNING, "ABI property in configuration was not specified and parsed ABI from target "
                        + "({0}) property is not present for specified platform.", new Object[] { currentTarget.getAbi() });
                }
            }
        }

        System.out.println("Droidium runtime configuration: ");
        System.out.println(configuration.toString());
    }

    public AndroidContainerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AndroidContainerConfiguration configuration) {
        this.configuration = configuration;
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
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}", getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.exe", getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/{1}.bat", getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", PLATFORMS_FOLDER_NAME, tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}", getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.exe", getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/tools/lib/{1}.bat", getPlatformName(), tool)),
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
     * Returns directory for current platform
     * @return
     */
    public File getPlatformDirectory() {
        return currentPlatform.getPath();
    }

    private String getBuildTool(String tool) {

        // look only into android-sdks/platforms/android-{number}/tools/aapt
        File[] possiblePlatformPaths = {
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}", PLATFORMS_FOLDER_NAME, getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.exe", PLATFORMS_FOLDER_NAME, getPlatformName(), tool)),
            new File(sdkPath, MessageFormat.format("{0}/{1}/tools/{2}.bat", PLATFORMS_FOLDER_NAME, getPlatformName(), tool)) };

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

    private String getPlatformName() {
        return currentPlatform.getPath().getName();
    }
}