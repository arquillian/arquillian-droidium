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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.native_.exception.SelendroidRebuilderException;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessResult;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.task.io.FileReader;
import org.arquillian.spacelift.tool.basic.StringReplacementTool;
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

    private final AndroidSDK androidSDK;

    // that are replaced in AndroidManifest.xml template taken from Selendroid project
    private static final String SELENDROID_VERSION_KEY = "${selendroidVersion}";
    private static final String MAIN_PACKAGE_KEY = "${mainPackage}";
    private static final String SERVER_INSTRUMENTATION_CLASSNAME_KEY = "${instrumentationClassName}";
    private static final String LIGHTWEIGHT_INSTRUMENTATION_CLASSNAME_KEY = "${lwInstrumentationClassName}";
    private static final String TARGET_PACKAGE_KEY = "${targetPackage}";

    /**
     *
     * @param androidSDK
     * @throws IllegalStateException if some argument is a null object
     */
    public SelendroidRebuilder(AndroidSDK androidSDK) {
        Validate.notNull(androidSDK, "Android SDK for Selendroid rebuilder can not be null a null object!");
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
    public File rebuild(File selendroidWorkingCopy, String selendroidPackageName, String modifiedSelendroidPackageName, String applicationBasePackage, String selendroidVersion) {
        Validate.notNull(selendroidWorkingCopy, "Working copy of Selendroid server to rebuild can not be a null object!");
        Validate.notNullOrEmpty(selendroidPackageName,
            "Selendroid package name for rebuilding of Selendroid server can not be a null object nor an empty string!");
        Validate.notNullOrEmpty(modifiedSelendroidPackageName,
            "Selendroid package name for rebuilding of Selendroid server can not be a null object nor an empty string!");
        Validate.notNullOrEmpty(applicationBasePackage,
            "Application base package for rebuilding of Selendroid server can not be a null object nor an empty string!");
        Validate.notNull(applicationBasePackage,
            "Selendroid version for rebuilding of Selendroid server can not be a null object!");


        final File tmpDir = androidSDK.getPlatformConfiguration().getTmpDir();

        File finalAndroidManifest = new File(tmpDir, "AndroidManifest.xml");
        File dummyAPK = new File(tmpDir, "dummy.apk");

        // copying of AndroidManifest.xml from resources of the native plugin to working directory
        FileOutputStream finalAndroidManifestStream;
        try {
            finalAndroidManifestStream = new FileOutputStream(finalAndroidManifest.getAbsoluteFile());
        } catch (FileNotFoundException ex) {
            throw new SelendroidRebuilderException();
        }

        InputStream androidManifestStream = this.getClass().getClassLoader().getResourceAsStream("AndroidManifest.xml");

        if (androidManifestStream == null) {
            throw new SelendroidRebuilderException("the class loader of " + this.getClass().getName()
                + " could not find AndroidManifest.xml resource");
        }

        try {
            finalAndroidManifestStream.write(IOUtils.toByteArray(androidManifestStream));
        } catch (IOException ex) {
            throw new SelendroidRebuilderException("unable to write to " + finalAndroidManifest.getAbsolutePath());
        }

        closeStream(finalAndroidManifestStream);
        closeStream(androidManifestStream);

        Map<String, String> replacementMapping = new HashMap<String, String>(4);
        replacementMapping.put(Pattern.quote(SELENDROID_VERSION_KEY), selendroidVersion);
        replacementMapping.put(Pattern.quote(MAIN_PACKAGE_KEY), modifiedSelendroidPackageName);
        replacementMapping.put(Pattern.quote(TARGET_PACKAGE_KEY), applicationBasePackage);
        replacementMapping.put(Pattern.quote(SERVER_INSTRUMENTATION_CLASSNAME_KEY), selendroidPackageName + ".ServerInstrumentation");
        replacementMapping.put(Pattern.quote(LIGHTWEIGHT_INSTRUMENTATION_CLASSNAME_KEY), selendroidPackageName + ".LightweightInstrumentation");

        modifyManifest(finalAndroidManifest, replacementMapping);

        // create dummy package in order to get compiled AndroidManifest.xml
        createDummyAPK(dummyAPK, finalAndroidManifest);

        // extract AndroidManifest.xml from that dummy.apk package and add it to Selendroid server working copy
        Archive<?> dummyArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, dummyAPK);
        Archive<?> finalArchive = ShrinkWrap.createFromZipFile(JavaArchive.class, selendroidWorkingCopy);

        finalArchive.delete("AndroidManifest.xml");
        finalArchive.add(dummyArchive.get("AndroidManifest.xml").getAsset(), "AndroidManifest.xml");

        File targetFile = new File(androidSDK.getPlatformConfiguration().getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());

        return DroidiumFileUtils.export(finalArchive, targetFile);
    }

    /**
     * Creates dummy APK file in order to get compiled AndroidManifest.xml.
     *
     * @param dummyAPK APK to store dummy APK to
     * @param androidManifest AndroidManifest.xml to be used while creating dummy APK
     * @throws SelendroidRebuilderException when creating of dummy APK fails
     */
    private void createDummyAPK(File dummyAPK, File androidManifest) {
        Command createDummyPackage = new CommandBuilder(androidSDK.getAaptPath())
            .parameter("package")
            .parameter("-f")
            .parameter("-M")
            .parameter(androidManifest.getAbsolutePath())
            .parameter("-I")
            .parameter(new File(androidSDK.getPlatformDirectory(), "android.jar").getAbsolutePath())
            .parameter("-F")
            .parameter(dummyAPK.getAbsolutePath())
            .build();

        ProcessResult processResult = Tasks.prepare(CommandTool.class).command(createDummyPackage).execute().await();

        if (processResult != null && processResult.exitValue() != 0) {
            throw new SelendroidRebuilderException("Command failed to execute: "
                + createDummyPackage.toString() + "with output " + processResult.output());
        }
    }

    /**
     * Modifies original manifest file by replacing {@code ${}} occurences in the templace
     *
     * @return modified file
     */
    private File modifyManifest(File manifest, Map<String, String> replacementMapping) {
        StringReplacementTool sed = Tasks.prepare(StringReplacementTool.class).in(manifest);
        for(Map.Entry<String, String> replacement:replacementMapping.entrySet()) {
            sed.replace(replacement.getKey()).with(replacement.getValue());
        }

        List<File> newManifest = sed.execute().await();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, Tasks.chain(newManifest, FileReader.class).execute().await().entrySet().iterator().next().getValue());
        }

        return newManifest.iterator().next();
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

}