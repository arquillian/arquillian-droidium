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
package org.arquillian.droidium.multiplecontainers;

import java.util.Collection;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.Validate;

/**
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ContainerGuesser {

    public static final String ADAPTER_IMPL_CONFIG_STRING = "adapterImplClass";

    public static boolean isDroidiumContainer(ContainerDef containerDef) {
        if (isContainerOfType(ContainerType.DROIDIUM, containerDef)
            || isContainerOfType(ContainerType.ANDROID, containerDef)) {
            return true;
        }
        return hasAndroidContainerSpecificProperties(containerDef);
    }

    private static boolean hasAndroidContainerSpecificProperties(ContainerDef containerDef) {
        Map<String, String> properties = containerDef.getContainerProperties();

        if (properties.containsKey("avdName")
            || properties.containsKey("serialId")
            || properties.containsKey("consolePort")
            || properties.containsKey("emulatorOptions")) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    public static boolean isDroidiumContainer(ContainerDef containerDef, Collection<DeployableContainer> containers) {
        // double check that it is not Android / Droidium
        if (ContainerGuesser.isDroidiumContainer(containerDef)) {
            if (ContainerGuesser.getContainerAdapter(ContainerType.getAdapterClassNamePrefix(ContainerType.ANDROID), containers) != null) {
                return true;
            }
        }

        return false;
    }

    public static boolean isJBossContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.JBOSS, containerDef);
    }

    public static boolean isEAPContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.EAP, containerDef);
    }

    public static boolean isWildFlyContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.WILDFLY, containerDef);
    }

    public static boolean isGlassFishContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.GLASSFISH, containerDef);
    }

    public static boolean isTomeeContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.TOMEE, containerDef);
    }

    public static boolean isOpenshiftContainer(ContainerDef containerDef) {
        return isContainerOfType(ContainerType.OPENSHIFT, containerDef);
    }

    public static boolean isContainerOfType(ContainerType type, ContainerDef containerDef) {
        return isContainerOfType(type, containerDef.getContainerName());
    }

    public static boolean isContainerOfType(ContainerType type, String containerQualifier) {
        return containerQualifier.toLowerCase().trim().contains(type.toString());
    }

    @SuppressWarnings("rawtypes")
    public static DeployableContainer<?> parseContainer(ContainerType type, Collection<DeployableContainer> containers) {
        for (DeployableContainer<?> container : containers) {
            if (container.getClass().getName().startsWith(ContainerType.getAdapterClassNamePrefix(type))) {
                return container;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static DeployableContainer<?> guessDeployableContainer(ContainerDef containerDef,
        Collection<DeployableContainer> containers) {

        for (ContainerType type : ContainerType.values()) {
            if (ContainerGuesser.isContainerOfType(type, containerDef)) {
                return ContainerGuesser.parseContainer(type, containers);
            }
        }

        return null;
    }

    @SuppressWarnings("rawtypes")
    public static DeployableContainer<?> getContainerAdapter(String adapterImplClass, Collection<DeployableContainer> containers) {
        Validate.notNullOrEmpty(adapterImplClass, "The value of " + ADAPTER_IMPL_CONFIG_STRING + " can not be a null object "
            + "nor an empty string!");

        Class<?> foundAdapter = null;

        if (SecurityActions.isClassPresent(adapterImplClass)) {
            foundAdapter = SecurityActions.loadClass(adapterImplClass);
        } else {
            return null;
        }

        for (DeployableContainer<?> container : containers) {
            if (foundAdapter.isInstance(container)) {
                return container;
            }
        }

        return null;
    }

    public static boolean hasAdapterImplClassProperty(ContainerDef containerDef) {
        for (Map.Entry<String, String> entry : containerDef.getContainerProperties().entrySet()) {
            if (entry.getKey().equals(ADAPTER_IMPL_CONFIG_STRING)) {
                return true;
            }
        }
        return false;
    }

    public static String getAdapterImplClassValue(ContainerDef containerDef) {
        return containerDef.getContainerProperties().get(ADAPTER_IMPL_CONFIG_STRING).trim();
    }

}
