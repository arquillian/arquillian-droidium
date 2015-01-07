/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.droidium.platform.impl;

import java.io.File;

/**
 * Simple validation utility
 *
 * @author <a href="@mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class Validate {

    /**
     * Checks if some object is null or not.
     *
     * @param object object to check against nullity
     * @param message the exception message
     * @throws IllegalStateException if object is null
     * @throws IllegalArgumentException when message object is null
     */
    public static void notNull(final Object object, final String message) throws IllegalStateException,
        IllegalArgumentException {
        if (message == null) {
            throw new IllegalArgumentException("Exception message is a null object!");
        }
        if (object == null) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that all specified objects are not null objects.
     *
     * @param objects objects to check against nullity
     * @param message exception message
     * @throws IllegalArgumentException throws if at leas one object is null
     */
    public static void notNulls(final Object[] objects, final String message) throws IllegalStateException {
        notNull(message, "Exception message is a null object!");
        notNull(objects, "Array to check the nullity of objects is a null object.");

        for (Object o : objects) {
            if (o == null) {
                throw new IllegalStateException(message);
            }
        }
    }

    /**
     * Checks that the specified String is not null or empty, throws exception if it is.
     *
     * @param string The object to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if string is null
     */
    public static void notNullOrEmpty(final String string, final String message) throws IllegalStateException {
        notNull(message, "Exception message is a null object!");

        if (string == null || string.trim().length() == 0) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that at least one of specified String is not empty
     *
     * @param strings The array of strings to be checked
     * @param message The exception message
     * @throws AndroidConfigurationException Throws if all strings are null or empty
     */
    public static void notAllNullsOrEmpty(final String[] strings, final String message) throws IllegalStateException {
        notNull(message, "Exception message is a null object!");
        notNull(strings, "Array to check the nullity of objects is null object!");

        if (strings.length == 0) {
            return;
        }

        for (String string : strings) {
            if (string != null && string.trim().length() != 0) {
                return;
            }
        }

        throw new IllegalStateException(message);
    }

    /**
     * Checks that the specified String is not null or empty and represents a readable file, throws exception if it is empty or
     * null and does not represent a path to a file.
     *
     * @param path The path to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if path is empty, null or invalid
     */
    public static boolean isReadable(final String path, final String message) throws IllegalArgumentException {
        notNullOrEmpty(path, "File to check against readability is null object or empty.");

        File file = new File(path);

        return isReadable(file, message);
    }

    /**
     * Checks that the specified String is not null or empty and represents a readable directory, throws exception if it is
     * empty or null and does not represent a path to a directory.
     *
     * @param path The path to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if path is empty, null or invalid
     */
    public static void isReadableDirectory(final String path, final String message) throws IllegalArgumentException {
        notNullOrEmpty(path, message);

        File file = new File(path);
        isReadableDirectory(file, message);
    }

    /**
     * Checks that the specified {@code file} represents a readable file.
     *
     * @param file The file to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if file is null or invalid
     */
    public static boolean isReadable(final File file, final String message) throws IllegalArgumentException {
        notNull(file, message);

        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(message);
        }

        return true;
    }

    /**
     * Checks that the specified {@code file} represents a readable directory.
     *
     * @param file The path to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if file is null or invalid
     */
    public static void isReadableDirectory(final File file, final String message) throws IllegalArgumentException {
        notNull(file, "Directory to check against readability is null object.");

        if (!file.exists() || !file.isDirectory() || !file.canRead() || !file.canExecute()) {
            throw new IllegalArgumentException(message);
        }

    }

    /**
     * Checks if port is in range 0 - 65535
     *
     * @param port port number to check the validity of
     * @return true if port is in sane range, false otherwise
     */
    public static boolean isPortValid(int port) {
        return port > 0 && port < 65535;
    }

    /**
     * Checks if port is in range 0 - 65535
     *
     * @param port port number to check the validity of
     * @return true if port is in sane range, false otherwise
     * @throws NumberFormatException when port is not a number
     */
    public static boolean isPortValid(String port) {
        return isPortValid(Integer.parseInt(port));
    }

    /**
     * Checks that the specified File is not null or empty and represents a writeable file, throws exception if it is empty or
     * null and does not represent a path to a file.
     *
     * @param file The file to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if file is null or invalid
     */
    public static void isWritable(final File file, String message) throws IllegalArgumentException {
        notNull(file, "File to check against writability is a null object.");
        notNullOrEmpty(message, "exception message can not be null or empty string");

        if (!file.exists() || !file.canWrite()) {
            throw new IllegalArgumentException(message);
        }
    }

}
