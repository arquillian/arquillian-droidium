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
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.spi.event.AndroidSDCardCreate;
import org.arquillian.droidium.container.spi.event.AndroidSDCardDelete;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceCreate;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDelete;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDeleted;
import org.arquillian.spacelift.execution.ExecutionException;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.ProcessInteraction;
import org.arquillian.spacelift.process.ProcessInteractionBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
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
    private Event<AndroidVirtualDeviceAvailable> androidVirtualDeviceAvailable;

    @Inject
    private Event<AndroidVirtualDeviceDeleted> androidVirtualDeviceDeleted;

    @Inject
    private Event<AndroidSDCardDelete> androidSDCardDelete;

    @Inject
    private Event<AndroidSDCardCreate> androidSDCardCreate;

    public void deleteAndroidVirtualDevice(@Observes AndroidVirtualDeviceDelete event) {

        AndroidContainerConfiguration configuration = this.configuration.get();
        AndroidSDK sdk = androidSDK.get();

        String avdName = configuration.getAvdName();

        try {
            Command deleteAvdCommand = new CommandBuilder(sdk.getAndroidPath())
                .parameter("delete")
                .parameter("avd")
                .parameter("-n")
                .parameter(avdName)
                .build();

            Tasks.prepare(CommandTool.class).command(deleteAvdCommand).execute().await();

            logger.log(Level.INFO, "Android Virtual Device {0} deleted.", avdName);

        } catch (AndroidExecutionException ex) {
            throw new AndroidExecutionException("Unable to delete Android Virtual device " + avdName, ex);
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

        try {
            CommandBuilder cb = new CommandBuilder(sdk.getAndroidPath());
            cb.parameter("create")
                .parameter("avd")
                .parameter("-n")
                .parameter(configuration.getAvdName())
                .parameter("-t")
                .parameter(configuration.getTarget())
                .parameter("-f");
            // add ABI only if it was specified, append it to the command
            // Droidium might not be correctly autodiscovering ABI based on Target
            if (configuration.getAbi() != null && !"".equals(configuration.getAbi())) {
                cb.parameter("-b").parameter(configuration.getAbi());
            }

            if (configuration.getSdCard() != null && new File(configuration.getSdCard()).exists()) {
                cb.parameter("-c").parameter(configuration.getSdCard());
            } else {
                cb.parameter("-c").parameter(configuration.getSdSize());
            }

            Command command = cb.build();

            logger.log(Level.INFO, "Creating new AVD using: {0}", command);

            ProcessInteraction interaction = new ProcessInteractionBuilder().
                replyTo("Do you wish to create a custom hardware profile \\[no\\]")
                .with("no" + System.getProperty("line.separator"))
                .build();

            Tasks.prepare(CommandTool.class).interaction(interaction).command(command).execute().await();

            configuration.setAvdGenerated(true);

            androidVirtualDeviceAvailable.fire(new AndroidVirtualDeviceAvailable(configuration.getAvdName()));
        } catch (ExecutionException ex) {
            throw new AndroidExecutionException(ex, "Unable to create a new AVD Device");
        }
    }
}
