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
package org.arquillian.droidium.native_.impl;

import java.io.File;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Holds deployment resources logically bound together representing one deployment unit.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeployment {

    private Archive<?> deployArchive;

    private File resignedApk;

    private File deployApk;

    private String applicationBasePackage;

    private String applicationMainActivity;

    /**
     *
     * @param archive
     * @throws IllegalArgumentException if {@code archive} is a null object
     */
    public void setDeployArchive(Archive<?> archive) {
        Validate.notNull(archive, "Archive to set can not be a null object!");
        this.deployArchive = archive;
    }

    /**
     *
     * @param resignedApk
     * @throws IllegalArgumentException if {@code resignedApk} is a null object
     */
    public void setResignedApk(File resignedApk) {
        Validate.notNull(resignedApk, "File to set can not be a null object!");
        this.resignedApk = resignedApk;
    }

    /**
     *
     * @param deployApk
     * @throws IllegalArgumentException if {@code deployApk} is a null object
     */
    public void setDeployApk(File deployApk) {
        Validate.notNull(deployApk, "File to set can not be a null object!");
        this.deployApk = deployApk;
    }

    /**
     *
     * @param applicationBasePackage
     * @throws IllegalArgumentException if {@code applicationBasePackage} is a null object or an empty string
     */
    public void setApplicationBasePackage(String applicationBasePackage) {
        Validate.notNullOrEmpty(applicationBasePackage, "String to set can not be a null object nor empty!");
        this.applicationBasePackage = applicationBasePackage;
    }

    /**
     *
     * @param applicationMainActivity
     * @throws IllegalArgumentException if {@code applicationMainActivity} is a null object or an empty string
     */
    public void setApplicationMainActivity(String applicationMainActivity) {
        Validate.notNullOrEmpty(applicationMainActivity, "String to set can not be a null object nor empty!");
        this.applicationMainActivity = applicationMainActivity;
    }

    public Archive<?> getDeployArchive() {
        return deployArchive;
    }

    public File getResignedApk() {
        return resignedApk;
    }

    public File getDeployApk() {
        return deployApk;
    }

    public String getApplicationBasePackage() {
        return applicationBasePackage;
    }

    public String getApplicationMainActivity() {
        return applicationMainActivity;
    }
}
