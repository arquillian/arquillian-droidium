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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;

/**
 * Creates keystore and checks if some keystore already exists in the system.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public final class KeyStoreCreator {

    private static final Logger logger = Logger.getLogger(KeyStoreCreator.class.getName());

    private final AndroidSDK sdk;

    private final DroidiumNativeConfiguration configuration;

    /**
     *
     * @param sdk
     * @param configuration
     * @throws IllegalArgumentException if either {@code sdk} or {@code configuration} is a null object
     */
    public KeyStoreCreator(AndroidSDK sdk, DroidiumNativeConfiguration configuration) {
        Validate.notNull(sdk, "Android sdk for key store creator can not be a null object!");
        Validate.notNull(configuration, "Droidium native configuration for key store creator can not be a null object!");
        this.sdk = sdk;
        this.configuration = configuration;
    }

    /**
     * Checks if {@code keystore} exists.
     *
     * @param keyStore keystore to check the existentiality of
     * @return true if {@code keystore} exists, false otherwise
     */
    public boolean keyStoreExists(File keyStore) {
        try {
            Validate.isReadable(keyStore,
                "You must provide a valid path to keystore for signing of APK files: '" + keyStore.getAbsolutePath() + ".");
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Creates keystore. You can change the {@code -keypass} argument of keytool command by setting {@code keypass} property
     * in arquillian.xml. You can change {@code -sigalg} and {@code -keyalg} and {@code -storepass} as well by the same way.
     * Storetype is {@code JKS} and {@code -dname} is {@code CN=Android,O=Android,C=US}.
     *
     * @param keyStoreToCreate file which will be new keystore
     */
    public void createKeyStore(File keyStoreToCreate) {

        Command createKeyStoreCommand = new Command();

        createKeyStoreCommand.add(sdk.getPathForJavaTool("keytool"))
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
            .add(configuration.getSigalg())
            .add("-keyalg")
            .add(configuration.getKeyalg());

        logger.log(Level.INFO, createKeyStoreCommand.toString());

        // it seems ProcessExecutor hangs here for some reason, I was not able to execute keytool command with it.
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