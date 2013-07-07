/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.native_.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.utils.APKIdentifierGenerator;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.IdentifierType;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Provides various helper methods for Android packages.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidApplicationHelper {

    private static final Logger logger = Logger.getLogger(AndroidApplicationHelper.class.getName());

    private ProcessExecutor processExecutor;

    private AndroidSDK androidSDK;

    /**
     *
     * @param processExecutor
     * @param androidSDK
     */
    public AndroidApplicationHelper(ProcessExecutor processExecutor, AndroidSDK androidSDK) {
        this.processExecutor = processExecutor;
        this.androidSDK = androidSDK;
    }

    /**
     *
     * @param apk apk file to get the name of main activity from
     * @return name of main activity of application
     */
    public String getApplicationMainActivity(File apk) {
        try {
            List<String> results = processExecutor.execute(getAAPTBadgingCommand(apk).getAsList().toArray(new String[0]));
            return parseProperty(results, "launchable-activity");
        } catch (InterruptedException e) {
            logger.severe("Process to get name of main application activity was interrupted.");
            return null;
        } catch (ExecutionException e) {
            logger.severe("Execution exception while getting name of main application activity occured.");
            return null;
        }
    }

    /**
     *
     * @param apk apk file to get the name of main activity from
     * @return name of main activity of application
     */
    public String getApplicationMainActivity(String apk) {
        return getApplicationMainActivity(new File(apk));
    }

    /**
     *
     * @param apk apk file to get the name of application base package of
     * @return name of application base package
     */
    public String getApplicationBasePackage(File apk) {
        try {
            List<String> results = processExecutor.execute(getAAPTBadgingCommand(apk).getAsList().toArray(new String[0]));
            return parseProperty(results, "package");
        } catch (InterruptedException e) {
            logger.severe("Process to get name of base application package was interrupted.");
            return null;
        } catch (ExecutionException e) {
            logger.severe("Execution exception while getting name of base application package occured");
            return null;
        }
    }

    /**
     *
     * @param apk apk file to get the name of application base package of
     * @return name of application base package
     */
    public String getApplicationBasePackage(String apk) {
        return getApplicationBasePackage(new File(apk));
    }

    /**
     * Exports archive to file
     *
     * @param target where to export {@code archive}
     * @param archive archive to export
     * @return file to where {@code archive} was exported
     */
    public File exportArchiveToFile(File target, Archive<?> archive) {
        if (target.exists() && target.isFile()) {
            try {
                if (!target.delete()) {
                    logger.info("Unable to delete target file for Archive<?> to export into.");
                }
            } catch (SecurityException ex) {
                logger.info(ex.getMessage());
            }
        }

        final OutputStream out;
        final InputStream in;

        try {
            out = new FileOutputStream(target);
            in = archive.as(ZipExporter.class).exportAsInputStream();
            write(in, out);
            closeStream(in);
            closeStream(out);
            return target;
        } catch (final FileNotFoundException ex) {
            logger.severe(ex.getMessage());
        } catch (final IOException ex) {
            logger.severe(ex.getMessage());
        }
        return null;
    }

    /**
     * @return random name for APK file name (ends with .apk suffix)
     */
    public static String getRandomAPKFileName() {
        return new APKIdentifierGenerator().getIdentifier(IdentifierType.APK.getClass());
    }

    /**
     * @param apk
     * @return command which dumps badging from {@code apk}
     */
    private Command getAAPTBadgingCommand(File apk) {
        Command command = new Command();
        command.add(androidSDK.getAaptPath())
            .add("dump")
            .add("badging")
            .add(apk.getAbsolutePath());
        logger.log(Level.INFO, command.toString());
        return command;
    }

    /**
     * Parses property value from list of lines, returns the first parsed result. Property to parse is somewhere at line in form
     * {@code property .... name=stringToGet}
     *
     * @param lines list of lines to get property value from
     * @param property property to get, is situated at the beginning of line
     * @return stringToGet
     */
    private String parseProperty(List<String> lines, String property) {
        if (lines == null || lines.size() == 0) {
            return null;
        }

        Pattern packagePattern = Pattern.compile("name=[\'\"]([^\'\"]+)[\'\"]");
        Matcher m;

        for (String line : lines) {
            if (line.contains(property)) {
                m = packagePattern.matcher(line);
                while (m.find()) {
                    return m.group(1);
                }
            }
        }
        return null;
    }

    /**
     * Writes {@code input} to {@code output}
     *
     * @param input
     * @param output
     * @throws IOException
     */
    private void write(InputStream input, OutputStream output) throws IOException {
        Validate.notNull(input, "InputStream to read from can not be null object!");
        Validate.notNull(output, "OutputStream to write to can not be null object!");

        int read = 0;
        byte[] bytes = new byte[1024];

        while ((read = input.read(bytes)) != -1) {
            output.write(bytes, 0, read);
        }

        output.flush();
    }

    /**
     * Closes a stream.
     *
     * @param stream stream to close
     */
    private void closeStream(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (final IOException ignore) {
            // ignore
        } finally {
            stream = null;
        }
    }
}
