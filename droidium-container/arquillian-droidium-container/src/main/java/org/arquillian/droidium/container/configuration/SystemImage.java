/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.droidium.container.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of all (up to March 2014) possible types of system images.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
enum SystemImage {

    X86("x86"),
    ARMEABIV7A("armeabi-v7a"),
    ANDROID_WEAR_ARMEABIV7A("android-wear/armeabi-v7a"),
    MIPS("mips");

    private String name;

    private SystemImage(String name) {
        this.name = name;
    }

    /**
     * Returns all installed images for given platform
     *
     * @param sdkPath Path to SDK directory
     * @param platform Platform
     * @return
     */
    public static List<SystemImage> getSystemImagesForPlatform(File sdkPath, String apiLevel) {

        File dir = new File(new File(sdkPath, AndroidSDK.SYSTEM_IMAGES_FOLDER_NAME), "android-" + apiLevel);

        if (!dir.exists()) {
            return Collections.emptyList();
        }

        List<SystemImage> availableImages = new ArrayList<SystemImage>();
        for (SystemImage imageCandidate : SystemImage.values()) {
            File pathToImage = new File(dir, imageCandidate.name);
            if (pathToImage.exists() && pathToImage.isDirectory()) {
                availableImages.add(imageCandidate);
            }
        }

        // for android-19 up images are in "android-wear" dir or in "default"
        for (SystemImage imageCandidate : SystemImage.values()) {
            File pathToImage = new File(new File(dir, "default"), imageCandidate.name);
            if (pathToImage.exists() && pathToImage.isDirectory()) {
                availableImages.add(imageCandidate);
            }
        }

        return availableImages;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}