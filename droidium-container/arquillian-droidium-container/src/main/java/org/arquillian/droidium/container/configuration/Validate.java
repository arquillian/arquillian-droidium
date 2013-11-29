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
package org.arquillian.droidium.container.configuration;

import java.io.File;

/**
 * Simple validation utility
 *
 * @author <a href="@mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class Validate {

    /**
     * Minimal number of console port.
     */
    public static final long CONSOLE_PORT_MIN = 5554;

    /**
     * Maximal number of console port.
     */
    public static final long CONSOLE_PORT_MAX = 5584;

    /**
     * Minimal number of adb port.
     */
    public static final long ADB_PORT_MIN = 5555;

    /**
     * Maximal number of adb port.
     */
    public static final long ADB_PORT_MAX = 5585;

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
        notNullOrEmpty(path, "Directory to check against readability is null object or empty string.");

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
        notNull(file, "File to check against readability is null object.");

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
     * Checks if user set size of SD card in the proper format.
     *
     * SD card size has to be between 9M (9126K) and 1023G. Everything out of this range is considered to be invalid. This
     * method follows size and format recommendation of {@code mksdcard} tool from the Android tools distribution.
     *
     * @param sdSize size of sd card
     * @param message The exception message
     * @throws AndroidContainerConfigurationException when sdSize is invalid
     */
    public static void sdSize(final String sdSize, final String message) throws AndroidContainerConfigurationException {
        notNullOrEmpty(message, "Exception message is a null object!");
        notNullOrEmpty(sdSize, "Size of the Android SD card to check is null object or empty string");

        if (!(sdSize.trim().length() >= 2) || !sdSize.matches("^[1-9]{1}[0-9]*[KGM]?$")) {
            throw new AndroidContainerConfigurationException(message);
        }

        String sizeString = null;
        String sizeUnit = null;

        if (sdSize.substring(sdSize.length() - 1).matches("[KGM]")) {
            sizeString = sdSize.substring(0, sdSize.length() - 1);
            sizeUnit = sdSize.substring(sdSize.length() - 1);
        } else {
            sizeString = sdSize;
        }

        long size;

        try {
            size = Long.parseLong(sizeString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse '" + sizeString + "' to number.");
        }

        if (sizeUnit == null) {
            if (size > 1099511627264L || size < 9437184) {
                throw new AndroidContainerConfigurationException(
                    "Minimum size is 9M. Maximum size is 1023G. The Android emulator cannot use smaller or bigger images.");
            }
            return;
        }

        if ((size > 1023 && sizeUnit.equals("G"))
            || (size > 1048575 && sizeUnit.equals("M"))
            || (size > 1073741823 && sizeUnit.equals("K"))
            || (size < 9 && sizeUnit.equals("M"))
            || (size < 9126 && sizeUnit.equals("K"))) {
            throw new AndroidContainerConfigurationException(
                "Maximum size is 1099511627264 bytes, 1073741823K, 1048575M or 1023G. Minimum size is 9M. " +
                    "The Android emulator cannot use smaller images.");
        }
    }

    /**
     * Checks if console port is in valid range.
     *
     * Console port has to be even number in range {@value #CONSOLE_PORT_MIN} - {@value #CONSOLE_PORT_MAX}.
     *
     * @param consolePort console port to check validity of
     * @throws AndroidContainerConfigurationException if console port is null or not a number or not valid
     */
    public static void isConsolePortValid(final String consolePort) throws AndroidContainerConfigurationException {
        notNullOrEmpty(consolePort, "console port to validate is null or empty.");

        try {
            long port = Long.parseLong(consolePort);
            if (!(port >= CONSOLE_PORT_MIN && port <= CONSOLE_PORT_MAX && port % 2 == 0)) {
                throw new AndroidContainerConfigurationException(
                    "Console port is not in the right range or it is not an even number. It has to be in the range "
                        + CONSOLE_PORT_MIN
                        + "-" + CONSOLE_PORT_MAX + ".");
            }
        } catch (NumberFormatException e) {
            throw new AndroidContainerConfigurationException(
                "Unable to get console port number from the string '" + consolePort + "'.");
        }
    }

    /**
     * Checks if adb port is in valid range.
     *
     * {@code adbPort} port has to be odd number in range {@value #ADB_PORT_MIN} - {@value #ADB_PORT_MAX}
     *
     * @param adbPort adb port to check validity of
     * @throws AndroidContainerConfigurationException if adb port is null or not a number or not valid
     */
    public static void isAdbPortValid(final String adbPort) throws AndroidContainerConfigurationException {
        notNullOrEmpty(adbPort, "adb port to validate is null or empty");

        try {
            long port = Long.parseLong(adbPort);
            if (!(port >= ADB_PORT_MIN && port <= ADB_PORT_MAX && port % 2 == 1)) {
                throw new AndroidContainerConfigurationException(
                    "Adb port is not in the right range or it is not an odd number. It has to be in the range "
                        + ADB_PORT_MIN
                        + "-" + ADB_PORT_MAX + ".");
            }
        } catch (NumberFormatException e) {
            throw new AndroidContainerConfigurationException(
                "Unable to get adb port number from string '" + adbPort + "'.");
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
     * Checks if file name of the SD card is valid which means it has to have suffix of ".img".
     *
     * @param fileName name of the file to check validity of
     * @param message exception message
     * @throws AndroidContainerConfigurationException if file name of SD card is not valid.
     */
    public static void sdCardFileName(final String fileName, final String message)
        throws AndroidContainerConfigurationException {
        notNullOrEmpty(fileName, "SD card file name to validate is null or empty string");
        notNullOrEmpty(message, "exception message can't be null or empty string");

        if (fileName.endsWith(System.getProperty("file.separator"))) {
            throw new AndroidContainerConfigurationException("File name of SD card can't end with " +
                "system file separator. It denotes a directory and not a file!");
        }

        String[] tokens = new File(fileName).getName().split("\\.");
        if (!(tokens.length >= 2) || !tokens[tokens.length - 1].equals("img") || tokens[0].trim().isEmpty()) {
            throw new AndroidContainerConfigurationException(message);
        }
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
