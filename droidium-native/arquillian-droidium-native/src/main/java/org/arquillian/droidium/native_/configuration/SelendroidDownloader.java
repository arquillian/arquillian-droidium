/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.native_.configuration;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.droidium.native_.spi.event.DroidiumNativeConfigured;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.execution.TimeoutExecutionException;
import org.arquillian.spacelift.task.archive.UnzipTool;
import org.arquillian.spacelift.task.net.DownloadTool;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Downloads Selendroid server APKs dynamically.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDownloader {

    private static final Logger logger = Logger.getLogger(SelendroidDownloader.class.getName());

    private static final int DOWNLOAD_TIMEOUT_IN_MINUTES = 5;

    private static final String SELENDROID_SERVER_URL =
        "https://github.com/selendroid/selendroid/releases/download/"
            + DroidiumNativeConfiguration.SELENDROID_VERSION + "/selendroid-standalone-" + DroidiumNativeConfiguration.SELENDROID_VERSION + "-with-dependencies.jar";

    private static final String SELENDROID_SERVER_HOME = "target/selendroid-standalone-" + DroidiumNativeConfiguration.SELENDROID_VERSION + "-with-dependencies.jar";

    private static final String SELENDROID_UNZIP = "target/selendroid";

    @Inject
    private Instance<DroidiumNativeConfiguration> configuration;

    public void onDroidiumNativeConfigured(@Observes DroidiumNativeConfigured event) {

        DroidiumNativeConfiguration configuration = this.configuration.get();

        if (!Validate.isReadable(configuration.getServerApk())) {
            configuration.setProperty("serverApk", DroidiumNativeConfiguration.SERVER_HOME);

            if (!Validate.isReadable(configuration.getServerApk())) {

                logger.info("You must provide a valid path to Selendroid server APK to Arquillian Droidium"
                    + " native plugin. Please be sure you have the read access to specified file. APK is going to be "
                    + "downloaded for you automatically right now.");

                try {
                    File unzipped = Spacelift.task(DownloadTool.class)
                        .from(SELENDROID_SERVER_URL)
                        .to(SELENDROID_SERVER_HOME)
                        .then(UnzipTool.class)
                        .toDir(SELENDROID_UNZIP)
                        .execute()
                        .awaitAtMost(DOWNLOAD_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);

                    // cache it to ~/.droidium/

                    DroidiumFileUtils.copyFileToDirectory(new File(unzipped, "/prebuild/selendroid-server-" + DroidiumNativeConfiguration.SELENDROID_VERSION + ".apk"),
                        new File(System.getProperty("user.home") + "/.droidium"));

                } catch (TimeoutExecutionException ex) {
                    throw new TimeoutExecutionException(String.format("Unable to download Selendroid from "
                        + "%s in %s minutes.", SELENDROID_SERVER_URL, DOWNLOAD_TIMEOUT_IN_MINUTES));
                }
            }
        }
    }
}
