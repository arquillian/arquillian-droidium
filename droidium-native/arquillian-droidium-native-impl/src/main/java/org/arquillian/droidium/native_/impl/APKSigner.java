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

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.native_.api.Signer;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.exception.APKSignerException;
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

    private final AndroidSDK androidSDK;

    private final DroidiumNativeConfiguration configuration;

    private final ProcessExecutor processExecutor;

    private final AndroidApplicationHelper applicationHelper;

    /**
     *
     * @param processExecutor
     * @param androidSDK
     * @param configuration
     * @param applicationHelper
     * @throws IllegalArgumentException when some of arguments is null
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
        this.configuration = droneConfiguration;
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
            .add(configuration.getStorepass())
            .add("-keystore")
            .add(configuration.getKeystore().getAbsolutePath())
            .add(toSign.getAbsolutePath())
            .add(configuration.getAlias());

        logger.log(Level.INFO, jarSignerCommand.toString());

        try {
            processExecutor.execute(jarSignerCommand.getAsList().toArray(new String[0]));
        } catch (InterruptedException ex) {
            throw new APKSignerException("Signing process was interrupted.", ex);
        } catch (ExecutionException ex) {
            throw new APKSignerException("Unable to sign package, signing process failed.", ex);
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
        KeyStoreCreator keyStoreCreator = new KeyStoreCreator(androidSDK, configuration);
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
