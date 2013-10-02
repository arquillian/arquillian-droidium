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
package org.arquillian.droidium.native_.sign;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.utils.DroidiumNativeFileUtils;
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

    private final DroidiumNativeConfiguration configuration;

    private final ProcessExecutor executor;

    /**
     *
     * @param executor
     * @param sdk
     * @param configuration
     * @throws IllegalArgumentException when some of arguments is a null object
     */
    public APKSigner(ProcessExecutor executor, AndroidSDK sdk, DroidiumNativeConfiguration configuration)
        throws IllegalArgumentException {
        Validate.notNull(executor, "Process executor to set can not be a null object!");
        Validate.notNull(sdk, "Android SDK to set can not be a null object!");
        Validate.notNull(configuration, "Droidium native configuration to set can not be a null object!");
        this.executor = executor;
        this.sdk = sdk;
        this.configuration = configuration;
    }

    /**
     *
     * @param toSign file to sign
     * @param signed file after the signing
     * @return {@code signed} file
     */
    public File sign(File toSign, File signed) {

        checkKeyStore();

        Command jarSignerCommand = new Command();

        jarSignerCommand.add(sdk.getPathForJavaTool("jarsigner"))
            .add("-sigalg")
            .add("MD5withRSA")
            .add("-digestalg")
            .add("SHA1")
            .add("-signedjar")
            .add(signed.getAbsolutePath())
            .add("-storepass")
            .add(configuration.getStorepass())
            .add("-keystore")
            .add(configuration.getKeystore().getAbsolutePath())
            .add(toSign.getAbsolutePath())
            .add(configuration.getAlias());

        logger.log(Level.FINE, jarSignerCommand.toString());


        try {
            executor.execute(jarSignerCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException ex) {
            throw new APKSignerException("Signing process was interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new APKSignerException("Unable to sign package, signing process failed.", ex);
        }

        return signed;
    }

    /**
     *
     * @param toResign file to resign
     * @param resigned
     * @return {@code resigned} file
     */
    public File resign(File toResign, File resigned) {
        Archive<?> apk = ShrinkWrap.createFromZipFile(JavaArchive.class, toResign);
        apk.delete("META-INF");
        DroidiumNativeFileUtils.export(apk, toResign);
        return sign(toResign, resigned);
    }

    /**
     * Checks if keystore as specified in {@link DroidiumNativeConfiguration} exists.
     *
     * If it does not exist, checks if default keystore (in $HOME/.android/debug.keystore) exist. If default keystore does not
     * exist, creates it.
     *
     * Sets keystore back to {@link DroidiumNativeConfiguration} to whatever exist first.
     */
    private void checkKeyStore() {
        KeyStoreCreator keyStoreCreator = new KeyStoreCreator(sdk, configuration);
        if (!keyStoreCreator.keyStoreExists(configuration.getKeystore())) {
            File defaultKeyStore = new File(getDefaultKeyStorePath());
            if (!keyStoreCreator.keyStoreExists(defaultKeyStore)) {
                keyStoreCreator.createKeyStore(defaultKeyStore);
            }
            configuration.setProperty("keyStore", defaultKeyStore.getAbsolutePath());
        }
    }

    private String getDefaultKeyStorePath() {
        String separator = System.getProperty("file.separator");
        return System.getProperty("user.home") + separator + ".android" + separator + "debug.keystore";
    }
}
