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

    private File serverApk = new File("android-server.apk");

    private File logFile = new File("target" + System.getProperty("file.separator") + "android.log");

    /**
     * @return the serverApk
     */
    public File getServerApk() {
        return serverApk;
    }

    /**
     * @param serverApk the serverApk to set
     */
    public void setServerApk(File serverApk) {
        this.serverApk = serverApk;
    }

    /**
     * @return the logFile
     */
    public File getLogFile() {
        return logFile;
    }

    /**
     * @param logFile the logFile to set
     */
    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }
}
