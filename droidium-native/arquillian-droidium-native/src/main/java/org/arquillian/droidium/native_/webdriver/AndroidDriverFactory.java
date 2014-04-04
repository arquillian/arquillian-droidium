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
package org.arquillian.droidium.native_.webdriver;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DronePoint;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

/**
 * Manages lifecycle of {@code AndroidDriver}.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidDriverFactory implements
    Configurator<AndroidDriver, WebDriverConfiguration>,
    Instantiator<AndroidDriver, WebDriverConfiguration>,
    Destructor<AndroidDriver> {

    private static final Logger logger = Logger.getLogger(AndroidDriverFactory.class.getName());

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void destroyInstance(AndroidDriver driver) {
        driver.quit();
    }

    @Override
    public AndroidDriver createInstance(WebDriverConfiguration configuration) {
        URL remoteAddress = configuration.getRemoteAddress();

        // default remote address
        if (Validate.empty(remoteAddress)) {
            remoteAddress = WebDriverConfiguration.DEFAULT_REMOTE_URL;
            logger.log(Level.INFO, "Property \"remoteAdress\" was not specified, using default value of {0} for AndroidDriver",
                WebDriverConfiguration.DEFAULT_REMOTE_URL);
        }

        Validate.isValidUrl(remoteAddress, "Remote address must be a valid url, " + remoteAddress);

        return SecurityActions.newInstance(configuration.getImplementationClass(),
            new Class<?>[] { URL.class, Capabilities.class },
            new Object[] { remoteAddress, new DesiredCapabilities(configuration.getCapabilities()) },
            AndroidDriver.class);
    }

    @Override
    public WebDriverConfiguration createConfiguration(ArquillianDescriptor descriptor, DronePoint<AndroidDriver> injectionPoint) {
        WebDriverConfiguration configuration = new WebDriverConfiguration(new AndroidBrowserCapabilities());
        configuration.configure(descriptor, injectionPoint.getQualifier());
        return configuration;
    }

}