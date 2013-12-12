/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.arquillian.droidium.container.impl;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.execution.ProcessExecutor;
import org.arquillian.droidium.container.execution.ProcessInteractionBuilder;
import org.arquillian.droidium.container.spi.event.AndroidSDCardCreate;
import org.arquillian.droidium.container.spi.event.AndroidSDCardDelete;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceCreate;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDelete;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDeleted;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Deletes and creates Android virtual devices and initiates deletion of SD card as well.
 *
 * Observes:
 * <ul>
 * <li>{@link AndroidVirtualDeviceDelete}</li>
 * <li>{@link AndroidVirtualDeviceCreate}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidVirtualDeviceAvailable}</li>
 * <li>{@link AndroidVirtualDeviceDeleted}</li>
 * <li>{@link AndroidSDCardDelete}</li>
 * <li>{@link AndroidSDCardCreate}</li>
 * </ul>
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidVirtualDeviceManager {

    private static final Logger logger = Logger.getLogger(AndroidVirtualDeviceManager.class.getName());

    @Inject
    private Instance<AndroidContainerConfiguration> configuration;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<ProcessExecutor> executor;

    @Inject
    private Event<AndroidVirtualDeviceAvailable> androidVirtualDeviceAvailable;

    @Inject
    private Event<AndroidVirtualDeviceDeleted> androidVirtualDeviceDeleted;

    @Inject
    private Event<AndroidSDCardDelete> androidSDCardDelete;

    @Inject
    private Event<AndroidSDCardCreate> androidSDCardCreate;

    public void deleteAndroidVirtualDevice(@Observes AndroidVirtualDeviceDelete event) {

        AndroidContainerConfiguration configuration = this.configuration.get();

        try {
            ProcessExecutor executor = this.executor.get();
            String avdName = configuration.getAvdName();
            Command command = new Command();
            command.add(androidSDK.get().getAndroidPath())
                .add("delete")
                .add("avd")
                .add("-n")
                .add(avdName);
            executor.execute(command.getAsArray());

            logger.log(Level.INFO, "Android Virtual Device {0} deleted.", avdName);

        } catch (AndroidExecutionException ex) {
            logger.log(Level.WARNING, "Unable to delete Android Virtual Device " + configuration.getAvdName(), ex);
        }

        androidSDCardDelete.fire(new AndroidSDCardDelete());
        androidVirtualDeviceDeleted.fire(new AndroidVirtualDeviceDeleted(configuration.getAvdName()));
    }

    public void createAndroidVirtualDevice(@Observes AndroidVirtualDeviceCreate event) throws AndroidExecutionException {
        Validate.notNulls(new Object[] { configuration.get(), androidSDK.get() },
            "container configuration injection or Android SDK injection is null");

        androidSDCardCreate.fire(new AndroidSDCardCreate());

        AndroidContainerConfiguration configuration = this.configuration.get();
        AndroidSDK sdk = this.androidSDK.get();

        ProcessExecutor executor = this.executor.get();

        try {
            Command command = new Command();
            command.add(sdk.getAndroidPath())
                .add("create")
                .add("avd")
                .add("-n")
                .add(configuration.getAvdName())
                .add("-t")
                .add("android-" + configuration.getApiLevel())
                .add("-b")
                .add(configuration.getAbi())
                .add("-f");

            if (configuration.getSdCard() != null && new File(configuration.getSdCard()).exists()) {
                command.add("-c").add(configuration.getSdCard());
            } else {
                command.add("-c").add(configuration.getSdSize());
            }

            logger.log(Level.INFO, "Creating new avd using: {0}", command);

            ProcessInteractionBuilder interaction = new ProcessInteractionBuilder();
            interaction
                .replyTo("Do you wish to create a custom hardware profile \\[no\\]")
                .with("no" + System.getProperty("line.separator"));

            executor.execute(interaction.build(), command.getAsArray());

            configuration.setAvdGenerated(true);

            androidVirtualDeviceAvailable.fire(new AndroidVirtualDeviceAvailable(configuration.getAvdName()));
        } catch (AndroidExecutionException e) {
            // rewrap to have nice stacktrace
            throw new AndroidExecutionException(e, "Unable to create a new AVD Device");
        }
    }
}
