/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
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
package org.arquillian.droidium.platform.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumPlatformConfiguration {

    private static final Logger logger = Logger.getLogger(DroidiumPlatformConfiguration.class.getName());

    private String fileSeparator = System.getProperty("file.separator");

    private Map<String, String> properties = new HashMap<String, String>();

    private String forceNewBridge = "false";

    private String androidHome = resolveAndroidHome();

    private String androidSdkHome = resolveAndroidSdkHome();

    private String javaHome = resolveJavaHome();

    private String ddmlibCommandTimeout = "20000";

    private String storepass = "android";

    private String keypass = "android";

    private String alias = "androiddebugkey";

    private String sigalg = "SHA1withRSA";

    private String keyalg = "RSA";

    private String removeTmpDir = "true";

    private String adbServerPort = "5037";

    // by default null since its default value depends on androidSdkHome which
    // can be changed by user, when not set by user, it will be resolved in validate() in this class
    private String keystore = null;

    private String tmpDir = resolveTmpDir();

    public boolean isForceNewBridge() {
        return Boolean.parseBoolean(getProperty("forceNewBridge", forceNewBridge));
    }

    public String getAndroidHome() {
        return getProperty("androidHome", androidHome);
    }

    public String getAndroidSdkHome() {
        return getProperty("androidSdkHome", androidSdkHome);
    }

    public String getJavaHome() {
        return getProperty("javaHome", javaHome);
    }

    public String getDdmlibCommandTimeout() {
        return getProperty("ddmlibCommandTimeout", ddmlibCommandTimeout);
    }

    public String getStorepass() {
        return getProperty("storepass", storepass);
    }

    public String getKeypass() {
        return getProperty("keypass", keypass);
    }

    public String getAlias() {
        return getProperty("alias", alias);
    }

    public String getSigalg() {
        return getProperty("sigalg", sigalg);
    }

    public String getKeyalg() {
        return getProperty("keyalg", keyalg);
    }

    public boolean isRemoveTmpDir() {
        return Boolean.parseBoolean(getProperty("removeTmpDir", removeTmpDir));
    }

    public String getAdbServerPort() {
        return getProperty("adbServerPort", adbServerPort);
    }

    public String getKeystore() {
        return getProperty("keystore", keystore);
    }

    public File getTmpDir() {
        return new File(getProperty("tmpDir", tmpDir));
    }

    public Boolean getRemoveTmpDir() {
        return Boolean.parseBoolean(getProperty("removeTmpDir", removeTmpDir));
    }

    public Boolean getForceNewBridge() {
        return Boolean.parseBoolean(getProperty("forceNewBridge", forceNewBridge));
    }

    // helpers

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

    public String resolveAndroidSdkHome() {
        String ANDROID_SDK_HOME_ENV = System.getenv("ANDROID_SDK_HOME");
        String ANDROID_SDK_HOME_PROPERTY = System.getProperty("android.sdk.home");

        return checkSlash(ANDROID_SDK_HOME_PROPERTY == null ?
            (ANDROID_SDK_HOME_ENV == null ? resolveUserHome() : ANDROID_SDK_HOME_ENV)
            : ANDROID_SDK_HOME_PROPERTY);
    }

    public String resolveUserHome() {
        String USER_HOME_ENV = System.getenv("HOME");
        String USER_HOME_PROPERTY = System.getProperty("user.home");

        return checkSlash(USER_HOME_PROPERTY == null ? USER_HOME_ENV : USER_HOME_PROPERTY);
    }

    public String resolveTmpDir() {
        String TMP_DIR_ENV = System.getenv("TMPDIR");
        if (TMP_DIR_ENV == null) {
            TMP_DIR_ENV = System.getenv("TEMP");
        }
        if (TMP_DIR_ENV == null) {
            TMP_DIR_ENV = System.getenv("TMP");
        }

        if (TMP_DIR_ENV != null) {
            TMP_DIR_ENV = TMP_DIR_ENV.trim();
        }

        String TMP_DIR_PROPERTY = System.getProperty("droidium.tmpdir");

        if (TMP_DIR_PROPERTY != null) {
            TMP_DIR_PROPERTY = TMP_DIR_PROPERTY.trim();
        }

        if (TMP_DIR_ENV == null && TMP_DIR_PROPERTY == null) {
            return checkSlash(System.getProperty("java.io.tmpdir"));
        } else {
            return checkSlash(TMP_DIR_PROPERTY == null ? TMP_DIR_ENV : TMP_DIR_PROPERTY);
        }

    }

    public void setProperties(Map<String, String> properties) {
        Validate.notNull(properties, "Properties to set for Arquillian Droidium Platform configuration can not be a null object!");
        this.properties = properties;
    }

    /**
     * Gets value of {@code name} property. In case a value for such name does not exist or is null or empty string,
     * {@code defaultValue} is returned.
     *
     * @param name name of property you want to get a value of
     * @param defaultValue value returned in case {@code name} is not found
     * @return value of a {@code name} property
     * @throws IllegalArgumentException if {@code name} is a null object or an empty string
     */
    public String getProperty(String name, String defaultValue) throws IllegalStateException {
        Validate.notNullOrEmpty(name, "unable to get configuration value of null configuration key");

        String found = properties.get(name);
        if (found == null || found.length() == 0) {
            return defaultValue;
        } else {
            return found;
        }
    }

    /**
     * Sets {@code property} to {@code value}.
     *
     * @param property property to set
     * @param value value of property
     * @throws IllegalArgumentException if either arguments are null or empty strings
     */
    public void setProperty(String property, String value) throws IllegalStateException {
        Validate.notNullOrEmpty(property, "unable to set configuration value which key is null");
        Validate.notNullOrEmpty(value, "unable to set configuration value which is null");

        properties.put(property, value);
    }

    public void validate() {

        if (getAndroidHome() == null) {
            throw new IllegalStateException("You have not set ANDROID_HOME environment property nor "
                + "android.home system property. System property gets precedence.");
        } else {
            setProperty("androidHome", checkSlash(getAndroidHome()));
        }

        if (getJavaHome() == null) {
            throw new IllegalStateException("You have not set JAVA_HOME environment property nor "
                + "java.home system property. System property gets precedence.");
        } else {
            setProperty("javaHome", checkSlash(getJavaHome()));
        }

        setProperty("androidSdkHome", checkSlash(getAndroidSdkHome()));

        Validate.isReadableDirectory(getAndroidSdkHome(),
            "You must provide Android SDK home directory. The value you have provided is not valid. "
                + "You can either set it via an environment variable ANDROID_SDK_HOME or via "
                + "property called \"androidSdkHome\" in Arquillian configuration or you can set it as system property "
                + "\"android.sdk.home\". When this property is not specified anywhere, it defaults to \"" + resolveUserHome()
                + "\"");

        Validate.isReadableDirectory(getAndroidHome(),
            "You must provide Android home directory. The value you have provided is not valid. "
                + "You can either set it via environment variable ANDROID_HOME or via "
                + "property called \"androidHome\" in Arquillian configuration or you can set it as system property "
                + "\"android.home\".");

        if (adbServerPort != null) {
            Validate.isPortValid(adbServerPort);
        }

        File tmpDir = new File(getTmpDir(), UUID.randomUUID().toString());
        setProperty("tmpDir", tmpDir.getAbsolutePath());

        if (!tmpDir.exists()) {
            createTmpDir(tmpDir);
        }

        if (keystore == null) {
            keystore = getAndroidSdkHome() + ".android" + fileSeparator + "debug.keystore";
            setProperty("keystore", keystore);
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

    public Map<String, String> getAndroidSystemEnvironmentProperties() {
        Map<String, String> androidEnvironmentProperties = new HashMap<String, String>();

        androidEnvironmentProperties.put("ANDROID_HOME", getAndroidHome());
        androidEnvironmentProperties.put("ANDROID_SDK_HOME", getAndroidSdkHome());
        androidEnvironmentProperties.put("ANDROID_ADB_SERVER_PORT", getAdbServerPort());

        return androidEnvironmentProperties;
    }

    private String checkSlash(String path) {

        if (path == null || path.isEmpty()) {
            return null;
        }

        return path.endsWith(fileSeparator) ? path : path + fileSeparator;
    }

    private void createTmpDir(File parent) {
        try {
            boolean created = parent.mkdirs();
            if (!created) {
                throw new RuntimeException("Unable to create temporary directory " + parent.getAbsolutePath());
            }
        } catch (SecurityException ex) {
            logger.severe("Security manager denies to create the working dir in " + parent.getAbsolutePath());
            throw new RuntimeException("Unable to create working directory in " + parent.getAbsolutePath());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-40s %s\n", "HOME", resolveUserHome()));
        sb.append(String.format("%-40s %s\n", "JAVA_HOME", getJavaHome()));
        sb.append(String.format("%-40s %s\n", "ANDROID_HOME", getAndroidHome()));
        sb.append(String.format("%-40s %s\n", "ANDROID_SDK_HOME", getAndroidSdkHome()));
        sb.append(String.format("%-40s %s\n", "adbServerPort", getAdbServerPort()));
        sb.append(String.format("%-40s %s\n", "keystore", getKeystore()));
        sb.append(String.format("%-40s %s\n", "keypass", getKeypass()));
        sb.append(String.format("%-40s %s\n", "storepass", getStorepass()));
        sb.append(String.format("%-40s %s\n", "alias", getAlias()));
        sb.append(String.format("%-40s %s\n", "sigalg", getSigalg()));
        sb.append(String.format("%-40s %s\n", "keyalg", getKeyalg()));
        sb.append(String.format("%-40s %s\n", "tmpDir", getTmpDir()));
        sb.append(String.format("%-40s %s\n", "removeTmpDir", getRemoveTmpDir()));
        sb.append(String.format("%-40s %s\n", "ddmlibCommandTimeout", getDdmlibCommandTimeout()));
        sb.append(String.format("%-40s %s", "forceNewBridge", getForceNewBridge()));
        return sb.toString();
    }
}
