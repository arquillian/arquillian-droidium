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
package org.arquillian.droidium.container.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.arquillian.droidium.multiplecontainers.ContainerType;
import org.arquillian.droidium.multiplecontainers.MultipleLocalContainersRegistry;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;

/**
 * Removes Android container from container registry iff
 *
 * 1) Drone is on the class path
 * 2) There are webdriver extensions in arquillian.xml
 * 3) No webdriver extension asks for "android" browser
 *
 * https://issues.jboss.org/browse/ARQ-1577
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumExclusionDecider {

    private static final String DRONE_EXTENSION_CLASS_NAME = "org.jboss.arquillian.drone.DroneExtension";

    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    public void decideDroidiumStart(@Observes(precedence = Integer.MAX_VALUE) BeforeSuite event, ArquillianDescriptor descriptor) {
        if (SecurityActions.isClassPresent(DRONE_EXTENSION_CLASS_NAME)) {
            List<ExtensionDef> webDriverExtensions = getWebDriverExtensions(descriptor);
            if (webDriverExtensions.isEmpty()) {
                return;
            }

            for (ExtensionDef webdriverExtension : webDriverExtensions) {
                if (isAskingForAndroid(webdriverExtension)) {
                    return;
                }
            }

            ((MultipleLocalContainersRegistry) containerRegistry.get()).remove(ContainerType.ANDROID);
        }
    }

    private boolean isAskingForAndroid(ExtensionDef webdriverExtension) {
        for (Map.Entry<String, String> entry : webdriverExtension.getExtensionProperties().entrySet()) {
            if (entry.getKey().equals("browser") || entry.getKey().equals("browserName")) {
                if (entry.getValue().equals("android")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<ExtensionDef> getWebDriverExtensions(ArquillianDescriptor descriptor) {

        List<ExtensionDef> webDriverExtensions = new ArrayList<ExtensionDef>();

        for (ExtensionDef extension : descriptor.getExtensions()) {
            if (extension.getExtensionName().startsWith("webdriver")) {
                webDriverExtensions.add(extension);
            }
        }

        return webDriverExtensions;
    }

}
