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

import java.util.Map;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ExtensionDef;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Before Droidium container is started, Arquillian descriptor is scanned for the presence of webdriver extensions. In case we
 * found such extension which has browser specified as "android", we assure there is native extension on class path. This logic
 * can help user significantly since Droidium container is tightly coupled with extensions and it is suspicious user would use
 * container alone (even it is possible).
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumExtensionsValidation {

    private static final Logger logger = Logger.getLogger(DroidiumExtensionsValidation.class.getName());

    public void validateDescriptor(@Observes BeforeStart event, ArquillianDescriptor descriptor) {
        if (!event.getDeployableContainer().getConfigurationClass().equals(AndroidContainerConfiguration.class)) {
            return;
        }

        Map<String, String> extensionProperties = null;

        for (ExtensionDef extension : descriptor.getExtensions()) {
            extensionProperties = extension.getExtensionProperties();
            if (extension.getExtensionName().startsWith("webdriver")) {
                String browser = extensionProperties.get("browser");
                if (browser != null && browser.equals("android")) {
                    if (!isNativeExtensionOnClassPath()) {
                        logger.warning("You have Droidium container as well as webdriver extension with browser \"android\" "
                            + "configured in arquillian.xml but there is not Droidium native extension on "
                            + "the class path. This setting is highly suspicious - it might not be wrong when you actually "
                            + "do not use Drone instance injected into test case for that extension however, in most cases, "
                            + "please add Droidium native on class path to be able to test native Android "
                            + "applications or web applications from Android device.");
                        break;
                    }
                }
            }
        }
    }

    private boolean isNativeExtensionOnClassPath() {
        return SecurityActions.isClassPresent("org.arquillian.droidium.native_.DroidiumNativeExtension");
    }

}
