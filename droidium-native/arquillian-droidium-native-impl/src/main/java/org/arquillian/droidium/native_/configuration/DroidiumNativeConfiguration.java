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
 */
package org.arquillian.droidium.native_.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.arquillian.droidium.container.configuration.Validate;

/**
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeConfiguration {

    private String fileSeparator = System.getProperty("file.separator");

    private String logFile = "target" + fileSeparator + "android.log";

    private String serverApk = "selendroid-server.apk";

    private String serverPort = "8080";

    private String keystore = System.getProperty("user.home") + fileSeparator + ".android" + fileSeparator + "debug.keystore";

    private String storepass = "android";

    private String keypass = "android";

    private String alias = "androiddebugkey";

    private String sigalg = "SHA1withRSA";

    private String keyalg = "RSA";

    private String removeTmpDir = "true";

    private String tmpDir = System.getProperty("java.io.tmpdir");

    private Map<String, String> properties = new HashMap<String, String>();

    public File getLogFile() {
        return new File(getProperty("logFile", logFile));
    }

    public File getServerApk() {
        return new File(getProperty("serverApk", serverApk));
    }

    public String getServerPort() {
        return getProperty("serverPort", serverPort);
    }

    public File getKeystore() {
        return new File(getProperty("keystore", keystore));
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

    public boolean getRemoveTmpDir() {
        return Boolean.parseBoolean(getProperty("removeTmpDir", removeTmpDir));
    }

    public File getTmpDir() {
        return new File(getProperty("tmpDir", tmpDir));
    }

    /**
     * Sets properties as configuration.
     *
     * @param properties properties to set
     * @throws IllegalArgumentException if {@code properties} is a null object
     */
    public void setProperties(Map<String, String> properties) throws IllegalArgumentException {
        Validate.notNull(properties, "Properties to set for Arquillian Droidium native configuration can not be a null object");
        this.properties = properties;
    }

    /**
     * Gets value of {@code name} property. In case a value for such name does not exist or is null or empty string,
     * {@code defaultValue} is returned.
     *
     * @param name name of property you want to get a value of
     * @param defaultValue value returned in case {@code name} is a null string or it is empty
     * @return value of a {@code name} property
     * @throws IllegalArgumentException if either arguments are null or empty strings
     */
    public String getProperty(String name, String defaultValue) throws IllegalStateException {
        Validate.notNullOrEmpty(name, "unable to get configuration value of null configuration key");
        Validate.notNullOrEmpty(defaultValue, "unable to set configuration value of " + name + " to null");

        String found = properties.get(name);
        if (found == null || found.isEmpty()) {
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

    /**
     * Validates configuration of Arquillian Droidium native plugin.
     *
     * @throws IllegalArgumentException if {@code getServerApk()} is not readable or if {@code getTmpDir()} is not readable
     *         directory of if {@code getTmpDir()} is not writable of if {@code getAlias()},{@code getKeypass()} or
     *         {@code getStorepass()} is either null or empty string.
     * @throws IllegalStateException if it is impossible to create new file as {@code getLogFile()}.
     */
    public void validate() throws IllegalArgumentException, IllegalStateException {
        Validate.isReadable(getServerApk(), "You must provide a valid path to Android Server APK for "
            + "Arquillian Droidium native plugin. Plese be sure you have read access to the file you entered: "
            + getServerApk());

        Validate.isReadableDirectory(getTmpDir(),
            "Temporary directory you chosed to use for Arquillian Droidium native plugin "
                + "is not readable. Please be sure you entered a path you have read and write access to.");

        Validate.isWriteable(getTmpDir(), "Temporary directory you chosed to use for Arquillian Droidium native plugin "
            + "is not writable. Please be sure you entered a path you have read and write access to.");

        try {
            Validate.isReadable(getKeystore(), "Key store for Android APKs is not readable. File does not exist or you have "
                + "no read access to this file. In case it does not exist, Arquillian Droidium native plugin tries to create "
                + "keystore you specified dynamically in the file " + getKeystore());
        } catch (IllegalArgumentException ex) {

        }

        try {
            getLogFile().createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create logging file for Arquillian Droidium native plugin at"
                + getLogFile().getAbsolutePath(), e);
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
}
