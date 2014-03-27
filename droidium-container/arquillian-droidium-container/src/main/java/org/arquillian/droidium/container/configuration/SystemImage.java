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
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enumeration of all (up to March 2014) possible types of system images.
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
class SystemImage {

    private static final Logger log = Logger.getLogger(SystemImage.class.getName());

    private static final String RAMDISK_NAME = "ramdisk.img";

    private final String name;

    private final String abi;

    private SystemImage(String name, String abi) {
        this.name = name;
        this.abi = abi;
    }

    /**
     * Returns System Images for given target. Uses {@code abi} to filet
     *
     * @param sdkPath
     * @param target
     * @param abi can be null or empty string
     * @return
     * @throws AndroidContainerConfigurationException If there is no ABI or multiple ABI matched abi string
     */
    public static SystemImage getSystemImageForTarget(File sdkPath, Target target, String abi)
        throws AndroidContainerConfigurationException {
        List<SystemImage> images = getSystemImagesForTarget(sdkPath, target);

        // there is an empty ABI provided in configuration, return the only one available
        if (abi == null || "".equals(abi)) {
            if (images.size() == 1) {
                return images.iterator().next();
            }
            return guessAbi(images, target);
        }

        for (SystemImage si : images) {
            // check for full name of abi
            if (abi.contains("/")) {
                if (abi.equals(si.getAbi())) {
                    return si;
                }
            }
            // check for name without default part
            else {
                if (("default/" + abi).equals(si.getAbi())) {
                    return si;
                }
            }
        }

        if (images.size() == 1) {
            // FIXME this is original Droidium behavior. Does it make sense?!
            log.log(Level.WARNING,
                "ABI property from configuration ({0}) does not match only available image {1}. Arquillian configuration will be ignored.",
                new Object[] { abi, images.iterator().next().getAbi() });
            return images.iterator().next();
        }

        // there are more than 1 images
        return guessAbi(images, target);

    }

    /**
     * Returns all installed images for the target
     *
     * @param sdkPath Path to SDK directory
     * @param target target we want to create
     * @return
     * @throws AndroidContainerConfigurationException if no images were found
     */
    private static List<SystemImage> getSystemImagesForTarget(File sdkPath, Target target)
        throws AndroidContainerConfigurationException {

        File dir = null;
        if (target.isGoogleAddon()) {
            dir = new File(new File(sdkPath, AndroidSDK.ADD_ONS_FOLDER_NAME), target.getImagesSubdirectory());
            // google might have release new version of image
            if (!dir.exists()) {
                List<File> addons = FileUtils.listDirectories(new File(sdkPath, AndroidSDK.ADD_ONS_FOLDER_NAME));
                String addonName = target.getImagesSubdirectory();
                if (addonName.endsWith("/images")) {
                    addonName = addonName.substring(0, addonName.length() - "/images".length());
                }
                // find a directory that starts with with same addon name
                for (File addon : addons) {
                    if (addon.getName().startsWith(addonName) && new File(addon, "images").exists()) {
                        dir = addon;
                        break;
                    }
                }
            }
        }
        else {
            dir = new File(new File(sdkPath, AndroidSDK.SYSTEM_IMAGES_FOLDER_NAME), target.getImagesSubdirectory());
        }

        if (!dir.exists()) {
            throw new AndroidContainerConfigurationException("Unable to find out images for target: " + target
                + ", directory: " + dir + " does not exist.");
        }

        // special handling for Android 2.3 Google API image, where the dir is directly the image
        if (target.isGoogleAddon() && target.getApiLevel() == 10 && new File(dir, RAMDISK_NAME).exists()) {
            return Collections.singletonList(new SystemImage(target.getName(), "armeabi"));
        }

        List<SystemImage> availableImages = new ArrayList<SystemImage>();
        List<File> directories = FileUtils.listDirectories(dir);
        for (File imageCandidate : directories) {
            // we have found an image but there might be a parent - default, android-wear, whatever
            // so we go one extra level up to figure out complete ABI name
            if (new File(imageCandidate, RAMDISK_NAME).exists()) {
                File parentDir = imageCandidate.getParentFile();
                // direct ancestors, we hit default image
                // alternatively, we hit a directory under images, which we consider default as well
                if (dir.equals(parentDir) || (parentDir != null && "images".equals(parentDir.getName()))) {
                    availableImages.add(new SystemImage(target.getName(), "default/" + imageCandidate.getName()));
                }
                else if (parentDir != null) {
                    availableImages.add(new SystemImage(target.getName(), parentDir.getName() + "/" + imageCandidate.getName()));
                }
                else {
                    throw new AndroidContainerConfigurationException("Unable to figure out what images are available for target: "
                        + target);
                }
            }
        }

        if (availableImages.isEmpty()) {
            throw new AndroidContainerConfigurationException("No system images were available for target: " + target);
        }

        // sort descending to ABI names to be filesystem independent
        Collections.sort(availableImages, new Comparator<SystemImage>() {
            @Override
            public int compare(SystemImage o1, SystemImage o2) {
                return o2.getAbi().compareTo(o1.getAbi());
            }
        });

        return availableImages;
    }

    private static SystemImage guessAbi(List<SystemImage> images, Target target) {

        // there are more images, we have to figure out which one to use
        StringBuilder sb = new StringBuilder("There were more ABIs for target \"")
            .append(target)
            .append("\" available: ");
        String delimiter = "";
        for (SystemImage si : images) {
            sb.append(delimiter).append(si.getAbi());
            delimiter = ", ";
        }
        sb.append(". Please use \"abi\" configuration property to select ABI. At this moment, Droidium will autoselect ")
            .append(images.iterator().next().getAbi())
            .append(".");

        log.log(Level.WARNING, sb.toString());

        return images.iterator().next();
    }

    public String getName() {
        return name;
    }

    public String getAbi() {
        return abi;
    }

    @Override
    public String toString() {
        return "image: " + name + ", abi: " + abi;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((abi == null) ? 0 : abi.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SystemImage other = (SystemImage) obj;
        if (abi == null) {
            if (other.abi != null)
                return false;
        } else if (!abi.equals(other.abi))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    private static final class FileUtils {
        static List<File> listDirectories(File rootDirectory) {
            List<File> dirs = new ArrayList<File>();
            // for (File candidate : rootDirectory.listFiles()) {
            // listDirectories(candidate, dirs);
            // }
            listDirectories(rootDirectory, dirs);
            return dirs;
        }

        static void listDirectories(File rootDirectory, List<File> acc) {
            if (rootDirectory == null || !rootDirectory.isDirectory()) {
                return;
            }

            acc.add(rootDirectory);
            for (File candidate : rootDirectory.listFiles()) {
                listDirectories(candidate, acc);
            }
        }
    }
}