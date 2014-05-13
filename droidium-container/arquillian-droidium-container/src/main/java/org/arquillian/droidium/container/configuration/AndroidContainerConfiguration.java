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

import java.util.UUID;
import java.util.logging.Logger;

import org.arquillian.droidium.container.log.LogLevel;
import org.arquillian.droidium.container.log.LogType;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * Configuration for Android container.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidContainerConfiguration implements ContainerConfiguration {

    private String fileSeparator = System.getProperty("file.separator");

    private static final Logger logger = Logger.getLogger(AndroidContainerConfiguration.class.getName());

    private String serialId;

    private String avdName;

    private String emulatorOptions;

    private String sdSize = "128M";

    private String sdCard;

    private String sdCardLabel;

    private boolean generateSDCard;

    private String abi;

    private long emulatorBootupTimeoutInSeconds = 600L;

    private long emulatorShutdownTimeoutInSeconds = 60L;

    private boolean avdGenerated;

    private String consolePort;

    private String adbPort;

    private int droneHostPort = 14444;

    private int droneGuestPort = 8080;

    private String logLevel = LogLevel.DEFAULT;

    private String logType = LogType.DEFAULT;

    private String logFilePath = "target" + fileSeparator + "logcat.log";

    private String logPackageWhitelist;

    private String logPackageBlacklist;

    // useful when more containers are being used, also affects log filename!
    private boolean logSerialId;

    private String target;

    public String getAvdName() {
        return avdName;
    }

    public void setAvdName(String avdName) {
        this.avdName = avdName;
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

    public long getEmulatorBootupTimeoutInSeconds() {
        return emulatorBootupTimeoutInSeconds;
    }

    public void setEmulatorBootupTimeoutInSeconds(long emulatorBootupTimeoutInSeconds) {
        this.emulatorBootupTimeoutInSeconds = emulatorBootupTimeoutInSeconds;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
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

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getLogPackageWhitelist() {
        return logPackageWhitelist;
    }

    public void setLogPackageWhitelist(String logPackageWhitelist) {
        this.logPackageWhitelist = logPackageWhitelist;
    }

    public String getLogPackageBlacklist() {
        return logPackageBlacklist;
    }

    public void setLogPackageBlacklist(String logPackageBlacklist) {
        this.logPackageBlacklist = logPackageBlacklist;
    }

    public boolean isLogFilteringEnabled() {
        return (logPackageWhitelist != null
            && !logPackageWhitelist.equals("")
            || (logPackageBlacklist != null && !logPackageBlacklist.equals("")));
    }

    public boolean isLogSerialId() {
        return logSerialId;
    }

    public void setLogSerialId(boolean logSerialId) {
        this.logSerialId = logSerialId;
    }

    @Override
    public void validate() throws AndroidContainerConfigurationException {

        if (avdName != null && serialId != null) {
            logger.warning("Both \"avdName\" and \"serialId\" properties are defined, the device "
                + "specified by \"serialId\" will get priority if connected.");
        } else if (avdName == null) {
            avdName = UUID.randomUUID().toString();
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

        if (logPackageWhitelist != null && !logPackageWhitelist.equals("") && logPackageBlacklist == null) {
            logPackageBlacklist = "*";
            logger
                .warning("\"logPackageBlacklist\" isn't defined, but \"logPackageWhitelist\" is. Assuming \"*\" as a value for \"logPackageBlacklist\"!");
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
        sb.append(String.format("%-40s %s\n", "avdName", avdName));
        sb.append(String.format("%-40s %s\n", "serialId", serialId));
        sb.append(String.format("%-40s %s\n", "target", target));
        sb.append(String.format("%-40s %s\n", "abi", abi));
        sb.append(String.format("%-40s %s\n", "consolePort", consolePort));
        sb.append(String.format("%-40s %s\n", "adbPort", adbPort));
        sb.append(String.format("%-40s %s\n", "emulatorBootupTimeoutInSeconds", emulatorBootupTimeoutInSeconds));
        sb.append(String.format("%-40s %s\n", "emulatorShutdownTimeoutInSeconds", emulatorShutdownTimeoutInSeconds));
        sb.append(String.format("%-40s %s\n", "emulatorOptions", emulatorOptions));
        sb.append(String.format("%-40s %s\n", "sdCard", sdCard));
        sb.append(String.format("%-40s %s\n", "sdSize", sdSize));
        sb.append(String.format("%-40s %s\n", "generateSDCard", generateSDCard));
        sb.append(String.format("%-40s %s\n", "logLevel", logLevel));
        sb.append(String.format("%-40s %s\n", "logType", logType));
        sb.append(String.format("%-40s %s\n", "logFilePath", logFilePath));
        sb.append(String.format("%-40s %s\n", "logPackageWhitelist", logPackageWhitelist));
        sb.append(String.format("%-40s %s\n", "logPackageBlacklist", logPackageBlacklist));
        return sb.toString();
    }

}
