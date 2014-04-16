/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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

/**
 * Helper structure to hold {@link AndroidDevice} and related {@link AndroidDeviceMetadata} together.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public interface AndroidDeviceRegister {

    /**
     * Puts {@link AndroidDevice} and related {@link AndroidDeviceMetadata} to register.
     *
     * @param androidDevice device to put
     * @param androidDeviceMetaData metadata to put
     */
    void put(AndroidDevice androidDevice, AndroidDeviceMetadata androidDeviceMetaData);

    /**
     * Gets {@link AndroidDeviceMetadata} for given {@link AndroidDevice}.
     *
     * @param androidDevice {@link AndroidDevice} to get {@link AndroidDeviceMetadata} for
     * @return metadata for given {@code androidDevice}
     */
    AndroidDeviceMetadata getMetadata(AndroidDevice androidDevice);

    /**
     *
     * @param androidDevice {@link AndroidDevice} to test the presence of
     * @return true if register contains {@code androidDevice}, false otherwise
     */
    boolean contains(AndroidDevice androidDevice);

    /**
     *
     * @param device device to remove from register, it deletes related metadata as well
     */
    void remove(AndroidDevice device);

    /**
     *
     * @param containerQualifier container qualifier of {@link AndroidDevice} to remove, it deletes related metadata as well
     */
    void removeByContainerQualifier(String containerQualifier);

    /**
     *
     * @param device device to add a deployment to
     * @param deploymentName deployment name to add
     */
    void addDeploymentForDevice(AndroidDevice device, String deploymentName);

    /**
     *
     * @param containerQualifier container qualifier to get an {@link AndroidDevice} of
     * @return {@link AndroidDevice} by given {@code containerQualifier}
     */
    AndroidDevice getByContainerQualifier(String containerQualifier);

    /**
     *
     * @param deploymentName name from deployment method to get {@link AndroidDevice} of which is logically bound to it
     * @return {@link AndroidDevice} which is logically bound to that {@code deploymentName}
     */
    AndroidDevice getByDeploymentName(String deploymentName);

    /**
     *
     * @return single {@link AndroidDevice}
     * @throws IllegalStateException in case there is more then one {@link AndroidDevice} to get.
     */
    AndroidDevice getSingle() throws IllegalStateException;

    /**
     *
     * @return number of entries in this register
     */
    int size();

}