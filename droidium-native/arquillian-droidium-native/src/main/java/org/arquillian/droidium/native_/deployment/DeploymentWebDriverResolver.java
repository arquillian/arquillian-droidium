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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.instrumentation.DeploymentInstrumentationMapper;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.arquillian.droidium.native_.spi.event.AfterExtensionDroneMapping;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 * Maps deployment names to extension qualifiers.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link AfterExtensionDroneMapping}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DeploymentWebDriverResolver {

    private static final Logger logger = Logger.getLogger(DeploymentWebDriverResolver.class.getName());

    @Inject
    private Instance<DeploymentWebDriverMapper> deploymentWebDriverMapper;

    @Inject
    private Instance<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    /**
     * After mapping from extensions to Drones is done, we need hook to this event in order to map deployment name of package we
     * need to control with WebDriver instance to its extension name. Once all Drones are instantiated and configured, we can
     * match activity class to WebDriver instance which it instruments - so doing multiple instrumentation and starting activity
     * on request.
     *
     * @param event
     */
    public void resolveDeploymentWebDriverMap(@Observes AfterExtensionDroneMapping event) {

        for (Map.Entry<String, InstrumentationConfiguration> deploymentInstrumentationEntry : deploymentInstrumentationMapper
            .get().get().entrySet()) {

            if (deploymentInstrumentationEntry.getValue().getPort().equals(event.getPort())) {
                deploymentWebDriverMapper.get().add(deploymentInstrumentationEntry.getKey(), event.getExtensionName());
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            for (Map.Entry<String, String> webDriverMapperEntry : deploymentWebDriverMapper.get().get().entrySet()) {
                System.out.println("Deployment name: " + webDriverMapperEntry.getKey() + "\n" +
                    "Extension qualifier: " + webDriverMapperEntry.getValue());
            }
        }
    }
}
