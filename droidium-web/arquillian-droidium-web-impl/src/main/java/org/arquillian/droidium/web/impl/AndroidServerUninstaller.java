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
package org.arquillian.droidium.web.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.web.spi.AndroidServerUninstalled;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Uninstaller of Android server for WebDriver support. Hooks to {@link AndroidContainerStop} before actual stopping of the
 * container is fired by higher precedence.
 *
 * <br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AndroidContainerStop}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidServerUninstalled}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidServerUninstaller {

    private static final Logger log = Logger.getLogger(AndroidServerUninstaller.class.getName());

    private static final String APK_APP_NAME = "org.openqa.selenium.android.app";

    @Inject
    private Event<AndroidServerUninstalled> androidServerUninstalled;

    public void uninstall(@Observes(precedence = 10) AndroidContainerStop event, AndroidDevice device) {
        Validate.notNull(device, "Injected Android device for is null!");

        try {
            device.uninstallPackage(APK_APP_NAME);

            log.info("Uninstallation of Android server for Arquillian Droidium web was performed.");
        } catch (AndroidExecutionException ex) {
            log.info("Uninstallation of Android server for Arquillian Droidium web failed.");
        }

        try {
            log.log(Level.INFO,
                "Removing port forwaring from {0} to {1} for Android server of Arquillian Droidium web support.",
                new Object[] { device.getDroneHostPort(), device.getDroneGuestPort() });

            device.removePortForwarding(device.getDroneHostPort(), device.getDroneGuestPort());

            log.info("Removing of port forwarding for Arquillian Droidium web support was successful. ");
        } catch (AndroidExecutionException ex) {
            log.info("Removing of port forwarding for Arquillian Droidium Web support after Android server "
                + "was uninstalled failed.");
        }

        androidServerUninstalled.fire(new AndroidServerUninstalled());
    }
}
