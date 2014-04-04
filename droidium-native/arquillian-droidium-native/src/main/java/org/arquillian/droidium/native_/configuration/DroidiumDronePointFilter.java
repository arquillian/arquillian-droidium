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
package org.arquillian.droidium.native_.configuration;

import org.arquillian.droidium.native_.webdriver.AndroidDriver;
import org.jboss.arquillian.drone.spi.DroneContext;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.DronePointContext;
import org.jboss.arquillian.drone.spi.DronePointFilter;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.WebDriver;

/**
 * Filter used to determine if some {@link DronePoint} is related to Android looking on {@code browserName} in configuration and
 * conforming to WebDriver interface. In case Drone is already instantiated, it checks if it is indeed {@link AndroidDriver}.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumDronePointFilter implements DronePointFilter<WebDriver> {

    @Override
    public boolean accepts(DroneContext context, DronePoint<? extends WebDriver> dronePoint) {
        DronePointContext<?> dronePointContext = context.get(dronePoint);

        if (dronePointContext.hasConfiguration()) {
            if (dronePointContext.isInstantiated()) {
                return dronePointContext.getInstance() instanceof AndroidDriver;
            } else {
                final WebDriverConfiguration configuration = dronePointContext.getConfigurationAs(WebDriverConfiguration.class);
                return configuration.getBrowser().equals("android");
            }
        }

        return false;
    }
}
