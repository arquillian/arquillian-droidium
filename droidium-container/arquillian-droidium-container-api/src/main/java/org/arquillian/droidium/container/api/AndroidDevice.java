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
package org.arquillian.droidium.container.api;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Representation of Android Device
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public interface AndroidDevice {

    /**
     * Returns serial number of device.
     *
     * @return Serial number
     */
    String getSerialNumber();

    /**
     * Returns name of Android Virtual Device.
     *
     * @return Either a virtual device name or {@code null} if device is not an emulator
     */
    String getAvdName();

    /**
     * Returns a map of properties available for the device. These properties are cached.
     *
     * @return A properties map
     */
    Map<String, String> getProperties();

    /**
     * Returns a value of property with given name
     *
     * @param name A key
     * @return Value of property or {@code null} if not present
     * @throws IOException
     * @throws AndroidExecutionException
     */
    String getProperty(String name) throws IOException, AndroidExecutionException;

    /**
     * Checks if the device is online
     *
     * @return {@code true} if device is online, {@code false} otherwise
     */
    boolean isOnline();

    /**
     * Checks if the device is an emulator
     *
     * @return {@code true} if device is an emulator, {@code false} otherwise
     */
    boolean isEmulator();

    /**
     * Checks if the device is offline.
     *
     * @return {@code true} if device is offline, {@code false} otherwise
     */
    boolean isOffline();

    /**
     * Executes a shell command on the device. Silently discards command output.
     *
     * @param command The command to be executed
     * @throws AndroidExecutionException
     */
    void executeShellCommand(String command) throws AndroidExecutionException;

    /**
     * Executes a shell command on the device
     *
     * @param command The command to be executed
     * @param reciever A processor to process command output
     * @throws AndroidExecutionException
     */
    void executeShellCommand(String command, AndroidDeviceOutputReciever reciever) throws AndroidExecutionException;

    /**
     * Creates a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     */
    void createPortForwarding(int localPort, int remotePort) throws AndroidExecutionException;

    /**
     * Removes a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     */
    void removePortForwarding(int localPort, int remotePort) throws AndroidExecutionException;

    /**
     * Installs an Android application on device. This is a helper method that combines the syncPackageToDevice,
     * installRemotePackage, and removePackage steps
     *
     * @param packageFilePath the absolute file system path to file on local host to install
     * @param reinstall set to {@code true}if re-install of app should be performed
     * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for available options.
     */
    void installPackage(File packageFilePath, boolean reinstall, String... extraArgs) throws AndroidExecutionException;

    /**
     * Uninstalls an package from the device.
     *
     * @param packageName the Android application package name to uninstall
     */
    void uninstallPackage(String packageName) throws AndroidExecutionException;

    /**
     * Checks if an APK package of name {@code packageName} is installed or not.
     *
     * @param packageName name of the installed package
     * @return true if a package of {@code packageName} is installed, false otherwise
     */
    boolean isPackageInstalled(String packageName) throws AndroidExecutionException;

    /**
     * @return console port of the emulator - the number after "emulator" string in the serial number string of the emulator
     *         obtained by {@code adb devices -l command}.
     */
    String getConsolePort();

    /**
     * Host is your local computer.
     *
     * @return host port for extensions like Droidium native or web plugin
     */
    int getDroneHostPort();

    /**
     * Guest port is port on the Android device side
     *
     * @return guest port for extensions like Droidium native or web plugin
     */
    int getDroneGuestPort();

    /**
     * Host is your local computer
     *
     * @param droneHostPort for extensions like Droidium native or web plugin
     */
    void setDroneHostPort(int droneHostPort);

    /**
     * Guest port is port on the Android device side
     *
     * @param droneGuestPort for extensions like Droidium native or web plugin
     */
    void setDroneGuestPort(int droneGuestPort);

    /**
     *
     * @return screenshot of Android device
     * @throws Exception
     */
    Screenshot getScreenshot() throws Exception;

    /**
     *
     * @param activityManager
     */
    void setActivityManager(ActivityManager activityManager);

    /**
     *
     * @return activity manager
     */
    ActivityManager getActivityManager();

    /**
     *
     * @param alreadyRuns true if Android device already runs, false otherwise
     */
    void setAlreadyRuns(boolean alreadyRuns);

    /**
     *
     * @return true if Android device is already running prior to test execution, false otherwise
     */
    boolean getAlreadyRuns();
}
