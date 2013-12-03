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
import java.util.HashMap;
import java.util.Map;
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

    private String emulatorOptions;

    private String sdSize = "128M";

    private String sdCard;

    private String sdCardLabel;

    private boolean generateSDCard;

    private String abi;

    private long emulatorBootupTimeoutInSeconds = 120L;

    private long emulatorShutdownTimeoutInSeconds = 60L;

    private String androidHome = resolveAndroidHome();

    private String androidSdkHome = resolveAndroidSdkHome();

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

    // by default null since its default value depends on androidSdkHome which
    // can be changed by user, when not set by user, it will be resolved in validate() in this class
    private String keystore = null;

    private String storepass = "android";

    private String keypass = "android";

    private String alias = "androiddebugkey";

    private String sigalg = "SHA1withRSA";

    private String keyalg = "RSA";

    private boolean removeTmpDir = true;

    private String tmpDir = resolveTmpDir();

    // useful when more containers are being used, also affects log filename!
    private boolean logSerialId;

    private String apiLevel;

    public String getAndroidSdkHome() {
        return androidSdkHome;
    }

    public void setAndroidSdkHome(String androidSdkHome) {
        this.androidSdkHome = androidSdkHome;
    }

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

        return checkSlash(JAVA_HOME_PROPERTY == null ? JAVA_HOME_ENV : JAVA_HOME_PROPERTY);
    }

    public String resolveAndroidHome() {
        String ANDROID_HOME_ENV = System.getenv("ANDROID_HOME");
        String ANDROID_HOME_PROPERTY = System.getProperty("android.home");

        return checkSlash(ANDROID_HOME_PROPERTY == null ? ANDROID_HOME_ENV : ANDROID_HOME_PROPERTY);
    }

    public String resolveUserHome() {
        String USER_HOME_ENV = System.getenv("HOME");
        String USER_HOME_PROPERTY = System.getProperty("user.home");

        return checkSlash(USER_HOME_PROPERTY == null ? USER_HOME_ENV : USER_HOME_PROPERTY);
    }

    public String resolveAndroidSdkHome() {
        String ANDROID_SDK_HOME_ENV = System.getenv("ANDROID_SDK_HOME");
        String ANDROID_SDK_HOME_PROPERTY = System.getProperty("android.sdk.home");

        return checkSlash(ANDROID_SDK_HOME_PROPERTY == null ?
            (ANDROID_SDK_HOME_ENV == null ? resolveUserHome() : ANDROID_SDK_HOME_ENV)
            : ANDROID_SDK_HOME_PROPERTY);
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

        return checkSlash(TMP_DIR_PROPERTY == null ? TMP_DIR_ENV : TMP_DIR_PROPERTY);
    }

    @Override
    public void validate() throws AndroidContainerConfigurationException {

        if (getAndroidHome() == null) {
            throw new AndroidContainerConfigurationException("You have not set ANDROID_HOME environment property nor "
                + "android.home system property. System property gets precedence.");
        } else {
            setAndroidHome(checkSlash(getAndroidHome()));
        }

        if (getJavaHome() == null) {
            throw new AndroidContainerConfigurationException("You have not set JAVA_HOME environment property nor "
                + "java.home system property. System property gets precedence.");
        } else {
            setJavaHome(checkSlash(getJavaHome()));
        }

        setAndroidSdkHome(checkSlash(androidSdkHome));
        Validate.isReadableDirectory(androidSdkHome,
            "You must provide Android SDK home directory. The value you've provided is not valid. "
                + "You can either set it via an environment variable ANDROID_SDK_HOME or via "
                + "property called \"androidSdkHome\" in Arquillian configuration or you can set it as system property "
                + "\"android.sdk.home\". When this property is not specified anywhere, it defaults to \"" + resolveUserHome() + "\"");

        Validate.isReadableDirectory(androidHome,
            "You must provide Android home directory. The value you have provided is not valid. "
            + "You can either set it via environment variable ANDROID_HOME or via "
            + "property called \"androidHome\" in Arquillian configuration or you can set it as system property "
            + "\"android.home\".");

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

        File tmpDir = new File(getTmpDir() + (new AndroidIdentifierGenerator()).getIdentifier(FileType.FILE));
        setTmpDir(tmpDir.getAbsolutePath());

        if (!tmpDir.exists()) {
            DroidiumFileUtils.createTmpDir(tmpDir);
        }

        if (keystore == null) {
            keystore = getAndroidSdkHome() + ".android" + fileSeparator + "debug.keystore";
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

        System.out.println(this.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HOME\t\t\t").append(resolveUserHome());
        sb.append("\nJAVA_HOME\t\t").append(javaHome);
        sb.append("\nANDROID_HOME\t\t").append(androidHome);
        sb.append("\nANDROID_SDK_HOME\t").append(androidSdkHome);
        sb.append("\navdName\t\t\t").append(avdName);
        sb.append("\nserialId\t\t").append(serialId);
        sb.append("\napiLevel\t\t").append(apiLevel);
        sb.append("\nabi\t\t\t").append(abi);
        sb.append("\nconsolePort\t\t").append(consolePort);
        sb.append("\nadbPort\t\t\t").append(adbPort);
        sb.append("\nemuBoot\t\t\t").append(emulatorBootupTimeoutInSeconds);
        sb.append("\nemuShut\t\t\t").append(emulatorShutdownTimeoutInSeconds);
        sb.append("\nemuOpts\t\t\t").append(emulatorOptions);
        sb.append("\nkeystore\t\t").append(keystore);
        sb.append("\nkeypass\t\t\t").append(keypass);
        sb.append("\nstorepass\t\t").append(storepass);
        sb.append("\nalias\t\t\t").append(alias);
        sb.append("\nsigalg\t\t\t").append(sigalg);
        sb.append("\nkeyalg\t\t\t").append(keyalg);
        sb.append("\nsdCard\t\t\t").append(sdCard);
        sb.append("\nsdSize\t\t\t").append(sdSize);
        sb.append("\ngenerateSD\t\t").append(generateSDCard);
        sb.append("\nlogLevel\t\t").append(logLevel);
        sb.append("\nlogType\t\t\t").append(logType);
        sb.append("\nlogFilePath\t\t").append(logFilePath);
        sb.append("\nlogPackageWhitelist\t").append(logPackageWhitelist);
        sb.append("\nlogPackageBlacklist\t").append(logPackageBlacklist);
        sb.append("\nremoveTmpDir\t\t").append(removeTmpDir);
        sb.append("\ntmpDir\t\t\t").append(tmpDir);
        sb.append("\nforce\t\t\t").append(forceNewBridge);
        return sb.toString();
    }

    public Map<String, String> getAndroidSystemEnvironmentProperties() {
        Map<String, String> androidEnvironmentProperties = new HashMap<String, String>();

        androidEnvironmentProperties.put("ANDROID_HOME", androidHome);
        androidEnvironmentProperties.put("ANDROID_SDK_HOME", androidSdkHome);

        return androidEnvironmentProperties;
    }

    private String checkSlash(String path) {

        if (path == null || path.isEmpty()) {
            return null;
        }

        return path.endsWith(fileSeparator) ? path : path + fileSeparator;
    }

}
