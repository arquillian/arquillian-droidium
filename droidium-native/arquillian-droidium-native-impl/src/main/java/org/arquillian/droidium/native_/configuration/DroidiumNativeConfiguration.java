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
package org.arquillian.droidium.native_.configuration;

import java.io.File;

/**
 * Configuration for Arquillian Droidium for native testing.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeConfiguration {

    private static String fileSeparator = System.getProperty("file.separator");

    private File serverApk = new File("selendroid-server.apk");

    private File logFile = new File("target" + DroidiumNativeConfiguration.fileSeparator + "android.log");

    private File keystore = DroidiumNativeConfiguration.getDefaultKeyStore();

    private String storepass = "android";

    private String keypass = "android";

    private String alias = "androiddebugkey";

    private boolean removeTmpDir = true;

    private File tmpDir = DroidiumNativeConfiguration.getTemporaryDirectory();

    public File getLogFile() {
        return logFile;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    public File getServerApk() {
        return serverApk;
    }

    public void setServerApk(File serverApk) {
        this.serverApk = serverApk;
    }

    public File getKeystore() {
        return keystore;
    }

    public void setKeystore(File keystore) {
        this.keystore = keystore;
    }

    public String getStorepass() {
        return storepass;
    }

    public void setStorepass(String storepass) {
        this.storepass = storepass;
    }

    public String getKeypass() {
        return keypass;
    }

    public void setKeypass(String keypass) {
        this.keypass = keypass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean getRemoveTmpDir() {
        return this.removeTmpDir;
    }

    public void setRemoveTmpDir(boolean remoteTmpDir) {
        this.removeTmpDir = remoteTmpDir;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public static File getDefaultKeyStore() {
        String separator = System.getProperty("file.separator");
        return new File(System.getProperty("user.home") + separator + ".android" + separator + "debug.keystore");
    }

    public static File getTemporaryDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }
}
