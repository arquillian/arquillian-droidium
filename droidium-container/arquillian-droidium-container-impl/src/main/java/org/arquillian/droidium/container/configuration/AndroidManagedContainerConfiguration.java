/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * Configuration for Android container.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidManagedContainerConfiguration implements ContainerConfiguration {

    private static final Logger logger = Logger.getLogger(AndroidManagedContainerConfiguration.class.getName());

    private boolean skip;

    private boolean forceNewBridge = true;

    private String serialId;

    private String avdName;

    private String generatedAvdPath = System.getProperty("java.io.tmpdir") + System.getProperty("file.separator");

    private String emulatorOptions;

    private String sdSize = "128M";

    private String sdCard;

    private String sdCardLabel;

    private boolean generateSDCard;

    private String abi;

    private long emulatorBootupTimeoutInSeconds = 120L;

    private long emulatorShutdownTimeoutInSeconds = 60L;

    private String home = System.getenv("ANDROID_HOME");

    private String javaHome = System.getenv("JAVA_HOME");

    private boolean avdGenerated;

    private String consolePort;

    private String adbPort;

    private int droneHostPort = 14444;

    private int droneGuestPort = 8080;

    // Android 2.3.3 is the default
    private String apiLevel = "10";

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getAvdName() {
        return avdName;
    }

    public void setAvdName(String avdName) {
        this.avdName = avdName;
    }

    public void setGeneratedAvdPath(String generatedAvdPath) {
        this.generatedAvdPath = generatedAvdPath;
    }

    public String getGeneratedAvdPath() {
        return this.generatedAvdPath;
    }

    public String getSerialId() {
        return serialId;
    }

    public void setSerialId(String serialId) {
        this.serialId = serialId;
    }

    public String getEmulatorOptions() {
        return emulatorOptions;
    }

    public void setEmulatorOptions(String emulatorOptions) {
        this.emulatorOptions = emulatorOptions;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isForceNewBridge() {
        return forceNewBridge;
    }

    public void setForceNewBridge(boolean force) {
        this.forceNewBridge = force;
    }

    public long getEmulatorBootupTimeoutInSeconds() {
        return emulatorBootupTimeoutInSeconds;
    }

    public void setEmulatorBootupTimeoutInSeconds(long emulatorBootupTimeoutInSeconds) {
        this.emulatorBootupTimeoutInSeconds = emulatorBootupTimeoutInSeconds;
    }

    public String getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(String apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getSdSize() {
        return sdSize;
    }

    public void setSdSize(String sdSize) {
        this.sdSize = sdSize;
    }

    public String getSdCard() {
        return sdCard;
    }

    public String getSdCardLabel() {
        return sdCardLabel;
    }

    public void setSdCardLabel(String sdCardLabel) {
        this.sdCardLabel = sdCardLabel;
    }

    public boolean getGenerateSDCard() {
        return this.generateSDCard;
    }

    public void setGenerateSDCard(boolean generate) {
        this.generateSDCard = generate;
    }

    public void setSdCard(String sdCard) {
        this.sdCard = sdCard;
    }

    public long getEmulatorShutdownTimeoutInSeconds() {
        return emulatorShutdownTimeoutInSeconds;
    }

    public void setEmulatorShutdownTimeoutInSeconds(long emulatorShutdownTimeoutInSeconds) {
        this.emulatorShutdownTimeoutInSeconds = emulatorShutdownTimeoutInSeconds;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public boolean isAVDGenerated() {
        return avdGenerated;
    }

    public void setAvdGenerated(boolean generated) {
        this.avdGenerated = generated;
    }

    public boolean getAvdGenerated() {
        return this.avdGenerated;
    }

    public String getConsolePort() {
        return consolePort;
    }

    public void setConsolePort(String consolePort) {
        this.consolePort = consolePort;
    }

    public String getAdbPort() {
        return adbPort;
    }

    public void setAdbPort(String adbPort) {
        this.adbPort = adbPort;
    }

    public int getDroneHostPort() {
        return droneHostPort;
    }

    public int getDroneGuestPort() {
        return droneGuestPort;
    }

    public void setDroneHostPort(int droneHostPort) {
        this.droneHostPort = droneHostPort;
    }

    public void setDroneGuestPort(int droneGuestPort) {
        this.droneGuestPort = droneGuestPort;
    }

    @Override
    public void validate() throws AndroidContainerConfigurationException {
        Validate.isReadableDirectory(home,
            "You must provide Android SDK home directory. The value you've provided is not valid ("
                + (home == null ? "" : home)
                + "). You can either set it via an environment variable ANDROID_HOME or via"
                + " a property called \"home\" in Arquillian configuration.");

        if (avdName != null && serialId != null) {
            logger.warning("Both \"avdName\" and \"serialId\" properties are defined, the device "
                + "specified by \"serialId\" will get priority if connected.");
        }

        if (avdName == null && serialId == null && consolePort == null) {
            logger.severe("All \"avdName\", \"serialId\" and \"consolePort\" are not defined.");
            throw new AndroidContainerConfigurationException(
                "All \"avdName\", \"serialId\" and \"consolePort\" are not defined.");
        }

        if (generatedAvdPath != null) {
            Validate.notNullOrEmpty(generatedAvdPath, "Directory you specified to store AVD to is empty string or null.");
            if (!generatedAvdPath.endsWith(System.getProperty("file.separator"))) {
                generatedAvdPath += System.getProperty("file.separator");
            }

            File f = new File(generatedAvdPath);
            if (!f.exists()) {
                try {
                    if (!f.mkdirs()) {
                        throw new AndroidContainerConfigurationException("Unable to create directory where AVD will be stored.");
                    }
                } catch (Exception e) {
                    throw new AndroidContainerConfigurationException("Unable to create directory where AVD will be stored " +
                        "(" + generatedAvdPath + "). Check that you have permission to create directory you specified.");
                }
            }
            Validate.isReadableDirectory(new File(generatedAvdPath), "Directory you specified as place where newly " +
                "generated AVD will be placed does not exist (" + generatedAvdPath + ").");
            Validate.isWritable(new File(generatedAvdPath), "Path you want to store generated AVD is not writable!");
        }

        if (consolePort != null) {
            Validate.isConsolePortValid(consolePort);
        }

        if (adbPort != null) {
            Validate.isAdbPortValid(adbPort);
        }

        if (sdCard != null) {
            Validate.sdCardFileName(sdCard, "File name (or path) of SD card to use '" + sdCard
                + "' is not valid. Check it is under existing and writable directory does have '.img' suffix.");
        }

        if (sdCardLabel != null) {
            Validate.notNullOrEmpty(sdCardLabel, "SD card label can not be the empty string");
        }

        if (sdSize != null) {
            Validate.sdSize(sdSize, "Check you did specify your sdSize property in arquillian.xml properly.");
        }

        if (droneHostPort != 14444) {
            Validate.isPortValid(droneHostPort);
        }

        if (droneGuestPort != 8080) {
            Validate.isPortValid(droneGuestPort);
        }

        if (emulatorBootupTimeoutInSeconds <= 0) {
            throw new AndroidContainerConfigurationException(
                "Emulator bootup timeout has to be bigger then 0.");
        }

        if (emulatorShutdownTimeoutInSeconds <= 0) {
            throw new AndroidContainerConfigurationException(
                "Emulator shutdown timeout has to be bigger then 0.");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\navdName\t\t\t:").append(this.avdName).append("\n");
        sb.append("generatedAvdPath\t:").append(this.generatedAvdPath).append("\n");
        sb.append("apiLevel\t\t:").append(this.apiLevel).append("\n");
        sb.append("serialId\t\t:").append(this.serialId).append("\n");
        sb.append("force\t\t\t:").append(this.forceNewBridge).append("\n");
        sb.append("skip\t\t\t:").append(this.skip).append("\n");
        sb.append("sdCard\t\t\t:").append(this.sdCard).append("\n");
        sb.append("sdSize\t\t\t:").append(this.sdSize).append("\n");
        sb.append("generateSD\t\t:").append(this.generateSDCard).append("\n");
        sb.append("abi\t\t\t:").append(this.abi).append("\n");
        sb.append("emuBoot\t\t\t:").append(this.emulatorBootupTimeoutInSeconds).append("\n");
        sb.append("emuShut\t\t\t:").append(this.emulatorShutdownTimeoutInSeconds).append("\n");
        sb.append("emuOpts\t\t\t:").append(this.emulatorOptions).append("\n");
        sb.append("home\t\t\t:").append(this.home).append("\n");
        sb.append("consolePort\t\t:").append(this.consolePort).append("\n");
        sb.append("adbPort\t\t\t:").append(this.adbPort).append("\n");
        return sb.toString();
    }

}
