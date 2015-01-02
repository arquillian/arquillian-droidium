/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.extension.recorder.screenshooter.droidium.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.extension.recorder.DefaultFileNameBuilder;
import org.arquillian.extension.recorder.RecorderFileUtils;
import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.arquillian.extension.recorder.screenshooter.ScreenshooterConfiguration;
import org.arquillian.extension.recorder.screenshooter.Screenshot;
import org.arquillian.extension.recorder.screenshooter.ScreenshotMetaData;
import org.arquillian.extension.recorder.screenshooter.ScreenshotType;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.jboss.arquillian.core.spi.Validate;

import com.android.ddmlib.RawImage;

/**
 * Takes screenshots for Android devices.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumScreenshooter implements Screenshooter {

    private File screenshotTargetDir;

    private ScreenshotType screenshotType;

    private ScreenshooterConfiguration configuration;

    private AndroidDevice androidDevice;

    private TakenResourceRegister takenResourceRegister;

    private String message;
    
    /**
     *
     * @param takenResourceRegister
     * @throws IllegalArgumentException if {@code takenResourceRegister} is a null object
     */
    public DroidiumScreenshooter(TakenResourceRegister takenResourceRegister) {
        Validate.notNull(takenResourceRegister, "Resource register can not be a null object!");
        this.takenResourceRegister = takenResourceRegister;
    }

    @Override
    public void init(ScreenshooterConfiguration configuration) {
        if (this.configuration == null) {
            if (configuration != null) {
                this.configuration = configuration;
                File root = this.configuration.getRootDir();
                setScreenshotTargetDir(root);
                setScreenshotType(ScreenshotType.valueOf(this.configuration.getScreenshotType()));
            }
        }
    }

    /**
     *
     * @param androidDevice sets Android device which will record videos.
     */
    public void setAndroidDevice(AndroidDevice androidDevice) {
        Validate.notNull(androidDevice, "Android device to set to take screenshots is a null object!");
        this.androidDevice = androidDevice;
    }    

    @Override
    public Screenshot takeScreenshot() {
        return takeScreenshot(screenshotType);
    }

    @Override
    public Screenshot takeScreenshot(ScreenshotType type) {
        Validate.notNull(type, "Screenshot type is a null object!");
        ScreenshotMetaData metaData = new ScreenshotMetaData();
        metaData.setResourceType(type);
        return takeScreenshot(
            new File(new DefaultFileNameBuilder().withMetaData(metaData).build()),
            type);
    }

    @Override
    public Screenshot takeScreenshot(String fileName) {
        Validate.notNullOrEmpty(fileName, "File name is a null object or an empty string!");
        return takeScreenshot(new File(fileName));
    }

    @Override
    public Screenshot takeScreenshot(File file) {
        Validate.notNull(file, "File is a null object!");
        return takeScreenshot(file, screenshotType);
    }

    @Override
    public Screenshot takeScreenshot(String fileName, ScreenshotType type) {
        Validate.notNullOrEmpty(fileName, "File name is a null object or an empty string!");
        Validate.notNull(type, "Type of screenshot is a null object!");
        return takeScreenshot(new File(fileName), type);
    }

    @Override
    public Screenshot takeScreenshot(File file, ScreenshotType type) {

        if (configuration == null) {
            throw new IllegalStateException("Screenshooter was not initialized. Please call init() method first.");
        }

        file = RecorderFileUtils.checkFileExtension(file, type);

        if (!androidDevice.isOnline()) {
            throw new RuntimeException("Android device is not online, can not take any screenshots.");
        }

        file = new File(screenshotTargetDir, file.getPath());
        RecorderFileUtils.createDirectory(file);

        RawImage rawImage = null;

        try {
            rawImage = androidDevice.getScreenshot().getRawImage();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get screenshot of underlying Android device.", e);
        }

        if (rawImage == null) {
            throw new RuntimeException("Unable to get screenshot of underlying Android device.");
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

        try {
            ImageIO.write(bufferedImage, type.toString(), file);
        } catch (IOException ex) {
            throw new RuntimeException("Could not write an image", ex);
        }

        Screenshot screenshot = new DroidiumScreenshot();
        screenshot.setResource(file);
        screenshot.setHeight(rawImage.height);
        screenshot.setWidth(rawImage.width);
        screenshot.setResourceType(screenshotType);

        if (message != null && !message.isEmpty()) {
            screenshot.setMessage(message);
            message = null;
        }

        takenResourceRegister.addTaken(screenshot);

        return screenshot;

    }

    @Override
    public Screenshooter setScreenshotTargetDir(String screenshotTargetDir) {
        Validate.notNullOrEmpty(screenshotTargetDir, "Screenshot target directory can not be a null object or an empty string");
        setScreenshotTargetDir(new File(screenshotTargetDir));
        return this;
    }

    @Override
    public Screenshooter setScreenshotTargetDir(File screenshotTargetDir) {
        Validate.notNull(screenshotTargetDir, "File is a null object!");
        RecorderFileUtils.createDirectory(screenshotTargetDir);
        this.screenshotTargetDir = screenshotTargetDir;
        return this;
    }

    @Override
    public Screenshooter setScreenshotType(ScreenshotType screenshotType) {
        Validate.notNull(screenshotType, "Screenshot type is a null object!");
        this.screenshotType = screenshotType;
        return this;
    }

    @Override
    public ScreenshotType getScreenshotType() {
        return screenshotType;
    }

    @Override
    public Screenshooter setMessage(String message) {
        this.message = message;
        return this;
    }

}
