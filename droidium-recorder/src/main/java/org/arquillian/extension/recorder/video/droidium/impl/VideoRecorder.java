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
package org.arquillian.extension.recorder.video.droidium.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.ScreenrecordOptions;
import org.arquillian.extension.recorder.video.Video;
import org.arquillian.extension.recorder.video.VideoType;
import org.arquillian.extension.recorder.video.droidium.configuration.DroidiumVideoConfiguration;
import org.jboss.arquillian.core.spi.Validate;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class VideoRecorder {

    private static final long DEFAULT_BITRATE = 4000000;

    private static final String RECORD_PREFIX = "droidium_record_";

    private static final String RECORD_EXTENSION = ".mp4";

    private long bitrate = DEFAULT_BITRATE;

    private int recordHeight = 0;

    private int recordWidth = 0;

    private AndroidDevice androidDevice;

    private DroidiumVideoConfiguration configuration;

    private File recordedVideo = null;

    private volatile boolean running = false;

    public VideoRecorder(DroidiumVideoConfiguration configuration) {
        Validate.notNull(configuration, "Video configuration is null!");
        this.configuration = configuration;
        this.bitrate = configuration.getBitrate();
        this.recordHeight = configuration.getHeight();
        this.recordWidth = configuration.getWidth();
    }

    public void setAndroidDevice(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    public void startRecording(final File toFile) {
        if (isRecording()) {
            throw new IllegalStateException("It seems you are already recording. Please call stopRecording() first.");
        }

        recordedVideo = toFile;

        try {
            recordedVideo.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create file to which video will be saved: " + recordedVideo.getAbsolutePath());
        }

        try {
            androidDevice.startRecording(getRemoteVideoFile(), getScreenRecorderOptions());
            running = androidDevice.isRecording();
        } catch (Exception e) {
            throw new RuntimeException("Error while starting to record a video.", e);
        }
    }

    public Video stopRecording() {
        if (!isRecording()) {
            throw new IllegalStateException("It seems you are not recording anything yet. Please call startRecording() first.");
        }

        org.arquillian.droidium.container.api.Video video = null;

        try {
            video = androidDevice.stopRecording(recordedVideo.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Error while stopping video recording.", e);
        } finally {
            running = false;
        }

        Video droidiumVideo = new DroidiumVideo();
        droidiumVideo.setResource(video.getVideo());
        droidiumVideo.setResourceType(VideoType.valueOf(configuration.getVideoType()));
        droidiumVideo.setWidth(recordWidth);
        droidiumVideo.setHeight(recordHeight);

        return droidiumVideo;
    }

    public long getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        if (bitrate > 0) {
            this.bitrate = bitrate;
        }
    }

    /**
     *
     * @return true if this recording is recording some video right now, false othewise
     */
    public boolean isRecording() {
        return running;
    }

    // utils

    private ScreenrecordOptions getScreenRecorderOptions() {
        ScreenrecordOptions.Builder builder = new ScreenrecordOptions.Builder();

        builder.setBitrate(getBitrate());

        if (recordHeight > 0 && recordWidth > 0) {
            builder.setSize(recordWidth, recordHeight);
        }

        return builder.build();
    }

    private File getRemoteVideoFile() {
        return new File(configuration.getRemoteVideoDir(), RECORD_PREFIX + UUID.randomUUID().toString() + RECORD_EXTENSION);
    }
}
