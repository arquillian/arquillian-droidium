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

import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Registers deployments for Android device.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentRegister {

    private List<AndroidDeployment> deployments = new ArrayList<AndroidDeployment>();

    /**
     * Adds deployment into the register.
     *
     * @param deployment deployment to add
     * @throws IllegalArgumentException if {@code deployment} is a null object
     */
    public void add(AndroidDeployment deployment) {
        Validate.notNull(deployment, "Deployment to add can not be a null object!");
        deployments.add(deployment);
    }

    /**
     * Gets firstly added deployment in the register
     *
     * @return deployment which was added first or {@code null} if none deployment was added yet
     */
    public AndroidDeployment getFirst() {
        return deployments.size() == 0 ? null : deployments.get(0);
    }

    /**
     * Gets lastly added deployment in the register
     *
     * @return deployment which was added last or {@code null} if none deployment was added yet
     */
    public AndroidDeployment getLast() {
        return deployments.size() == 0 ? null : deployments.get(deployments.size() - 1);
    }

    /**
     *
     * @param i
     * @return i-th deployment
     * @throws IndexOutOfBoundsException
     */
    public AndroidDeployment get(int i) {
        return deployments.get(i);
    }

    /**
     * Gets deployment which is backed by specified archive.
     *
     * @param archive archive to get deployment of
     * @return {@code AndroidDeployment} of {@code archive}
     */
    public AndroidDeployment get(Archive<?> archive) {
        for (AndroidDeployment deployment : deployments) {
            if (deployment.getDeployArchive() == archive) {
                return deployment;
            }
        }
        return null;
    }

}
