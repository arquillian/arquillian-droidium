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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.configuration.Validate;
import org.arquillian.droidium.native_.exception.SelendroidRebuilderException;
import org.arquillian.droidium.native_.utils.Command;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Rebuilds Selendroid application.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class SelendroidRebuilder {

    private static final Logger logger = Logger.getLogger(SelendroidRebuilder.class.getName());

    private File workingDir = null;

    private ProcessExecutor processExecutor;

    private AndroidSDK androidSDK;

    private AndroidApplicationHelper androidApplicationHelper;

    private String applicationBasePackage;

    /**
     * String in AndroidManifest.xml for Selendroid server to be replaced with target package of application under test
     */
    private static final String selendroidPackageString = "io.selendroid.testapp";

    /**
     * Needs to be deleted from AndroidManifest.xml since such resource is not present and aapt dump fails because of this
     */
    private static final String ICON = "android:icon=\"@drawable/selenium_icon\"";

    /**
     *
     * @param processExecutor
     * @param androidSDK
     * @param droidiumNativeConfiguration
     * @param androidApplicationHelper
     * @param workingDir directory where all files and resources this class is working with are put
     */
    public SelendroidRebuilder(ProcessExecutor processExecutor,
        AndroidSDK androidSDK,
        DroidiumNativeConfiguration droidiumNativeConfiguration,
        AndroidApplicationHelper androidApplicationHelper,
        File workingDir) {
        Validate.notNull(processExecutor, "Process exeuctor for Selendroid rebuilder can't be null!");
        Validate.notNull(androidSDK, "Android SDK for Selendroid rebuilder can't be null!");
        Validate.notNull(droidiumNativeConfiguration, "Android drone configuration for Selendroid rebuilder can't be null!");
        Validate.notNull(androidApplicationHelper, "Android applicatio helper for Selendroid rebuilder can't be null!");
        Validate.notNull(workingDir, "Working dir for Selendroid rebuilder can't be null!. Set it e.g. to /tmp/ ...");
        this.processExecutor = processExecutor;
        this.androidSDK = androidSDK;
        this.androidApplicationHelper = androidApplicationHelper;
        this.workingDir = workingDir;
    }

    /**
     * Rebuilds Selendroid package.
     *
     * Changes AndroidManifest.xml to reflect application under test and repackages it.
     *
     * @param selendroidWorkingCopy Selendroid working copy for AndroidManifest.xml substitution
     *
     * @return modified Selendroid application to be resigned
     */
    @SuppressWarnings("resource")
    public File rebuild(File selendroidWorkingCopy) {

        File toBeReplacedAndroidManifest = new File(workingDir, "AndroidManifestToBeReplaced.xml");
        File finalAndroidManifest = new File(workingDir, "AndroidManifest.xml");
        File dummyAPK = new File(workingDir, "dummy.apk");

        // copying of AndroidManifest.xml from resources of the native plugin to working directory
        FileOutputStream toBeReplacedAndroidManifestStream;
        try {
            toBeReplacedAndroidManifestStream = new FileOutputStream(toBeReplacedAndroidManifest.getAbsoluteFile());
        } catch (FileNotFoundException ex) {
            throw new SelendroidRebuilderException();
        }

        InputStream AndroidManifestStream = this.getClass().getClassLoader().getResourceAsStream("AndroidManifest.xml");

        if (AndroidManifestStream == null) {
            throw new SelendroidRebuilderException("class loader of " + this.getClass().getName() +
                " could not find AndroidManifest.xml");
        }

        try {
            toBeReplacedAndroidManifestStream.write(IOUtils.toByteArray(AndroidManifestStream));
        } catch (IOException ex) {
            throw new SelendroidRebuilderException("unable to write to " + toBeReplacedAndroidManifest.getAbsolutePath());
        }

        closeStream(toBeReplacedAndroidManifestStream);
        closeStream(AndroidManifestStream);

        filterManifestFile(toBeReplacedAndroidManifest, finalAndroidManifest);

        // create dummy package in order to get compiled AndroidManifest.xml
        createDummyAPK(dummyAPK, finalAndroidManifest);

        // extract AndroidManifest.xml from that dummy.apk package and add it to Selendroid server working copy
        Archive<?> dummyArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, dummyAPK);
        Archive<?> finalArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, selendroidWorkingCopy);

        finalArchive.delete("AndroidManifest.xml");
        finalArchive.add(dummyArchive.get("AndroidManifest.xml").getAsset(), "AndroidManifest.xml");

        File targetFile = new File(workingDir, AndroidApplicationHelper.getRandomAPKFileName());

        return androidApplicationHelper.exportArchiveToFile(targetFile, finalArchive);
    }

    /**
     *
     * @param applicationBasePackage
     */
    public void setApplicationBasePackage(String applicationBasePackage) {
        this.applicationBasePackage = applicationBasePackage;
    }

    /**
     * Creates dummy APK file to get compiler AndroidManifest.xml
     *
     * @param dummyAPK APK to store dummy APK to
     * @param androidManifest AndroidManifest.xml to be used while creating dummy APK
     */
    private void createDummyAPK(File dummyAPK, File androidManifest) {
        Command createDummyPackage = new Command();
        createDummyPackage.add(androidSDK.getAaptPath())
            .add("package")
            .add("-M")
            .add(androidManifest.getAbsolutePath())
            .add("-I")
            .add(androidSDK.getPlatform() + "/android.jar")
            .add("-F")
            .add(dummyAPK.getAbsolutePath());

        logger.log(Level.INFO, createDummyPackage.toString());

        try {
            processExecutor.execute(createDummyPackage.getAsList().toArray(new String[0]));
        } catch (InterruptedException e) {
            throw new SelendroidRebuilderException("Command was interrupted: " + createDummyPackage.toString());
        } catch (ExecutionException e) {
            throw new SelendroidRebuilderException("Command failed to execute: " + createDummyPackage.toString());
        }
    }

    /**
     * Replaces AndroidManifest.xml file which contains default test application base package to use the proper one
     *
     * @param toBeReplaced
     * @param finalManifest
     */
    @SuppressWarnings("unchecked")
    private void filterManifestFile(File toBeReplaced, File finalManifest) {
        try {
            List<String> manifest = FileUtils.readLines(toBeReplaced);
            List<String> packageReplacement = Replacer.replace(manifest, selendroidPackageString, applicationBasePackage);
            List<String> iconReplacement = Replacer.replace(packageReplacement, ICON, "");
            FileUtils.writeLines(finalManifest, iconReplacement);
        } catch (IOException e) {
            throw new SelendroidRebuilderException("unable to filter Android manifest file for string substitutions "
                + "of target package and icon.");
        }
    }

    /**
     * Replaces strings in list of strings.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    private static class Replacer {

        /**
         * @param lines lines to filter for the replacement
         * @param toReplace string to replace
         * @param replacement string to replace {@code toReplace} with
         * @return filtered {@code fileLines} after replacement
         * @throws IllegalArgumentException if {@code toReplace} is null or empty, or if {@code replacement} is null, or if list
         *         to be filtered is null
         */
        public static List<String> replace(List<String> lines, String toReplace, String replacement) {
            Validate.notNullOrEmpty(toReplace, "String to replace can not be null object nor empty string!");
            Validate.notNull(replacement, "String as a replacement can not be null object.");

            Validate.notNull(lines, "List to be filtered for replacement of '"
                + toReplace + "' for '" + replacement + "' can't be null object!");

            if (lines.size() == 0) {
                return null;
            }

            List<String> afterFilter = new ArrayList<String>();
            for (String line : lines) {
                afterFilter.add(line.replaceAll(toReplace, replacement));
            }

            return afterFilter;
        }
    }

    /**
     * Closes a stream
     *
     * @param stream stream to be closed
     */
    private void closeStream(Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ignore) {

        }
    }
}