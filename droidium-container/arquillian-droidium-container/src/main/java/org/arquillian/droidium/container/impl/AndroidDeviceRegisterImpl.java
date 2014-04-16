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
package org.arquillian.droidium.container.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceMetadata;
import org.arquillian.droidium.container.api.AndroidDeviceRegister;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;

/**
 * Holds {@link ContainerScoped} Android devices which are reachable outside of {@link ContainerContext}.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDeviceRegisterImpl implements AndroidDeviceRegister {

    private final Map<AndroidDevice, AndroidDeviceMetadata> register;

    public AndroidDeviceRegisterImpl() {
        register = new ConcurrentHashMap<AndroidDevice, AndroidDeviceMetadata>();
    }

    @Override
    public void put(AndroidDevice androidDevice, AndroidDeviceMetadata androidDeviceMetaData) {
        register.put(androidDevice, androidDeviceMetaData);
    }

    @Override
    public AndroidDeviceMetadata getMetadata(AndroidDevice androidDevice) {
        if (androidDevice != null) {
            return register.get(androidDevice);
        }
        return null;
    }

    @Override
    public boolean contains(AndroidDevice androidDevice) {
        return register.containsKey(androidDevice);
    }

    @Override
    public void remove(AndroidDevice device) {
        register.remove(device);
    }

    @Override
    public void removeByContainerQualifier(String containerQualifier) {

        Map.Entry<AndroidDevice, AndroidDeviceMetadata> toRemove = null;

        for (Map.Entry<AndroidDevice, AndroidDeviceMetadata> entry : register.entrySet()) {
            if (entry.getValue().getContainerQualifier().equals(containerQualifier)) {
                toRemove = entry;
                break;
            }
        }

        if (toRemove != null) {
            register.remove(toRemove);
        }
    }

    @Override
    public void addDeploymentForDevice(AndroidDevice device, String deploymentName) {
        AndroidDeviceMetadata metadata = register.get(device);
        if (metadata != null) {
            metadata.getDeploymentNames().add(deploymentName);
        }
        register.put(device, metadata);
    }

    @Override
    public AndroidDevice getByContainerQualifier(String containerQualifier) {
        for (Map.Entry<AndroidDevice, AndroidDeviceMetadata> entry : register.entrySet()) {
            if (entry.getValue().getContainerQualifier().equals(containerQualifier)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public AndroidDevice getByDeploymentName(String deploymentName) {
        for (Map.Entry<AndroidDevice, AndroidDeviceMetadata> entry : register.entrySet()) {
            if (entry.getValue().getDeploymentNames().contains(deploymentName)) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public int size() {
        return register.size();
    }

    @Override
    public AndroidDevice getSingle() {
        if (register.size() != 1) {
            throw new IllegalStateException("You can not get single AndroidDevice from register. There is more than 1 of it!");
        }
        return register.entrySet().iterator().next().getKey();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<AndroidDevice, AndroidDeviceMetadata> entry : register.entrySet()) {
            sb.append(entry.getKey()).append("\n")
                .append("container qualifier: ").append(entry.getValue().getContainerQualifier()).append("\n")
                .append("deployment names: ").append(entry.getValue().getDeploymentNames()).append("\n");
        }

        return sb.toString();
    }
}
