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
import org.arquillian.droidium.native_.spi.exception.InvalidInstrumentationConfigurationException;
import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Event representing that underlying package should be instrumented, in our case by Selendroid server.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class PerformInstrumentation {

    private final Archive<?> archive;

    private final InstrumentationConfiguration instrumentationConfiguration;

    /**
     *
     * @param archive Android package to remove the instrumentation from
     * @param configuration Instrumentation configuration
     * @throws IllegalArgumentException if {@code archive} or {@code configuration} is a null object
     * @throws InvalidInstrumentationConfigurationException when {@code configuration} is invalid
     */
    public PerformInstrumentation(Archive<?> archive, InstrumentationConfiguration configuration) {
        Validate.notNull(archive, "APK package to instrument can not be a null object!");
        Validate.notNull(configuration, "Instrumentation configuration can not be a null object");
        this.archive = archive;
        this.instrumentationConfiguration = configuration;

        this.instrumentationConfiguration.validate();
    }

    /**
     *
     * @return archive on which the instrumentation will occur
     */
    public Archive<?> getPackage() {
        return archive;
    }

    /**
     *
     * @return instrumentation configuration for the underlying deployment archive parsed from {@code @Instrumentable}
     *         annotation placed on {@code @Deployment} method.
     */
    public InstrumentationConfiguration getConfiguration() {
        return instrumentationConfiguration;
    }
}
