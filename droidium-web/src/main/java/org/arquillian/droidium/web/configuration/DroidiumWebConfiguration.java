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
package org.arquillian.droidium.web.configuration;

import java.io.File;

/**
 * Configuration for Droidium for web in Arquillian.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumWebConfiguration {

    private File androidServerApk = new File("android-server.apk");

    private File webdriverLogFile = new File("target" + System.getProperty("file.separator") + "android-webdriver-monkey.log");

    /**
     * @return the androidServerApk
     */
    public File getAndroidServerApk() {
        return androidServerApk;
    }

    /**
     * @param androidServerApk the androidServerApk to set
     */
    public void setAndroidServerApk(File androidServerApk) {
        this.androidServerApk = androidServerApk;
    }

    /**
     * @return the webdriverLogFile
     */
    public File getWebdriverLogFile() {
        return webdriverLogFile;
    }

    /**
     * @param webdriverLogFile the webdriverLogFile to set
     */
    public void setWebdriverLogFile(File webdriverLogFile) {
        this.webdriverLogFile = webdriverLogFile;
    }
}
