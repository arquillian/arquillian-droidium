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
package org.arquillian.droidium.native_.selendroid;

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
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.utils.Command;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Rebuilds Selendroid application. It takes raw Selendroid server AndroidManifest.xml located in resources. This manifest is
 * modified to reflect the target package which will be instrumented by it and new AndroidManifest.xml is compiled. After it,
 * new Selendroid server is built and it is ready to be resigned and installed into target device where it waits for the
 * execution of the instrumentation command.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class SelendroidRebuilder {

    private static final Logger logger = Logger.getLogger(SelendroidRebuilder.class.getName());

    private final ProcessExecutor processExecutor;

    private final AndroidSDK androidSDK;

    /**
     * Name of Selendroid server package.
     */
    public static final String SELENDROID_PACKAGE_NAME = "package=\"io.selendroid\"";

    /**
     * String in AndroidManifest.xml for Selendroid server to be replaced with target package of application under test.
     */
    public static final String SELENDROID_TEST_PACKAGE = "io.selendroid.testapp";

    /**
     * Needs to be deleted from AndroidManifest.xml since such resource is not present and aapt dump fails because of this.
     */
    public static final String ICON = "android:icon=\"@drawable/selenium_icon\"";

    /**
     *
     * @param processExecutor
     * @param androidSDK
     * @throws IllegalStateException if some argument is a null object
     */
    public SelendroidRebuilder(ProcessExecutor processExecutor, AndroidSDK androidSDK) {
        Validate.notNull(processExecutor, "Process exeuctor for Selendroid rebuilder can not be a null object!");
        Validate.notNull(androidSDK, "Android SDK for Selendroid rebuilder can not be null a null object!");
        this.processExecutor = processExecutor;
        this.androidSDK = androidSDK;
    }

    /**
     * Rebuilds Selendroid server
     *
     * @param selendroidWorkingCopy
     * @param selendroidPackageName package name of Selendroid server to use upon rebuilding
     * @param applicationBasePackage name of application base package to use upon rebuilding
     * @return rebuilt Selendroid server meant to be resigned
     * @throws IllegalArgumentException if {@code selendroidPackageName} is null object or if {@code selendroidPackageName} or
     *         {@code applicationBasePackage} is a null object or an empty string
     * @throws SelendroidRebuilderException if rebuilding fails
     */
    @SuppressWarnings("resource")
    public File rebuild(File selendroidWorkingCopy, String selendroidPackageName, String applicationBasePackage) {
        Validate.notNull(selendroidWorkingCopy, "Working copy of Selendroid server to rebuild can not be a null object!");
        Validate.notNullOrEmpty(selendroidPackageName,
            "Selendroid package name for rebuilding of Selendroid server can not be a null object nor an empty string!");
        Validate.notNullOrEmpty(applicationBasePackage,
            "Application base package for rebuilding of Selendroid server can not be a null object nor an empty string!");

        File toBeReplacedAndroidManifest = new File(DroidiumNativeFileUtils.getTmpDir(), "AndroidManifestToBeReplaced.xml");
        File finalAndroidManifest = new File(DroidiumNativeFileUtils.getTmpDir(), "AndroidManifest.xml");
        File dummyAPK = new File(DroidiumNativeFileUtils.getTmpDir(), "dummy.apk");

        // copying of AndroidManifest.xml from resources of the native plugin to working directory
        FileOutputStream toBeReplacedAndroidManifestStream;
        try {
            toBeReplacedAndroidManifestStream = new FileOutputStream(toBeReplacedAndroidManifest.getAbsoluteFile());
        } catch (FileNotFoundException ex) {
            throw new SelendroidRebuilderException();
        }

        InputStream androidManifestStream = this.getClass().getClassLoader().getResourceAsStream("AndroidManifest.xml");

        if (androidManifestStream == null) {
            throw new SelendroidRebuilderException("the class loader of " + this.getClass().getName() +
                " could not find AndroidManifest.xml resource");
        }

        try {
            toBeReplacedAndroidManifestStream.write(IOUtils.toByteArray(androidManifestStream));
        } catch (IOException ex) {
            throw new SelendroidRebuilderException("unable to write to " + toBeReplacedAndroidManifest.getAbsolutePath());
        }

        closeStream(toBeReplacedAndroidManifestStream);
        closeStream(androidManifestStream);

        filter(toBeReplacedAndroidManifest, finalAndroidManifest, selendroidPackageName, applicationBasePackage);

        // create dummy package in order to get compiled AndroidManifest.xml
        createDummyAPK(dummyAPK, finalAndroidManifest);

        // extract AndroidManifest.xml from that dummy.apk package and add it to Selendroid server working copy
        Archive<?> dummyArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, dummyAPK);
        Archive<?> finalArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, selendroidWorkingCopy);

        finalArchive.delete("AndroidManifest.xml");
        finalArchive.add(dummyArchive.get("AndroidManifest.xml").getAsset(), "AndroidManifest.xml");

        File targetFile = new File(DroidiumNativeFileUtils.getTmpDir(), DroidiumNativeFileUtils.getRandomAPKFileName());

        return DroidiumNativeFileUtils.export(finalArchive, targetFile);
    }

    /**
     * Creates dummy APK file in order to get compiled AndroidManifest.xml.
     *
     * @param dummyAPK APK to store dummy APK to
     * @param androidManifest AndroidManifest.xml to be used while creating dummy APK
     * @throws SelendroidRebuilderException when creating of dummy APK fails
     */
    private void createDummyAPK(File dummyAPK, File androidManifest) {
        Command createDummyPackage = new Command();
        createDummyPackage.add(androidSDK.getAaptPath())
            .add("package")
            .add("-f")
            .add("-M")
            .add(androidManifest.getAbsolutePath())
            .add("-I")
            .add(androidSDK.getPlatform() + "/android.jar")
            .add("-F")
            .add(dummyAPK.getAbsolutePath());

        try {
            processExecutor.execute(createDummyPackage.getAsArray());
        } catch (InterruptedException e) {
            throw new SelendroidRebuilderException("Command was interrupted: " + createDummyPackage.toString());
        } catch (ExecutionException e) {
            throw new SelendroidRebuilderException("Command failed to execute: " + createDummyPackage.toString());
        }
    }

    /**
     * Filters {@code toBeReplaced} manifest and writes it, after filtering, to {@code finalManifest}.
     *
     * @param toBeReplaced manifest file to be filtered
     * @param finalManifest manifest file after the filtering
     * @param selendroidPackageName Selendroid package name to set in {@code finalManifest} instead of the old one
     * @param applicationBasePackage application base package to set in {@code finalManifest} instead of the old one
     * @throws SelendroidRebuilderException
     */
    private void filter(File toBeReplaced, File finalManifest, String selendroidPackageName, String applicationBasePackage) {
        try {
            ManifestFilter filter = new ManifestFilter(FileUtils.readLines(toBeReplaced));

            filter.filter(SELENDROID_PACKAGE_NAME, "package=\"" + selendroidPackageName + "\"")
                .filter(SELENDROID_TEST_PACKAGE, applicationBasePackage)
                .filter(ICON, "");

            if (logger.isLoggable(Level.FINE))
            for (String line : filter.getFiltered()) {
                System.out.println(line);
            }

            FileUtils.writeLines(finalManifest, filter.getFiltered());
        } catch (IOException e) {
            throw new SelendroidRebuilderException("Unable to filter Android manifest. Tried to filter "
                + toBeReplaced.getAbsolutePath() + " into " + finalManifest.getAbsolutePath() + ".");
        }
    }

    /**
     * Closes a stream
     *
     * @param stream stream to be closed
     */
    private void closeStream(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ignore) {
            // ignore
        } finally {
            stream = null;
        }
    }

    /**
     * Wrapper around the filtering.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    protected class ManifestFilter {

        private List<String> manifest;

        public ManifestFilter(List<String> manifest) {
            this.manifest = manifest;
        }

        public ManifestFilter filter(String toReplace, String replacement) {
            manifest = Replacer.replace(manifest, toReplace, replacement);
            return this;
        }

        public List<String> getFiltered() {
            return this.manifest;
        }
    }

    /**
     * Replaces strings in a list of strings.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    protected static class Replacer {

        /**
         * @param lines lines to filter for the replacement
         * @param toReplace string to replace
         * @param replacement string to replace {@code toReplace} with
         * @return filtered {@code fileLines} after replacement
         * @throws IllegalArgumentException if {@code toReplace} is null or empty, or if {@code replacement} is null, or if list
         *         to be filtered is null
         */
        public static List<String> replace(List<String> lines, String toReplace, String replacement) {
            Validate.notNullOrEmpty(toReplace, "The string to be replaced can not be a null object nor an empty string!");
            Validate.notNull(replacement, "The string as a replacement can not be a null object!");
            Validate.notNull(lines, "The list to be filtered for the replacement of '"
                + toReplace + "' for '" + replacement + "' can not be a null object!");

            if (lines.size() == 0) {
                return null;
            }

            List<String> filtered = new ArrayList<String>();
            for (String line : lines) {
                filtered.add(line.replaceAll(toReplace, replacement));
            }

            return filtered;
        }
    }

}