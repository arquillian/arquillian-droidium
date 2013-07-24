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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.utils.Command;

/**
 * Creates keystore and checks if some keystore exists in the system.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>o
 *
 */
public final class KeyStoreCreator {

    private static final Logger logger = Logger.getLogger(KeyStoreCreator.class.getName());

    private final AndroidSDK androidSDK;

    private final DroidiumNativeConfiguration configuration;

    public KeyStoreCreator(AndroidSDK androidSDK, DroidiumNativeConfiguration configuration) {
        Validate.notNull(androidSDK, "AndroidSDK for KeyStoreCreator can not be a null object!");
        Validate.notNull(configuration, "Droidium native configuration for KeyStoreCreator can not be a null object!");
        this.androidSDK = androidSDK;
        this.configuration = configuration;
    }

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
     */
    public void createKeyStore(File keyStoreToCreate) {

        Command createKeyStoreCommand = new Command();

        createKeyStoreCommand.add(androidSDK.getPathForJavaTool("keytool"))
            .add("-genkey")
            .add("-v")
            .add("-keystore")
            .add(keyStoreToCreate.getAbsolutePath())
            .add("-storepass")
            .add(configuration.getStorepass())
            .add("-alias")
            .add(configuration.getAlias())
            .add("-keypass")
            .add(configuration.getKeypass())
            .add("-dname")
            .addAsString("CN=Android,O=Android,C=US")
            .add("-storetype")
            .add("JKS")
            .add("-sigalg")
            .add("MD5withRSA")
            .add("-keyalg")
            .add("RSA");

        logger.log(Level.INFO, createKeyStoreCommand.toString());

        BufferedReader bufferedReader = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(createKeyStoreCommand.getAsList());
            Process process = builder.start();

            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (logger.isLoggable(Level.FINE)) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {

                } finally {
                    bufferedReader = null;
                }
            }
        }
    }
}