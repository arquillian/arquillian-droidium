/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.droidium.container.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.ActivityManager;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceOutputReciever;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.api.Screenshot;
import org.arquillian.droidium.container.configuration.Validate;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

/**
 * The implementation of {@link AndroidDevice}.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeviceImpl implements AndroidDevice {

    private static final Logger log = Logger.getLogger(AndroidDeviceImpl.class.getName());

    private IDevice delegate;

    private ActivityManager activityManager;

    private int droneHostPort = 14444;

    private int droneGuestPort = 8080;

    public AndroidDeviceImpl() {
        // only for testing purposes
    }

    AndroidDeviceImpl(IDevice delegate) {
        Validate.notNull(delegate, "delegate to set for Android device can not be a null object");
        this.delegate = delegate;
    }

    @Override
    public void setActivityManager(ActivityManager activityManager) {
        Validate.notNull(activityManager, "Activity manager to set for Android device can not be a null object!");
        this.activityManager = activityManager;
    }

    @Override
    public ActivityManager getActivityManager() {
        return activityManager;
    }

    @Override
    public String getSerialNumber() {
        return delegate.getSerialNumber();
    }

    @Override
    public String getAvdName() {
        if (isEmulator()) {
            String avdName = delegate.getAvdName();
            if (avdName == null || avdName.equals("<build>")) {
                return null;
            }
            return avdName;
        }
        return null;
    }

    @Override
    public Map<String, String> getProperties() {
        return delegate.getProperties();
    }

    @Override
    public String getProperty(String name) throws IOException, AndroidExecutionException {
        try {
            return delegate.getPropertyCacheOrSync(name);
        } catch (TimeoutException e) {
            throw new AndroidExecutionException("Unable to get property '" + name + "' value in given timeout", e);
        } catch (AdbCommandRejectedException e) {
            throw new AndroidExecutionException("Unable to get property '" + name + "' value, command was rejected", e);
        } catch (ShellCommandUnresponsiveException e) {
            throw new AndroidExecutionException("Unable to get property '" + name + "' value, shell is not responsive", e);
        }
    }

    @Override
    public boolean isOnline() {
        return delegate.isOnline();
    }

    @Override
    public boolean isEmulator() {
        return delegate.isEmulator();
    }

    @Override
    public boolean isOffline() {
        return delegate.isOffline();
    }

    @Override
    public String getConsolePort() {
        return isEmulator() ? getSerialNumber().split("-")[1] : null;
    }

    @Override
    public void executeShellCommand(String command) throws AndroidExecutionException {
        final String commandString = command;
        executeShellCommand(command, new AndroidDeviceOutputReciever() {
            @Override
            public void processNewLines(String[] lines) {
                if (log.isLoggable(Level.INFO)) {
                    for (String line : lines) {
                        log.log(Level.FINE, "Shell command {0}: {1}", new Object[] { commandString, line });
                    }
                }
            }

            @Override
            public boolean isCancelled() {
                return false;
            }
        });

    }

    @Override
    public void executeShellCommand(String command, AndroidDeviceOutputReciever reciever)
        throws AndroidExecutionException {
        try {
            delegate.executeShellCommand(command, new AndroidRecieverDelegate(reciever));
        } catch (TimeoutException e) {
            throw new AndroidExecutionException("Unable to execute command '" + command + "' within given timeout", e);
        } catch (AdbCommandRejectedException e) {
            throw new AndroidExecutionException("Unable to execute command '" + command + "', command was rejected", e);
        } catch (ShellCommandUnresponsiveException e) {
            throw new AndroidExecutionException("Unable to execute command '" + command + "', shell is not responsive",
                e);
        } catch (IOException e) {
            throw new AndroidExecutionException("Unable to execute command '" + command + "'", e);
        }

    }

    @Override
    public void createPortForwarding(int localPort, int remotePort) throws AndroidExecutionException {
        try {
            delegate.createForward(localPort, remotePort);
        } catch (TimeoutException e) {
            throw new AndroidExecutionException("Unable to forward port (" + localPort + " to " + remotePort
                + ") within given timeout", e);
        } catch (AdbCommandRejectedException e) {
            throw new AndroidExecutionException("Unable to forward port (" + localPort + " to " + remotePort
                + "), command was rejected", e);
        } catch (IOException e) {
            throw new AndroidExecutionException("Unable to forward port (" + localPort + " to " + remotePort + ").", e);
        }
    }

    @Override
    public void removePortForwarding(int localPort, int remotePort) throws AndroidExecutionException {
        try {
            delegate.removeForward(localPort, remotePort);
        } catch (TimeoutException e) {
            throw new AndroidExecutionException("Unable to remove port forwarding (" + localPort + " to " + remotePort
                + ") within given timeout", e);
        } catch (AdbCommandRejectedException e) {
            throw new AndroidExecutionException("Unable to remove port forwarding (" + localPort + " to " + remotePort
                + "), command was rejected", e);
        } catch (IOException e) {
            throw new AndroidExecutionException("Unable to remove port forwarding (" + localPort + " to " + remotePort
                + ").", e);
        }
    }

    @Override
    public void installPackage(File packageFilePath, boolean reinstall, String... extraArgs) throws AndroidExecutionException {
        Validate.isReadable(packageFilePath.getAbsoluteFile(), "File " + packageFilePath.getAbsoluteFile()
            + " must represent a readable APK file");
        try {
            String retval = delegate.installPackage(packageFilePath.getAbsolutePath(), reinstall, extraArgs);
            if (retval != null) {
                throw new AndroidExecutionException("Unable to install APK from " + packageFilePath.getAbsolutePath()
                    + ". Command failed with status code: " + retval);
            }
        } catch (InstallException e) {
            throw new AndroidExecutionException("Unable to install APK from " + packageFilePath.getAbsolutePath(), e);
        }

    }

    @Override
    public boolean isPackageInstalled(String packageName) throws AndroidExecutionException {
        try {
            String command = "pm list packages -f";
            PackageInstalledMonkey monkey = new PackageInstalledMonkey(packageName);
            executeShellCommand(command, monkey);
            return monkey.isInstalled();
        } catch (Exception e) {
            throw new AndroidExecutionException("Unable to decide if package " + packageName + " is installed or nor", e);
        }
    }

    @Override
    public void uninstallPackage(String packageName) throws AndroidExecutionException {
        try {
            delegate.uninstallPackage(packageName);
        } catch (InstallException e) {
            throw new AndroidExecutionException("Unable to uninstall APK named " + packageName, e);
        }

    }

    @Override
    public int getDroneHostPort() {
        return droneHostPort;
    }

    @Override
    public int getDroneGuestPort() {
        return droneGuestPort;
    }

    @Override
    public void setDroneHostPort(int droneHostPort) {
        this.droneHostPort = droneHostPort;
    }

    @Override
    public void setDroneGuestPort(int droneGuestPort) {
        this.droneGuestPort = droneGuestPort;
    }

    @Override
    public Screenshot getScreenshot() throws Exception {
        Screenshot screenshot = new ScreenshotImpl();
        screenshot.setRawImage(delegate.getScreenshot());
        return screenshot;
    }

    private static class ScreenshotImpl implements Screenshot {

        RawImage screenshot;

        @Override
        public RawImage getRawImage() {
            return screenshot;
        }

        @Override
        public void setRawImage(RawImage screenshot) {
            this.screenshot = screenshot;
        }

    }

    private static class PackageInstalledMonkey implements AndroidDeviceOutputReciever {

        private String packageName;

        private boolean installed = false;

        public PackageInstalledMonkey(String packageName) {
            this.packageName = packageName;
        }

        @Override
        public void processNewLines(String[] lines) {
            for (String line : lines) {
                if (line.contains(packageName)) {
                    installed = true;
                    break;
                }
            }
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        public boolean isInstalled() {
            return installed;
        }
    }

    private static final class AndroidRecieverDelegate extends MultiLineReceiver {

        private AndroidDeviceOutputReciever delegate;

        public AndroidRecieverDelegate(AndroidDeviceOutputReciever delegate) {
            this.delegate = delegate;
        }

        @Override
        public void processNewLines(String[] lines) {
            delegate.processNewLines(lines);
        }

        @Override
        public boolean isCancelled() {
            return delegate.isCancelled();
        }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-40s %s\n", "avdName", getAvdName()));
        sb.append(String.format("%-40s %s\n", "consolePort", getConsolePort()));
        sb.append(String.format("%-40s %s\n", "serialNumber", getSerialNumber()));
        sb.append(String.format("%-40s %s\n", "isEmulator", isEmulator()));
        sb.append(String.format("%-40s %s\n", "isOffline", isOffline()));
        sb.append(String.format("%-40s %s\n", "isOnline", isOnline()));
        sb.append(String.format("%-40s %s\n", "droneHostPort", getDroneHostPort()));
        sb.append(String.format("%-40s %s", "droneGuestPort", getDroneGuestPort()));
        return sb.toString();
    }

}
