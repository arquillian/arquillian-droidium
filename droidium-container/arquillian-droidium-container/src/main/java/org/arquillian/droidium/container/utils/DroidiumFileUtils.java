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
package org.arquillian.droidium.container.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Set of utility methods for Droidium regarding of file and directory management. It creates temporary directory where all
 * resources needed while the test execution is running are stored e.g. rebuilt packages are stored there.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumFileUtils {

    private static final Logger logger = Logger.getLogger(DroidiumFileUtils.class.getName());

    private static File tmpDir = null;

    private static final IdentifierGenerator<FileType> aig = new AndroidIdentifierGenerator();

    /**
     * Removes directory
     *
     * @param dir directory to remove
     */
    public static void removeDir(File dir) {
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException ex) {
            logger.log(Level.INFO, "Unable to delete directory {0}. Reason: {1}",
                new Object[] { dir.getAbsolutePath(), ex.getMessage() });
        }
    }

    public static void removeDir(String dir) {
        DroidiumFileUtils.removeDir(new File(dir));
    }

    /**
     * Gets temporary directory created by {@link #createTmpDir(File)}
     *
     * @return temporary directory where Droidium puts all resources, APKs and files during test execution
     */
    public static File getTmpDir() {
        return DroidiumFileUtils.tmpDir;
    }

    /**
     * Creates directory with random name.
     *
     * Temporary resource directory can be specified by {@code tmpDir} configuration property in extension configuration in
     * arquillian.xml.
     */
    public static void createTmpDir(File parent) {
        try {
            DroidiumFileUtils.tmpDir = parent;
            boolean created = DroidiumFileUtils.tmpDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Unable to create temporary directory " + DroidiumFileUtils.tmpDir.getAbsolutePath());
            }
        } catch (SecurityException ex) {
            logger.severe("Security manager denies to create the working dir in " + parent.getAbsolutePath());
            throw new RuntimeException("Unable to create working directory in " + parent.getAbsolutePath());
        }
    }

    /**
     * Creates empty file saved under {@code parent}
     *
     * @param parent parent directory of file to create
     * @return empty file saved in parent with random name
     */
    public static File createRandomEmptyFile(File parent) {
        File temp;

        try {
            do {
                temp = new File(parent, aig.getIdentifier(FileType.FILE));
            } while (!temp.createNewFile());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create file in " + parent.getAbsolutePath());
        }
        return temp;
    }

    /**
     * Copies {@code src} file to {@code dest} directory.
     *
     * @param src source file to copy
     * @param dest destination directory where file is copied
     */
    public static File copyFileToDirectory(File src, File dest) {
        try {
            FileUtils.copyFileToDirectory(src, dest);
            return new File(dest, src.getName());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to copy " + src.getAbsolutePath() + " to " + dest.getAbsolutePath());
        }
    }

    /**
     * @return random name for APK file name (ends with .apk suffix)
     */
    public static String getRandomAPKFileName() {
        return aig.getIdentifier(FileType.APK);
    }

    /**
     * Exports archive to file. If {@code toFile} exists, it will be deleted. If deletion fails, we try to overwrite it during
     * exporting on behalf of ShrinkWrap.
     *
     * @param fromArchive archive to export its content from
     * @param toFile file to export the content from archive to
     * @return {@code toFile}
     */
    public static File export(Archive<?> fromArchive, File toFile) {
        Validate.notNull(fromArchive, "Archive to export from can not be a null object!");
        Validate.notNull(toFile, "File to export archive to can not be a null object!");
        if (toFile.exists() && toFile.isFile()) {
            if (!toFile.delete()) {
                logger.fine("File to export the archive to exists and it can not be removed.");
            }
        }

        fromArchive.as(ZipExporter.class).exportTo(toFile, true);
        return toFile;
    }

    /**
     * Exports archive to file. Archive will be exported to file placed in temporary directory. This method has
     * to be called after {@link #createTmpDir(File)}.
     *
     * @param fromArchive archive to export
     * @return exported archive as file
     * @throws IllegalStateException if temporary directory to export archive by default is null (not set)
     */
    public static File export(Archive<?> fromArchive) {
        Validate.notNull(fromArchive, "Archive to export from can not be a null object!");
        if (DroidiumFileUtils.getTmpDir() == null) {
            throw new IllegalStateException("Please call DroidiumFileUtils.createTmpDir(File) before calling this method.");
        }
        File toFile = new File(DroidiumFileUtils.getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());
        return export(fromArchive, toFile);
    }

}
