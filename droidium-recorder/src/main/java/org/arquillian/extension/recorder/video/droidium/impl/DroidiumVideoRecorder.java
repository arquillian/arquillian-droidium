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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.extension.recorder.ContainerAwareFileNameBuilder;
import org.arquillian.extension.recorder.RecorderFileUtils;
import org.arquillian.extension.recorder.video.Recorder;
import org.arquillian.extension.recorder.video.Video;
import org.arquillian.extension.recorder.video.VideoConfiguration;
import org.arquillian.extension.recorder.video.VideoMetaData;
import org.arquillian.extension.recorder.video.VideoType;
import org.arquillian.extension.recorder.video.droidium.configuration.DroidiumVideoConfiguration;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumVideoRecorder implements Recorder {

    private static final Logger logger = Logger.getLogger(DroidiumVideoRecorder.class.getName());

    private File videoTargetDir;

    private VideoType videoType;

    private VideoConfiguration configuration;

    private VideoRecorder recorder;

    private TakenResourceRegister takenResourceRegister;

    private AndroidDevice androidDevice;

    private String message;

    private Video timeoutedVideo = null;

    public DroidiumVideoRecorder(TakenResourceRegister takenResourceRegister) {
        Validate.notNull(takenResourceRegister, "Resource register can not be a null object!");
        this.takenResourceRegister = takenResourceRegister;
    }

    @Override
    public void init(VideoConfiguration configuration) {
        if (this.configuration == null) {
            if (configuration != null) {
                this.configuration = configuration;
                File root = this.configuration.getRootDir();

                setVideoTargetDir(root);
                setVideoType(VideoType.valueOf(this.configuration.getVideoType()));

                recorder = new VideoRecorder((DroidiumVideoConfiguration) this.configuration);
            }
        }
    }

    /**
     *
     * @param androidDevice sets Android device which will record videos.
     */
    public void setAndroidDevice(AndroidDevice androidDevice) {
        Validate.notNull(androidDevice, "Android device to set for Droidium video recorder is null object.");
        this.androidDevice = androidDevice;
    }

    @Override
    public void startRecording() {
        startRecording(videoType);
    }

    @Override
    public void startRecording(VideoType videoType) {
        Validate.notNull(videoType, "Video type is a null object!");
        VideoMetaData metaData = new VideoMetaData();
        metaData.setResourceType(videoType);
        startRecording(
            new File(new ContainerAwareFileNameBuilder(androidDevice.getAvdName()).withMetaData(metaData).build()),
            videoType);
    }

    @Override
    public void startRecording(String fileName) {
        Validate.notNullOrEmpty(fileName, "File name is a null object or an empty string!");
        startRecording(new File(fileName));
    }

    @Override
    public void startRecording(File file) {
        Validate.notNull(file, "File is a null object");
        startRecording(file, videoType);
    }

    @Override
    public void startRecording(String fileName, VideoType videoType) {
        Validate.notNullOrEmpty(fileName, "File name is a null object or an empty string!");
        Validate.notNull(videoType, "Type of video is a null object!");
        startRecording(fileName, videoType);
    }

    @Override
    public void startRecording(File toFile, VideoType videoType) {
        Validate.notNull(toFile, "File is a null object!");
        Validate.notNull(videoType, "Type of video is a null object!");

        if (configuration == null) {
            throw new IllegalStateException("Video recorder was not initialized. Please call init() method first.");
        }

        if (!androidDevice.isOnline()) {
            throw new RuntimeException("Android device is not online, can not record any videos.");
        }

        if (recorder.isRecording()) {
            throw new IllegalStateException("It seems you are already recording some video, call stopRecording() firstly.");
        }

        String androidContainerIdentifier = getAndroidContainerIdentifier(androidDevice);

        if (!toFile.getName().startsWith(androidContainerIdentifier)) {

            String containerAwareFileName = androidContainerIdentifier + "_" + toFile.getName();

            toFile = new File(toFile.getParentFile(), containerAwareFileName);
        }

        toFile = RecorderFileUtils.checkFileExtension(toFile, videoType);
        toFile = new File(videoTargetDir, toFile.getPath());
        RecorderFileUtils.createDirectory(toFile.getParentFile());

        timeoutedVideo = null;

        recorder.setAndroidDevice(androidDevice);
        recorder.startRecording(toFile);

        logger.log(Level.INFO, "after starting recording");
    }

    @Override
    public Recorder setVideoTargetDir(String videoTargetDir) {
        Validate.notNullOrEmpty(videoTargetDir, "Video target directory can not be a null object nor an empty string!");
        return setVideoTargetDir(new File(videoTargetDir));
    }

    @Override
    public Recorder setVideoTargetDir(File videoTargetDir) {
        Validate.notNull(videoTargetDir, "File is a null object!");
        RecorderFileUtils.createDirectory(videoTargetDir);
        this.videoTargetDir = videoTargetDir;
        return this;
    }

    @Override
    public Recorder setVideoType(VideoType videoType) {
        Validate.notNull(videoType, "Video type is a null object!");
        this.videoType = videoType;
        return this;
    }

    @Override
    public VideoType getVideoType() {
        return videoType;
    }

    @Override
    public Video stopRecording() {

        Video video = null;

        if (timeoutedVideo != null) {
            video = timeoutedVideo;
        } else if (recorder != null && recorder.isRecording()) {
            video = recorder.stopRecording();
        } else {
            throw new IllegalStateException("It seems you are not recording yet.");
        }

        if (message != null && !message.isEmpty()) {
            video.setMessage(message);
            message = null;
        }

        takenResourceRegister.addTaken(video);

        logger.log(Level.INFO, "after stop recording");

        return video;
    }

    @Override
    public Recorder setFrameRate(int framerate) {
        return this;
    }

    @Override
    public int getFrameRate() {
        return 0;
    }

    @Override
    public Recorder setMessage(String message) {
        this.message = message;
        return this;
    }

    // helpers

    private String getAndroidContainerIdentifier(AndroidDevice androidDevice) {

        String id = null;

        if (androidDevice.isEmulator()) {
            id = androidDevice.getAvdName();
        } else {
            id = androidDevice.getSerialNumber();
        }

        return id;
    }

}
