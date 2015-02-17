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
import java.util.HashMap;
import java.util.Map;

import org.arquillian.droidium.container.configuration.Validate;

/**
 * Configuration for Arquillian Droidium Native extension.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeConfiguration {

    public static final String SELENDROID_VERSION = "0.14.0";

    public static final String SERVER_HOME = System.getProperty("user.home") + "/.droidium/selendroid-server-" + SELENDROID_VERSION + ".apk";

    public static final String DRIVER_HOME = System.getProperty("user.home") + "/.droidium/android-driver-app-" + SELENDROID_VERSION + ".apk";

    private String serverApk = SERVER_HOME;

    private String driverApk = DRIVER_HOME;

    private Map<String, String> properties = new HashMap<String, String>();

    public File getServerApk() {
        return new File(getProperty("serverApk", serverApk));
    }

    public File getDriverApk() {
        return new File(getProperty("driverApk", driverApk));
    }

    /**
     * Sets properties as configuration.
     *
     * @param properties properties to set
     * @throws IllegalArgumentException if {@code properties} is a null object
     */
    public void setProperties(Map<String, String> properties) throws IllegalArgumentException {
        Validate.notNull(properties, "Properties to set for Arquillian Droidium native configuration can not be a null object!");
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

    /**
     * Validates configuration of Arquillian Droidium native plugin.
     */
    public void validate() throws IllegalArgumentException, IllegalStateException {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-40s %s\n", "serverApk", getServerApk()));
        sb.append(String.format("%-40s %s\n", "driverApk", getDriverApk()));
        return sb.toString();
    }
}
