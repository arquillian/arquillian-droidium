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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceOutputReciever;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.api.ScreenshotType;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.AndroidScreenshotIdentifierGenerator;

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
class AndroidDeviceImpl implements AndroidDevice {

    private static final Logger log = Logger.getLogger(AndroidDeviceImpl.class.getName());

    private IDevice delegate;

    private int droneHostPort = 14444;

    private int droneGuestPort = 8080;

    private String screenshotTargetDir = "target" + System.getProperty("file.separator");

    private ScreenshotType screenshotType = ScreenshotType.PNG;

    AndroidDeviceImpl(IDevice delegate) {
        Validate.notNull(delegate, "delegate to set for Android device can not be a null object.");
        this.delegate = delegate;
    }

    @Override
    public void setScreenshotTargetDir(String screenshotTargetDir) {
        Validate.notNullOrEmpty(screenshotTargetDir, "Screenshot target directory can not be a null object or an empty string");
        File file = new File(screenshotTargetDir);
        if (!file.exists()) {
            if (file.mkdirs()) {
                this.screenshotTargetDir = screenshotTargetDir;
                log.info("Created screenshot target directory: " + file.getAbsolutePath());
            } else {
                throw new IllegalArgumentException("Unable to create screenshot target dir " + file.getAbsolutePath());
            }
        } else {
            Validate.isReadableDirectory(screenshotTargetDir,
                "want-to-be target screenshot directory path exists and is not a directory");
            this.screenshotTargetDir = screenshotTargetDir;
        }
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
                        log.log(Level.INFO, "Shell command {0}: {1}", new Object[] { commandString, line });
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
    public File takeScreenshot() {
        return takeScreenshot(null, getScreenshotImageFormat());
    }

    @Override
    public File takeScreenshot(String fileName) {
        return takeScreenshot(fileName, getScreenshotImageFormat());
    }

    @Override
    public File takeScreenshot(ScreenshotType type) {
        return takeScreenshot(null, type);
    }

    @Override
    public File takeScreenshot(String fileName, ScreenshotType type) {
        if (fileName != null) {
            if (fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("file name to save a screenshot to can not be empty string");
            }
        }
        if (!isOnline()) {
            throw new AndroidExecutionException("Android device is not online, can not take any screenshots.");
        }

        RawImage rawImage = null;

        try {
            rawImage = delegate.getScreenshot();
        } catch (IOException ex) {
            log.info("Unable to take a screenshot of device " + getAvdName() == null ? getSerialNumber() : getAvdName());
            ex.printStackTrace();
        } catch (TimeoutException ex) {
            log.info("Taking of screenshot timeouted.");
            ex.printStackTrace();
        } catch (AdbCommandRejectedException ex) {
            log.info("Command which takes screenshot was rejected.");
            ex.printStackTrace();
        }

        BufferedImage bufferedImage = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_RGB);

        int index = 0;

        int indexInc = rawImage.bpp >> 3;
        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++, index += indexInc) {
                int value = rawImage.getARGB(index);
                bufferedImage.setRGB(x, y, value);
            }
        }

        String imageName = null;

        if (fileName == null) {
            imageName = new AndroidScreenshotIdentifierGenerator().getIdentifier(type.getClass());
        }
        else {
            imageName = fileName + "." + type.toString();
        }

        File image = new File(screenshotTargetDir, imageName);

        try {
            ImageIO.write(bufferedImage, type.toString(), image);
        } catch (IOException e) {
            log.info("unable to save screenshot of type " + type.toString() + " to file " + image.getAbsolutePath());
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void setScreensthotImageFormat(ScreenshotType type) {
        Validate.notNull(type, "Screenshot format to set can not be a null object!");
        this.screenshotType = type;
    }

    private ScreenshotType getScreenshotImageFormat() {
        return this.screenshotType;
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
        sb.append("\navdName\t\t:").append(this.getAvdName()).append("\n");
        sb.append("consolePort\t:").append(this.getConsolePort()).append("\n");
        sb.append("serialNumber\t:").append(this.getSerialNumber()).append("\n");
        sb.append("isEmulator\t:").append(this.isEmulator()).append("\n");
        sb.append("isOffline\t:").append(this.isOffline()).append("\n");
        sb.append("isOnline\t:").append(this.isOnline()).append("\n");
        sb.append("hostPort\t:").append(this.getDroneHostPort()).append("\n");
        sb.append("guestPort:\t").append(this.getDroneGuestPort()).append("\n");
        return sb.toString();
    }

}
