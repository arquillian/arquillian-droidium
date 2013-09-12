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
package org.arquillian.droidium.native_.spi.event;

import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;

/**
 * Event representing that underlying package should be instrumented, in our case by Selendroid server.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class PerformInstrumentation {

    private final String deploymentName;

    private final InstrumentationConfiguration instrumentationConfiguration;

    /**
     *
     * @param deploymentName
     * @param configuration
     * @throws InvalidInstrumentationConfigurationException if {@code configuration} is invalid
     */
    public PerformInstrumentation(String deploymentName, InstrumentationConfiguration configuration) {
        this.deploymentName = deploymentName;
        this.instrumentationConfiguration = configuration;

        this.instrumentationConfiguration.validate();
    }

    public String getDeploymentName() {
        return deploymentName;
    }

    public InstrumentationConfiguration getConfiguration() {
        return instrumentationConfiguration;
    }
}
