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
package org.arquillian.droidium.native_.instrumentation;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arquillian.droidium.native_.configuration.DroneConfigurationHolder;
import org.arquillian.droidium.native_.deployment.AndroidDeploymentRegister;
import org.arquillian.droidium.native_.deployment.DeploymentWebDriverMapper;
import org.arquillian.droidium.native_.deployment.ExtensionDroneMapper;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.arquillian.droidium.native_.spi.event.AfterInstrumentationPerformed;
import org.arquillian.droidium.native_.spi.event.BeforeInstrumentationPerformed;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.spi.event.BeforeDroneInstantiated;

/**
 * Decides if Drone callable, before it is instantiated, instruments some deployment. If it does, we have to install Selendroid
 * server which instruments that deployment. Selendroid server is installed on the device afterwards in order to provide just
 * instantiated Drone callable the possibility to initialize itself by some internal HTTP ping-pong between Drone and Android
 * device. It is important to realize that Selendroid server is uninstalled upon Drone destruction so in order to save
 * computational resources and time, it is recommended to use class scoped Drones instead of method ones.<br>
 * <br>
 * Observes:
 * <ul>
 * <li>{@link BeforeDroneInstantiated}</li>
 * </ul>
 * Fires:
 * <ul>
 * <li>{@link BeforeInstrumentationPerformed}</li>
 * <li>{@link PerformInstrumentation}</li>
 * <li>{@link AfterInstrumentationPerformed}</li>
 * </ul>
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationPerformDecider {

    private static final Logger logger = Logger.getLogger(InstrumentationPerformDecider.class.getName());

    @Inject
    private Instance<DeploymentInstrumentationMapper> instrumentationMapper;

    @Inject
    private Instance<AndroidDeploymentRegister> androidDeploymentRegister;

    @Inject
    private Instance<ExtensionDroneMapper> extensionDroneMapper;

    @Inject
    private Instance<DeploymentWebDriverMapper> deploymentWebDriverMapper;

    @Inject
    private Event<BeforeInstrumentationPerformed> beforeInstrumentationPerformed;

    @Inject
    private Event<PerformInstrumentation> performInstumentation;

    @Inject
    private Event<AfterInstrumentationPerformed> afterInstrumentationPerformed;

    public void decidePerformingInstrumentation(@Observes BeforeDroneInstantiated event) {

        String deploymentName = null;

        for (Map.Entry<String, DroneConfigurationHolder> entry : extensionDroneMapper.get().get().entrySet()) {
            if (entry.getValue().getQualifier().equals(event.getQualifier().getSimpleName().toLowerCase())) {
                deploymentName = deploymentWebDriverMapper.get().getDeploymentName(entry.getKey());
            }
        }

        InstrumentationConfiguration instrumentationConfiguration = instrumentationMapper.get().getDeploymentName(
            deploymentName);

        if (instrumentationConfiguration != null) {

            logger.log(Level.FINE, "instrumentation against deployment {0}", new Object[] { deploymentName });

            beforeInstrumentationPerformed
                .fire(new BeforeInstrumentationPerformed(deploymentName, instrumentationConfiguration));
            performInstumentation.fire(new PerformInstrumentation(deploymentName, instrumentationConfiguration));
            afterInstrumentationPerformed.fire(new AfterInstrumentationPerformed(deploymentName, instrumentationConfiguration));
        }
    }
}
