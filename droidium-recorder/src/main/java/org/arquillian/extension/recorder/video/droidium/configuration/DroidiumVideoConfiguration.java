/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.recorder.video.droidium.configuration;

import java.io.File;

import org.arquillian.extension.recorder.video.VideoConfiguration;
import org.arquillian.extension.recorder.video.VideoType;
import org.arquillian.recorder.reporter.ReporterConfiguration;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumVideoConfiguration extends VideoConfiguration {

    private static final long MIN_BITRATE = 100000; // 0.1Mbps

    private static final long MAX_BITRATE = 200 * 1000000; // 200Mbps

    private static final long DEFAULT_BITRATE = 4000000; // 200Mbps

    private String bitrate = "4000000"; // 4Mbps by default

    private String remoteVideoDir = "/sdcard/";

    public DroidiumVideoConfiguration(ReporterConfiguration reporterConfiguration) {
        super(reporterConfiguration);
    }

    /**
     * By default set to 4 (4Mbps) when not overridden in configuration.
     *
     * @return bitrate of which videos should be taken
     */
    public long getBitrate() {
        long bitrate = Long.parseLong(getProperty("bitrate", this.bitrate));

        if (bitrate < MIN_BITRATE || bitrate > MAX_BITRATE) {
            bitrate = DEFAULT_BITRATE;
        }

        return bitrate;
    }

    /**
     *
     * @return directory on Android where videos will be saved, defaults to {@code /sdcard/}.
     */
    public File getRemoteVideoDir() {
        return new File(getProperty("remoteVideoDir", remoteVideoDir));
    }

    @Override
    public String getVideoType() {
        return VideoType.MP4.toString().toUpperCase();
    }

    @Override
    public void validate() {
        super.validate();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(String.format("%-40s %s\n", "bitrate", getBitrate()));
        sb.append(String.format("%-40s %s\n", "remoteVideoDir", getRemoteVideoDir().getAbsolutePath()));
        return sb.toString();
    }

}
