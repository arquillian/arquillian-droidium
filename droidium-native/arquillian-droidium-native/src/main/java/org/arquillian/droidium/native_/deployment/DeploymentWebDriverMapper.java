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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Maps deployment names taken from {@code @Deployment} methods to extension qualifiers from arquillian.xml. The logic behind
 * this is that when you have some {@code @Deployment} you want to instrument, you have to put {@code @Instrumentable} on it.
 * There is a port via which the instance of Drone talks to Android device. So then, every instrumentable {@code @Deployment} is
 * backed by Drone extension in arquillian.xml. This class holds this mapping - deployment name and extension qualifier.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentWebDriverMapper {

    private Map<String, String> map = new ConcurrentHashMap<String, String>();

    /**
     *
     * @param deploymentWebDriverMap
     * @throws IllegalArgumentException if {@code deploymentWebDriverMap} is a null object
     */
    public void set(Map<String, String> deploymentWebDriverMap) {
        Validate.notNull(deploymentWebDriverMap, "Map you are trying to pass can not be a null object!");
        map = deploymentWebDriverMap;
    }

    /**
     *
     * @return the whole mapping
     */
    public Map<String, String> get() {
        return map;
    }

    /**
     *
     * @param deploymentName name of deployment, taken from {@code @Deployment} method
     * @param extensionQualifier extension qualifier for Drone extension which operates on the given deployment
     */
    public void add(String deploymentName, String extensionQualifier) {
        Validate.notNullOrEmpty(deploymentName, "Deployment name can not be a null object!");
        Validate.notNullOrEmpty(extensionQualifier, "Extension qualifier for deployment '" + deploymentName
            + "' can not be a null object!");
        map.put(deploymentName, extensionQualifier);
    }

    /**
     *
     * @param deployment name of deployment you want to know extension name of
     * @return extension name (extension qualifier) which acts on this deployment
     */
    public String getExtensionName(String deployment) {
        return map.get(deployment);
    }

    /**
     *
     * @param extensionQualifier name of extension you want to know deployment name of
     * @return deployment name from deployment method which is backed by specified extension qualifier or null if there is not
     *         any mapping
     */
    public String getDeploymentName(String extensionQualifier) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(extensionQualifier)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
