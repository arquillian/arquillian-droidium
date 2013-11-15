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
package org.arquillian.droidium.container.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.api.Screenshooter;
import org.arquillian.droidium.container.api.Screenshot;
import org.arquillian.droidium.container.api.ScreenshotType;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.container.utils.AndroidScreenshotIdentifierGenerator;

import com.android.ddmlib.RawImage;

/**
 * Takes screenshots of Android device.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidScreenshooter implements Screenshooter {

    private static final Logger log = Logger.getLogger(AndroidScreenshooter.class.getName());

    private String screenshotTargetDir = "target" + System.getProperty("file.separator");

    private ScreenshotType screenshotType = ScreenshotType.PNG;

    private AndroidDevice device;

    /**
     *
     * @param device Android device to get screenshots of
     */
    public AndroidScreenshooter(AndroidDevice device) {
        Validate.notNull(device, "Android device you try to get into Android screenshooter can not be a null object!");
        this.device = device;
    }

    @Override
    public void setScreenshotTargetDir(String screenshotTargetDir) {
        Validate.notNullOrEmpty(screenshotTargetDir, "Screenshot target directory can not be a null object or an empty string");
        File file = new File(screenshotTargetDir);
        if (!file.exists()) {
            if (file.mkdirs()) {
                this.screenshotTargetDir = screenshotTargetDir;
                log.info("Created screenshot target directory: " + file.getAbsolutePath());
            } else {
                throw new IllegalArgumentException("Unable to create screenshot target dir " + file.getAbsolutePath());
            }
        } else {
            Validate.isReadableDirectory(screenshotTargetDir,
                "want-to-be target screenshot directory path exists and is not a directory");
            this.screenshotTargetDir = screenshotTargetDir;
        }
    }

    @Override
    public Screenshot takeScreenshot() {
        return takeScreenshot(null, getScreenshotImageFormat());
    }

    @Override
    public Screenshot takeScreenshot(String fileName) {
        return takeScreenshot(fileName, getScreenshotImageFormat());
    }

    @Override
    public Screenshot takeScreenshot(ScreenshotType type) {
        return takeScreenshot(null, type);
    }

    @Override
    public Screenshot takeScreenshot(String fileName, ScreenshotType type) {

        if (fileName != null) {
            if (fileName.trim().isEmpty()) {
                throw new IllegalArgumentException("The file name to save a screenshot to can not be an empty string.");
            }
        }
        if (!device.isOnline()) {
            throw new AndroidExecutionException("Android device is not online, can not take any screenshots.");
        }

        AndroidScreenshot screenshot = (AndroidScreenshot) device.getScreenshot();

        RawImage rawImage = screenshot.getRawImage();

        if (rawImage == null) {
            throw new AndroidExecutionException("Unable to get screenshot of underlying Android device.");
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

        String imageName = null;

        if (fileName == null) {
            imageName = new AndroidScreenshotIdentifierGenerator().getIdentifier(type);
        }
        else {
            imageName = fileName + "." + type.toString();
        }

        File image = new File(screenshotTargetDir, imageName);

        try {
            ImageIO.write(bufferedImage, type.toString(), image);
        } catch (IOException e) {
            log.info("unable to save screenshot of type " + type.toString() + " to file " + image.getAbsolutePath());
            e.printStackTrace();
        }

        screenshot.setScreenshot(image);

        return screenshot;
    }

    @Override
    public void setScreensthotImageFormat(ScreenshotType type) {
        Validate.notNull(type, "Screenshot format to set can not be a null object!");
        this.screenshotType = type;
    }

    private ScreenshotType getScreenshotImageFormat() {
        return this.screenshotType;
    }

}
