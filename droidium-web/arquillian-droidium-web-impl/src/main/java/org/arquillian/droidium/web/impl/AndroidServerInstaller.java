/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceOutputReciever;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.web.configuration.DroidiumWebConfiguration;
import org.arquillian.droidium.web.spi.event.AndroidServerInstalled;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Installator of Android Selenium server to Android device, physical or virtual. Sets port forwarding afterwards, by default
 * from port 14444 to 8080 at device side.
 *
 * <br>
 * </br> Observes:
 * <ul>
 * <li>{@link AndroidDeviceReady}</li>
 * </ul>
 *
 * Fires:
 * <ul>
 * <li>{@link AndroidServerInstalled}</li>
 * </ul>
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 */
public class AndroidServerInstaller {

    private static final Logger log = Logger.getLogger(AndroidServerInstaller.class.getName());

    private static final String APK_APP_NAME = "org.openqa.selenium.android.app";

    @Inject
    private Event<AndroidServerInstalled> androidServerInstalled;

    @Inject
    private Instance<DroidiumWebConfiguration> configuration;

    public void install(@Observes AndroidDeviceReady event) throws AndroidExecutionException, IOException {

        AndroidDevice device = event.getDevice();

        installServerAPK(device, configuration.get().getServerApk());
        log.info("Installation of Android Server APK for Arquillan Droidium web support was performed.");

        log.info("Starting Android Server for Arquillian Droidium web testing.");
        WebDriverMonkey monkey = new WebDriverMonkey(configuration.get().getLogFile());
        device.executeShellCommand(getWebDriverHubCommand().toString(), monkey);

        log.info("Waiting until Android server for Arquillian Droidium web support is started.");
        waitUntilStarted(device, monkey);

        log.log(Level.INFO, "Creating port forwarding from {0} to {1} from Android server to Arquillian Droidium web plugin.",
            new Object[] { device.getDroneHostPort(), device.getDroneGuestPort() });
        device.createPortForwarding(device.getDroneHostPort(), device.getDroneGuestPort());

        androidServerInstalled.fire(new AndroidServerInstalled());
    }

    private void installServerAPK(AndroidDevice device, File apk) throws AndroidExecutionException {
        if (device.isPackageInstalled(APK_APP_NAME)) {
            log.info("Package " + APK_APP_NAME + " is installed, trying to uninstall it.");
            device.uninstallPackage(APK_APP_NAME);
        }
        device.installPackage(apk, true);
    }

    private Command getTopCommand() {
        Command command = new Command();
        command.add("top -n 1");
        return command;
    }

    private Command getWebDriverHubCommand() {
        Command command = new Command();
        command.add("am start -a android.intent.action.MAIN -n org.openqa.selenium.android.app/.MainActivity");

        command.add(configuration.get().getOptions());

        // if debug is not already set via "options" and we specified we want to use debugging via debug option
        if (!command.getAsString().contains("-e debug") && configuration.get().getDebug()) {
            command.add("-e debug true");
        }

        return command;
    }

    private void waitUntilStarted(AndroidDevice device, WebDriverMonkey monkey) throws IOException,
        AndroidExecutionException {

        log.info("Starting Web Driver Hub on Android device.");
        for (int i = 0; i < 5; i++) {
            device.executeShellCommand(getTopCommand().toString(), monkey);
            if (monkey.isWebDriverHubStarted()) {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        throw new AndroidExecutionException("Unable to start Android Server for Arquillian Droidium web support.");
    }

    private static class WebDriverMonkey implements AndroidDeviceOutputReciever {

        private static final Logger logger = Logger.getLogger(WebDriverMonkey.class.getName());

        private final Writer output;

        private boolean started = false;

        public WebDriverMonkey(File output) throws IOException {
            this.output = new FileWriter(output);
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                logger.finest(line);
                try {
                    output.append(line).append("\n").flush();
                } catch (IOException e) {
                    // ignore output
                }
                if (line.contains(APK_APP_NAME)) {
                    this.started = true;
                }
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        public boolean isWebDriverHubStarted() {
            return started;
        }

    }

}
