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
import java.util.UUID;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.log.LogLevel;
import org.arquillian.droidium.container.log.LogType;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
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

    private boolean forceNewBridge = true;

    private String serialId;

    private String avdName;

    private String generatedAvdPath = resolveTmpDir();

    private String emulatorOptions;

    private String sdSize = "128M";

    private String sdCard;

    private String sdCardLabel;

    private boolean generateSDCard;

    private String abi;

    private long emulatorBootupTimeoutInSeconds = 120L;

    private long emulatorShutdownTimeoutInSeconds = 60L;

    private String androidHome = resolveAndroidHome();

    private String javaHome = resolveJavaHome();

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

    private String keystore = resolveUserHome() + fileSeparator + ".android" + fileSeparator + "debug.keystore";

    private String storepass = "android";

    private String keypass = "android";

    private String alias = "androiddebugkey";

    private String sigalg = "SHA1withRSA";

    private String keyalg = "RSA";

    private boolean removeTmpDir = true;

    private String tmpDir = System.getProperty("java.io.tmpdir") + fileSeparator +
        (new AndroidIdentifierGenerator()).getIdentifier(FileType.FILE);

    // useful when more containers are being used, also affects log filename!
    private boolean logSerialId;

    private String apiLevel;

    public String getAndroidHome() {
        return androidHome;
    }

    public void setAndroidHome(String androidHome) {
        this.androidHome = androidHome;
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
        return (logPackageWhitelist != null && !logPackageWhitelist.equals("") || (logPackageBlacklist != null && !logPackageBlacklist
            .equals("")));
    }

    public boolean isLogSerialId() {
        return logSerialId;
    }

    public void setLogSerialId(boolean logSerialId) {
        this.logSerialId = logSerialId;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getStorepass() {
        return storepass;
    }

    public void setStorepass(String storepass) {
        this.storepass = storepass;
    }

    public String getKeypass() {
        return keypass;
    }

    public void setKeypass(String keypass) {
        this.keypass = keypass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getSigalg() {
        return sigalg;
    }

    public void setSigalg(String sigalg) {
        this.sigalg = sigalg;
    }

    public String getKeyalg() {
        return keyalg;
    }

    public void setKeyalg(String keyalg) {
        this.keyalg = keyalg;
    }

    public boolean getRemoveTmpDir() {
        return removeTmpDir;
    }

    public void setRemoveTmpDir(boolean removeTmpDir) {
        this.removeTmpDir = removeTmpDir;
    }

    public String getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(String tmpDir) {
        this.tmpDir = tmpDir;
    }

    public String resolveJavaHome() {
        String JAVA_HOME_ENV = System.getenv("JAVA_HOME");
        String JAVA_HOME_PROPERTY = System.getProperty("java.home");

        return JAVA_HOME_ENV == null ? (JAVA_HOME_PROPERTY == null ? null : JAVA_HOME_PROPERTY) : JAVA_HOME_ENV;
    }

    public String resolveAndroidHome() {
        String ANDROID_HOME_ENV = System.getenv("ANDROID_HOME");
        String ANDROID_HOME_PROPERTY = System.getProperty("android.home");

        return ANDROID_HOME_PROPERTY == null ? (ANDROID_HOME_ENV == null ? null : ANDROID_HOME_ENV) : ANDROID_HOME_PROPERTY;
    }

    public String resolveUserHome() {
        String USER_HOME_ENV = System.getenv("HOME");
        String USER_HOME_PROPERTY = System.getProperty("user.home");

        return USER_HOME_PROPERTY == null ? (USER_HOME_ENV == null ? null : USER_HOME_ENV) : USER_HOME_PROPERTY;
    }

    public String resolveTmpDir() {
        String TMP_DIR_ENV = System.getenv("TMPDIR");
        if (TMP_DIR_ENV == null) {
            TMP_DIR_ENV = System.getenv("TEMP");
        }
        if (TMP_DIR_ENV == null) {
            TMP_DIR_ENV = System.getenv("TMP");
        }

        String TMP_DIR_PROPERTY = System.getProperty("java.io.tmpdir");

        return TMP_DIR_PROPERTY == null ? (TMP_DIR_ENV == null ? null : TMP_DIR_ENV) : TMP_DIR_PROPERTY;
    }

    @Override
    public void validate() throws AndroidContainerConfigurationException {

        if (getAndroidHome() == null) {
            throw new AndroidContainerConfigurationException("You have not set ANDROID_HOME environment property nor "
                + "android.home system property. System property gets precedence.");
        }

        if (getJavaHome() == null) {
            throw new AndroidContainerConfigurationException("You have not set JAVA_HOME environment property nor "
                + "java.home system property. System property gets precedence.");
        }

        Validate.isReadableDirectory(androidHome,
            "You must provide Android SDK home directory. The value you've provided is not valid ("
                + (androidHome == null ? "" : androidHome)
                + "). You can either set it via an environment variable ANDROID_HOME or via"
                + " a property called \"home\" in Arquillian configuration.");

        if (avdName != null && serialId != null) {
            logger.warning("Both \"avdName\" and \"serialId\" properties are defined, the device "
                + "specified by \"serialId\" will get priority if connected.");
        } else if (avdName == null) {
            avdName = UUID.randomUUID().toString();
        }

        if (generatedAvdPath != null) {
            Validate.notNullOrEmpty(generatedAvdPath, "Directory you specified to store AVD to is empty string.");
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

        File tmpDir = new File(getTmpDir());

        if (!tmpDir.exists()) {
            DroidiumFileUtils.createTmpDir(tmpDir);
        } else {
            Validate.isReadableDirectory(getTmpDir(),
                "Temporary directory you chose to use for Arquillian Droidium native plugin "
                    + "is not readable. Please be sure you entered a path you have read and write access to.");

            Validate.isWritable(new File(getTmpDir()), "Temporary directory you chose to use for Arquillian Droidium "
                + "native plugin is not writable. Please be sure you entered a path you have read and write access to.");
        }

        try {
            Validate.isReadable(getKeystore(), "Key store for Android APKs is not readable. File does not exist or you have "
                + "no read access to this file. In case it does not exist, Arquillian Droidium native plugin tries to create "
                + "keystore you specified dynamically in the file " + getKeystore());
        } catch (IllegalArgumentException ex) {

        }

        Validate.notNullOrEmpty(getAlias(),
            "You must provide valid alias for signing of APK files. You entered '" + getAlias() + "'.");
        Validate.notNullOrEmpty(getKeypass(),
            "You must provide valid keypass for signing of APK files. You entered '" + getKeypass() + "'.");
        Validate.notNullOrEmpty(getStorepass(),
            "You must provide valid storepass for signing of APK files. You entered '" + getStorepass() + "'.");
        Validate.notNullOrEmpty(getKeyalg(), "You must provide valid key algorithm for signing packages. You entered '"
            + getKeyalg() + "'.");
        Validate.notNullOrEmpty(getSigalg(), "You must provide valid key algoritm for signing packages. You entered '" +
            getSigalg() + "'.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\navdName\t\t\t:").append(this.avdName).append("\n");
        sb.append("generatedAvdPath\t:").append(this.generatedAvdPath).append("\n");
        sb.append("apiLevel\t\t:").append(this.apiLevel).append("\n");
        sb.append("serialId\t\t:").append(this.serialId).append("\n");
        sb.append("force\t\t\t:").append(this.forceNewBridge).append("\n");
        sb.append("sdCard\t\t\t:").append(this.sdCard).append("\n");
        sb.append("sdSize\t\t\t:").append(this.sdSize).append("\n");
        sb.append("generateSD\t\t:").append(this.generateSDCard).append("\n");
        sb.append("abi\t\t\t:").append(this.abi).append("\n");
        sb.append("emuBoot\t\t\t:").append(this.emulatorBootupTimeoutInSeconds).append("\n");
        sb.append("emuShut\t\t\t:").append(this.emulatorShutdownTimeoutInSeconds).append("\n");
        sb.append("emuOpts\t\t\t:").append(this.emulatorOptions).append("\n");
        sb.append("home\t\t\t:").append(this.androidHome).append("\n");
        sb.append("consolePort\t\t:").append(this.consolePort).append("\n");
        sb.append("adbPort\t\t\t:").append(this.adbPort).append("\n");
        sb.append("logLevel\t\t:").append(this.logLevel).append("\n");
        sb.append("logType\t\t\t:").append(this.logType).append("\n");
        sb.append("logFilePath\t\t:").append(this.logFilePath).append("\n");
        sb.append("logPackageWhitelist\t:").append(this.logPackageWhitelist).append("\n");
        sb.append("logPackageBlacklist\t:").append(this.logPackageBlacklist).append("\n");
        sb.append("keystore\t\t:").append(this.keystore).append("\n");
        sb.append("keypass\t\t\t:").append(this.keypass).append("\n");
        sb.append("storepass\t\t:").append(this.storepass).append("\n");
        sb.append("alias\t\t\t:").append(this.alias).append("\n");
        sb.append("sigalg\t\t\t:").append(this.sigalg).append("\n");
        sb.append("keyalg\t\t\t:").append(this.keyalg).append("\n");
        sb.append("removeTmpDir\t\t:").append(this.removeTmpDir).append("\n");
        sb.append("tmpDir\t\t\t:").append(this.tmpDir).append("\n");
        return sb.toString();
    }

}
