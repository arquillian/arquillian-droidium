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
package org.arquillian.droidium.container.sign;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Signs and resigns arbitrary APK files.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKSigner {

    private static final Logger logger = Logger.getLogger(APKSigner.class.getName());

    private final AndroidSDK sdk;

    private final ProcessExecutor executor;

    /**
     *
     * @param executor
     * @param sdk
     * @throws IllegalArgumentException when some of arguments is a null object
     */
    public APKSigner(ProcessExecutor executor, AndroidSDK sdk)
        throws IllegalArgumentException {
        Validate.notNull(executor, "Process executor to set can not be a null object!");
        Validate.notNull(sdk, "Android SDK to set can not be a null object!");
        this.executor = executor;
        this.sdk = sdk;
    }

    /**
     *
     * @param toSign file to sign
     * @param signed file after the signing
     * @return {@code signed} file
     */
    public File sign(File toSign, File signed) {

        checkKeyStore();

        Command jarSignerCommand = new CommandBuilder()
            .add(sdk.getPathForJavaTool("jarsigner"))
            .add("-sigalg").add("MD5withRSA")
            .add("-digestalg").add("SHA1")
            .add("-signedjar").add(signed.getAbsolutePath())
            .add("-storepass").add(sdk.getPlatformConfiguration().getStorepass())
            .add("-keystore").add(new File(sdk.getPlatformConfiguration().getKeystore()).getAbsolutePath())
            .add(toSign.getAbsolutePath())
            .add(sdk.getPlatformConfiguration().getAlias())
            .build();

        logger.log(Level.FINE, jarSignerCommand.toString());

        try {
            executor.execute(jarSignerCommand);
        } catch (AndroidExecutionException e) {
            throw new APKSignerException("Unable to sign package, signing process failed.", e);
        }

        return signed;
    }

    /**
     *
     * @param toResign file to resign
     * @return resigned file
     */
    public File resign(File toResign) {
        Validate.notNull(toResign, "File to resign can not be a null object!");
        Archive<?> apk = ShrinkWrap.createFromZipFile(JavaArchive.class, toResign);
        apk.delete("META-INF");
        File toSign = new File(sdk.getPlatformConfiguration().getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());
        DroidiumFileUtils.export(apk, toSign);
        return sign(toSign, new File(sdk.getPlatformConfiguration().getTmpDir(), DroidiumFileUtils.getRandomAPKFileName()));
    }

    /**
     * Checks if keystore as specified in {@link DroidiumPlatformConfiguration} exists.
     *
     * If it does not exist, checks if default keystore (in $ANDROID_SDK_HOME/.android/debug.keystore) exist. If default
     * keystore does not exist, this method creates it.
     *
     * Sets keystore back to {@link DroidiumPlatformConfiguration} to whatever exist first.
     */
    private void checkKeyStore() {
        KeyStoreCreator keyStoreCreator = new KeyStoreCreator(executor, sdk);
        if (!keyStoreCreator.keyStoreExists(new File(sdk.getPlatformConfiguration().getKeystore()))) {
            File defaultKeyStore = new File(getDefaultKeyStorePath());
            if (!keyStoreCreator.keyStoreExists(defaultKeyStore)) {
                keyStoreCreator.createKeyStore(defaultKeyStore);
            }
            sdk.getPlatformConfiguration().setProperty("keystore", defaultKeyStore.getAbsolutePath());
        }
    }

    private String getDefaultKeyStorePath() {
        String separator = System.getProperty("file.separator");
        return sdk.getPlatformConfiguration().getAndroidSdkHome() + ".android" + separator + "debug.keystore";
    }
}