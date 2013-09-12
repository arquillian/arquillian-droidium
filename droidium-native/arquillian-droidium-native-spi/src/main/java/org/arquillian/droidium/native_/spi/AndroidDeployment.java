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
package org.arquillian.droidium.native_.spi;

import java.io.File;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Holds deployment resources for Android device logically bound together representing one deployment unit.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeployment extends DroidiumDeployment {

    private Archive<?> deployArchive;

    private File resignedApk;

    private File deployApk;

    private String applicationBasePackage;

    private String applicationMainActivity;

    private String deploymentName;

    /**
     *
     * @param archive
     * @throws IllegalArgumentException if {@code archive} is a null object
     * @return this
     */
    public AndroidDeployment setDeployment(Archive<?> archive) {
        Validate.notNull(archive, "Archive to set can not be a null object!");
        this.deployArchive = archive;
        return this;
    }

    @Override
    public Archive<?> getDeployment() {
        return deployArchive;
    }

    @Override
    public String getDeploymentName() {
        return deploymentName;
    }

    public void setDeploymentName(String deploymentName) {
        this.deploymentName = deploymentName;
    }

    /**
     * Sets the resigned application - this application is meant to be actually installed on the target Android device.
     *
     * @param resignedApk
     * @throws IllegalArgumentException if {@code resignedApk} is a null object
     * @return this
     */
    public AndroidDeployment setResignedApk(File resignedApk) {
        Validate.notNull(resignedApk, "File to set can not be a null object!");
        this.resignedApk = resignedApk;
        return this;
    }

    public File getResignedApk() {
        return resignedApk;
    }

    /**
     * Application to deploy before it is resigned.
     *
     * @param deployApk
     * @throws IllegalArgumentException if {@code deployApk} is a null object
     * @return this
     */
    public AndroidDeployment setDeployApk(File deployApk) {
        Validate.notNull(deployApk, "File to set can not be a null object!");
        this.deployApk = deployApk;
        return this;
    }

    public File getDeployApk() {
        return deployApk;
    }

    /**
     *
     * @param applicationBasePackage Android base package of the application to be deployed
     * @throws IllegalArgumentException if {@code applicationBasePackage} is a null object or an empty string
     * @return this
     */
    public AndroidDeployment setApplicationBasePackage(String applicationBasePackage) {
        Validate.notNullOrEmpty(applicationBasePackage, "String to set can not be a null object nor an empty string!");
        this.applicationBasePackage = applicationBasePackage;
        return this;
    }

    public String getApplicationBasePackage() {
        return applicationBasePackage;
    }

    /**
     *
     * @param applicationMainActivity Android main application activity of the application to be deployed
     * @throws IllegalArgumentException if {@code applicationMainActivity} is a null object or an empty string
     * @return this
     */
    public AndroidDeployment setApplicationMainActivity(String applicationMainActivity) {
        Validate.notNullOrEmpty(applicationMainActivity, "String to set can not be a null object nor an empty string!");
        this.applicationMainActivity = applicationMainActivity;
        return this;
    }

    public String getApplicationMainActivity() {
        return applicationMainActivity;
    }

}
