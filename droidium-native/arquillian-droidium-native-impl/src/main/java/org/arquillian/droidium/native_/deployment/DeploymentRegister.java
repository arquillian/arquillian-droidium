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
package org.arquillian.droidium.native_.deployment;

import java.util.ArrayList;
import java.util.List;

import org.arquillian.droidium.native_.spi.DroidiumDeployment;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Registers deployments for Android device.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentRegister<T extends DroidiumDeployment> {

    private final List<T> deployments = new ArrayList<T>();

    /**
     * Adds deployment into the register.
     *
     * @param deployment deployment to add
     * @throws IllegalArgumentException if {@code deployment} is a null object
     */
    public void add(T deployment) {
        Validate.notNull(deployment, "Deployment to add can not be a null object!");
        deployments.add(deployment);
    }

    /**
     * Gets firstly added deployment in the register.
     *
     * @return deployment which was added first or {@code null} if none was added yet
     */
    public T getFirst() {
        return deployments.size() == 0 ? null : deployments.get(0);
    }

    /**
     * Gets lastly added deployment into the register.
     *
     * @return deployment which was added last or {@code null} if none was added yet
     */
    public T getLast() {
        return deployments.size() == 0 ? null : deployments.get(deployments.size() - 1);
    }

    /**
     *
     * @param i
     * @return i-th deployment
     * @throws IndexOutOfBoundsException
     */
    public T get(int i) {
        return deployments.get(i);
    }

    /**
     * @return how much deployments we have registered so far
     */
    public int getSize() {
        return deployments.size();
    }

    /**
     * Gets deployment which is backed by specified archive.
     *
     * @param archive archive to get the deployment of
     * @return deployment bean of {@code archive} or null if there is no such mapping
     */
    public T get(Archive<?> archive) {
        for (T deployment : deployments) {
            if (deployment.getDeployment() == archive) {
                return deployment;
            }
        }
        return null;
    }

    /**
     *
     * @param deploymentName name of deployment to get
     * @return deployment of such {@code deploymentName} or null if no such deployment was found
     */
    public T get(String deploymentName) {
        for (T deployment : deployments) {
            if (deployment.getDeploymentName().equals(deploymentName)) {
                return deployment;
            }
        }
        return null;
    }

    public List<T> getAll() {
        return deployments;
    }

}
