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

import com.android.ddmlib.ScreenRecorderOptions;

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
     * Installs an Android application on device. This is a helper method that combines the syncPackageToDevice,
     * installRemotePackage, and removePackage steps
     *
     * @param packageFilePath the absolute file system path to file on local host to install
     * @param reinstall set to {@code true}if re-install of app should be performed
     * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for available options.
     */
    void installPackage(String packageFilePath, boolean reinstall, String... extraArgs) throws AndroidExecutionException;

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

    /**
     * Pulls file from mobile phone to computer.
     *
     * @param remoteFilePath remote file to pull
     * @param localFilePath local file to save pulled file to
     * @throws Exception
     */
    void pull(String remoteFilePath, String localFilePath) throws Exception;

    /**
     * Pulls file from mobile phone to computer.
     *
     * @param remoteFile remote file to pull
     * @param localFile local file to save pulled file to
     * @throws Exception
     */
    void pull(File remoteFile, File localFile) throws Exception;

    /**
     * Pushes file from computer to mobile phone.
     *
     * @param localFilePath local file to push
     * @param remoteFilePath remote file to save pushed file to
     * @throws Exception
     */
    void push(String localFilePath, String remoteFilePath) throws Exception;

    /**
     * Pushes file from computer to mobile phone.
     *
     * @param localFile local file to push
     * @param remoteFile remote file to save pushed file to
     * @throws Exception
     */
    void push(File localFile, File remoteFile) throws Exception;

    /**
     * Removes file on Android device.
     *
     * @param remoteFilePath file path to remove
     * @throws Exception
     */
    void remove(String remoteFilePath) throws Exception;

    /**
     * Removes file on Android device.
     *
     * @param remoteFile file to remove
     * @throws Exception
     */
    void remove(File remoteFile) throws Exception;

    /**
     * Starts video recording.
     *
     * You have to use Android device of API level 19 (4.4) and above in order to use this with success.
     *
     * @param remoteFilePath path on mobile phone to save the recorded video to
     * @param options
     * @since 1.0.0.Alpha6
     */
    void startRecording(String remoteFilePath, ScreenrecordOptions options) throws Exception;

    /**
     * Starts video recording.
     *
     * You have to use Android device of API level 19 (4.4) and above in order to use this with success.
     *
     * @param remoteFilePath path on mobile phone to save the recorded video to
     * @param options
     * @since 1.0.0.Alpha6
     */
    void startRecording(File remoteFilePath, ScreenrecordOptions options) throws Exception;

    /**
     * Starts video recording.
     *
     * Recorded video will be automatically saved somewhere to {@code /sdcard} on a mobile phone.
     *
     * @param options
     * @throws Exception
     * @since 1.0.0.Alpha6
     */
    void startRecording(ScreenrecordOptions options) throws Exception;

    /**
     * Checks if this Android device records some video.
     *
     * @return true if device records some video, false otherwise
     */
    boolean isRecording();

    /**
     * Stops Android device from recording a video. Returned video is in fact pulled from device dynamically to the host
     * computer.
     *
     * You have to use Android device of API level 19 (4.4) and above in order to use this with success.
     *
     * @return recorded video of Android device
     * @param localFilePath path on computer host where to save recorded file by {@link #startRecording(ScreenRecorderOptions)}
     * @since 1.0.0.Alpha6
     */
    Video stopRecording(String localFilePath) throws Exception;

    /**
     * Stops Android device from recording a video. Returned video is in fact pulled from device dynamically to the host
     * computer.
     *
     * You have to use Android device of API level 19 (4.4) and above in order to use this with success.
     *
     * @return recorded video of Android device
     * @param localFilePath path on computer host where to save recorded file by {@link #startRecording(ScreenRecorderOptions)}
     * @since 1.0.0.Alpha6
     */
    Video stopRecording(File localFilePath) throws Exception;
}
