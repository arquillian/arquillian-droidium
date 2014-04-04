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

import org.arquillian.droidium.native_.instrumentation.InstrumentationScanner;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Scans test class and resolves mapping between deployment names and their instrumentation configurations.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link BeforeClass}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * @see InstrumentationConfiguration
 */
public class DeploymentInstrumentationResolver {

    @Inject
    private Instance<DeploymentInstrumentationMapper> deploymentInstrumentationMapper;

    /**
     * Precedence is set here higher then the one in {@code DroneConfigurator} in order to be treated firstly in BeforeClass
     * context because once Drones get configured, we need this structure to be already initialized for further processing. We
     * can not hook just to {@code BeforeClass} nor {@code GenerateDeployment} because the order of these executions is random.
     *
     * @param event
     */
    public void resolveInstrumentationDeploymentMap(@Observes(precedence = 20) BeforeClass event) {
        Map<String, InstrumentationConfiguration> resolvedInstrumentation = InstrumentationScanner.scan(event.getTestClass());
        deploymentInstrumentationMapper.get().set(resolvedInstrumentation);
    }

}
