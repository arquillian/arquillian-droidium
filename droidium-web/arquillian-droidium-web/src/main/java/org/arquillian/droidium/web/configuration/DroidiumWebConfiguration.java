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
package org.arquillian.droidium.web.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.arquillian.droidium.container.configuration.Validate;

/**
 * Configuration for Droidium for web in Arquillian.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumWebConfiguration {

    private String serverApk = "android-server.apk";

    private String logFile = "target" + System.getProperty("file.separator") + "android.log";

    private String options = "";

    private String debug = "false";

    private Map<String, String> properties = new HashMap<String, String>();

    public File getServerApk() {
        return new File(getProperty("serverApk", serverApk));
    }

    public File getLogFile() {
        return new File(getProperty("logFile", logFile));
    }

    public String getOptions() {
        return new String(getProperty("options", options));
    }

    public boolean getDebug() {
        return Boolean.parseBoolean(getProperty("debug", this.debug));
    }

    /**
     * Sets properties as configuration.
     *
     * @param properties properties to set
     * @throws IllegalArgumentException if {@code properties} is a null object
     */
    public void setProperties(Map<String, String> properties) {
        Validate.notNull(properties, "Properties to set for Arquillian Droidium web configuration can not be a null object.");
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
    public String getProperty(String name, String defaultValue) {
        Validate.notNullOrEmpty(name, "unable to get configuration value of null configuration key");
        Validate.notNull(defaultValue, "unable to set configuration value of " + name + " to null");

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
    public void setProperty(String property, String value) {
        Validate.notNullOrEmpty(property, "unable to set configuration value which key is null");
        Validate.notNull(value, "unable to set configuration value which is null");

        properties.put(property, value);
    }

    /**
     * Validates configuration of Arquillian Droidium web plugin.
     *
     * @throws IllegalArgumentException if {@code getServerApk()}
     * @throws IllegalStateException if it is impossible to create new file as {@code getLogFile()}.
     */
    public void validate() {

        Validate.isReadable(getServerApk(), "You must provide a valid path to Android Server APK for "
            + "Arquillian Droidium web plugin. Plese be sure you have read access to the file you have specified: "
            + getServerApk());

        try {
            getLogFile().createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to create logging file for Arquillian Droidium web plugin at"
                + getLogFile().getAbsolutePath() + ".", e);
        }

    }
}
