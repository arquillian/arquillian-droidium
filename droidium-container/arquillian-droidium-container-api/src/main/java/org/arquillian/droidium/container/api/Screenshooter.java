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
package org.arquillian.droidium.container.api;

import java.io.File;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public interface Screenshooter {
    /**
     * Takes screenshot in default format with random string as a name with file format extension.
     *
     * @return screenshot of default image format
     */
    File takeScreenshot();

    /**
     * Takes screenshot in specified format. Name of screenshot is random string with file format extension.
     *
     * @param type type of screenshot
     * @return screenshot of given image type
     */
    File takeScreenshot(ScreenshotType type);

    /**
     * Takes screenshot of default file format with specified name.
     *
     * @param fileName name of file without file format extension
     * @return screenshot of default format with specified name
     */
    File takeScreenshot(String fileName);

    /**
     * Takes screenshot of specified type which is saved under specified name
     *
     * @param fileName name of file without file format extension
     * @param type type of screenshot required
     * @return screenshot of specified format with a specified name
     */
    File takeScreenshot(String fileName, ScreenshotType type);

    /**
     * Sets a directory where all screenshots taken by {@link AndroidDevice#takeScreenshot()} will be saved from now on.
     *
     * @param screenshotTargetDir directory to save screenshots to
     * @throws IllegalArgumentException if {@code screenshotTargetDir} is null, empty or does not represents existing and
     *         writable directory
     */
    void setScreenshotTargetDir(String screenshotTargetDir);

    /**
     * Sets the format of images to take. After setting this, all subsequent images will be of this format when not explicitly
     * specified otherwise.
     *
     * @param type type of screenshots to take from now on
     */
    void setScreensthotImageFormat(ScreenshotType type);
}
