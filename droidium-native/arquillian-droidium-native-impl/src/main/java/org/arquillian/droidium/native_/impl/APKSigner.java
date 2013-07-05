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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.api.Signer;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.configuration.Validate;
import org.arquillian.droidium.native_.utils.Command;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Implementation of the {@link Signer}. Signs APK files.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKSigner implements Signer {

    private static final Logger logger = Logger.getLogger(APKSigner.class.getName());

    private AndroidSDK androidSDK;

    private DroidiumNativeConfiguration droneConfiguration;

    private ProcessExecutor processExecutor;

    private AndroidApplicationHelper applicationHelper;

    /**
     *
     * @param androidSDK
     * @throws IllegalArgumentException when {@code androidSDK} is null
     */
    public APKSigner(ProcessExecutor processExecutor, AndroidSDK androidSDK, DroidiumNativeConfiguration droneConfiguration,
        AndroidApplicationHelper applicationHelper)
        throws IllegalArgumentException {
        Validate.notNull(processExecutor, "ProcessExecutor can't be null object to pass to APKSigner.");
        Validate.notNull(androidSDK, "AndroidSDK can't be null object to pass to APKSigner.");
        Validate.notNull(droneConfiguration, "Android drone configuration can't be null object to pass to APKSigner.");
        Validate.notNull(applicationHelper, "Andorid application helper can't be null object to pass to APKSigner");
        this.processExecutor = processExecutor;
        this.androidSDK = androidSDK;
        this.droneConfiguration = droneConfiguration;
        this.applicationHelper = applicationHelper;
    }

    @Override
    public void sign(File toSign, File signed) {

        checkKeyStore();

        Command jarSignerCommand = new Command();

        jarSignerCommand.add(androidSDK.getPathForJavaTool("jarsigner"))
            .add("-sigalg")
            .add("MD5withRSA")
            .add("-digestalg")
            .add("SHA1")
            .add("-signedjar")
            .add(signed.getAbsolutePath())
            .add("-storepass")
            .add(droneConfiguration.getStorepass())
            .add("-keystore")
            .add(droneConfiguration.getKeystore().getAbsolutePath())
            .add(toSign.getAbsolutePath())
            .add(droneConfiguration.getAlias());

        logger.log(Level.INFO, jarSignerCommand.toString());

        try {
            processExecutor.execute(jarSignerCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException e) {
            logger.log(Level.INFO, "Signing process was interrupted.");
            throw new RuntimeException(e.getMessage());
        } catch (ExecutionException e) {
            logger.log(Level.INFO, "Unable to sign package, signing process failed.");
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void resign(File toResign, File resigned) {
        Archive<?> apk = ShrinkWrap.createFromZipFile(JavaArchive.class, toResign);
        apk.delete("META-INF");
        applicationHelper.exportArchiveToFile(toResign, apk);
        sign(toResign, resigned);
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
        KeyStoreCreator keyStoreCreator = new KeyStoreCreator();
        if (!keyStoreCreator.keyStoreExists(droneConfiguration.getKeystore())) {
            File defaultKeyStore = new File(getDefaultKeyStorePath());
            if (!keyStoreCreator.keyStoreExists(defaultKeyStore)) {
                throw new AndroidExecutionException("Default keystore does not exist! No key store to use!");
            }
            droneConfiguration.setKeystore(defaultKeyStore);
        }
    }

    private String getDefaultKeyStorePath() {
        String separator = System.getProperty("file.separator");
        return System.getProperty("user.home") + separator + ".android" + separator + "debug.keystore";
    }

    /**
     * Creates keystore and checks if some keystore exists in the system.
     *
     * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>o
     *
     */
    private class KeyStoreCreator {

        /**
         * Checks if {@code keystore} exist.
         *
         * @param keyStore keystore to check existentiality of
         * @return true if {@code keystore} exists, false otherwise
         */
        public boolean keyStoreExists(File keyStore) {
            try {
                Validate.isReadable(keyStore,
                    "You must provide a valid path to keystore for signing of APK files: '"
                        + keyStore.getAbsolutePath() + ".");
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        /**
         * Creates keystore.
         *
         * @param keyStoreToCreate file which will be new keystore
         *
         * @deprecated do not use since there is a problem with not executing underlying keytool process
         */
        public void createKeyStore(File keyStoreToCreate) {

            Command createKeyStoreCommand = new Command();

            createKeyStoreCommand.add(androidSDK.getPathForJavaTool("keytool"))
                .add("-genkey")
                .add("-v")
                .add("-keystore")
                .add(keyStoreToCreate.getAbsolutePath())
                .add("-storepass")
                .add(droneConfiguration.getStorepass())
                .add("-alias")
                .add(droneConfiguration.getAlias())
                .add("-keypass")
                .add(droneConfiguration.getKeypass())
                .add("-dname")
                .addAsString("\"CN=Android Debug,O=Android,C=US\"")
                .add("-storetype")
                .add("JKS")
                .add("-sigalg")
                .add("MD5withRSA")
                .add("-keyalg")
                .add("RSA");

            logger.log(Level.INFO, createKeyStoreCommand.toString());

            try {
                processExecutor.execute(createKeyStoreCommand.getAsList().toArray(new String[0]));
            } catch (InterruptedException e) {
                logger.severe("Execution of keystore was interrupted.");
            } catch (ExecutionException e) {
                logger.severe("Unable to create keystore, execution exception occured.");
            }
        }
    }
}
