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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * @param selendroidWorkingCopy Selendroid working copy for making modifications
     *
     * @return modified Selendroid application which waits to be resigned
     */
    @SuppressWarnings("resource")
    public File rebuild(File selendroidWorkingCopy) {

        try {

            File toBeReplacedAndroidManifest = new File(workingDir, "AndroidManifestToBeReplaced.xml");
            File finalAndroidManifest = new File(workingDir, "AndroidManifest.xml");
            File dummyAPK = new File(workingDir, "dummy.apk");

            // ugly hack for copying AndroidManifest.xml from resources to tmpDir
            new FileOutputStream(toBeReplacedAndroidManifest.getAbsoluteFile())
                .write(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("AndroidManifest.xml")));

            filterManifestFile(toBeReplacedAndroidManifest, finalAndroidManifest);

            // create dummy package in order to get compiled AndroidManifest.xml

            createDummyAPK(dummyAPK, finalAndroidManifest);

            // extract AndroidManifest.xml from that dummy.apk package and add it to selendroid working copy
            Archive<?> dummyArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, dummyAPK);
            Archive<?> finalArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, selendroidWorkingCopy);

            finalArchive.delete("AndroidManifest.xml");
            finalArchive.add(dummyArchive.get("AndroidManifest.xml").getAsset(), "AndroidManifest.xml");

            File targetFile = new File(workingDir, AndroidApplicationHelper.getRandomAPKFileName());

            return androidApplicationHelper.exportArchiveToFile(targetFile, finalArchive);
        } catch (Exception ex) {
            return null;
        }
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
            logger.severe("Command was interrupted: " + createDummyPackage.toString());
        } catch (ExecutionException e) {
            logger.severe("Command failed to execute: " + createDummyPackage.toString());
        }
    }

    /**
     * Replaces AndroidManifest.xml file which contains default testapp base package to use the propper one
     *
     * @param toBeReplaced
     * @param finalManifest
     */
    @SuppressWarnings("unchecked")
    private void filterManifestFile(File toBeReplaced, File finalManifest) {
        try {
            List<String> oldManifest = FileUtils.readLines(toBeReplaced);
            List<String> finalManifestList = Replacer.replace(oldManifest,
                "org\\.openqa\\.selendroid\\.testapp", applicationBasePackage);
            FileUtils.writeLines(finalManifest, finalManifestList);
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
    }

    /**
     * Replaces strings in list of strings.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
     *
     */
    private static class Replacer {
        public static List<String> replace(List<String> fileLines, String toReplace, String replacement) {
            if (fileLines == null || fileLines.size() == 0) {
                return null;
            }

            List<String> afterReplace = new ArrayList<String>();
            for (String line : fileLines) {
                afterReplace.add(line.replaceAll(toReplace, replacement));
            }

            return afterReplace;
        }
    }
}