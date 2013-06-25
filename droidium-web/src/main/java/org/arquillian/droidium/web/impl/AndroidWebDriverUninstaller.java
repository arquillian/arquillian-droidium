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

import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.web.event.AndroidWebDriverUninstalled;
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
 * <li>{@link AndroidWebDriverUninstalled}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidWebDriverUninstaller {

    private static final Logger log = Logger.getLogger(AndroidWebDriverUninstaller.class.getName());

    private static final String APK_APP_NAME = "org.openqa.selenium.android.app";

    @Inject
    private Event<AndroidWebDriverUninstalled> androidWebDriverUninstalled;

    public void uninstall(@Observes(precedence = 10) AndroidContainerStop event, AndroidDevice device) {
        Validate.notNull(device, "Injected Android device is null!");

        uninstallServerAPK(device, APK_APP_NAME);

        androidWebDriverUninstalled.fire(new AndroidWebDriverUninstalled());
    }

    private void uninstallServerAPK(AndroidDevice device, String apkAppName) {
        device.uninstallPackage(APK_APP_NAME);
        log.info("Uninstallation of Android Server for WebDriver support was performed.");
    }
}
