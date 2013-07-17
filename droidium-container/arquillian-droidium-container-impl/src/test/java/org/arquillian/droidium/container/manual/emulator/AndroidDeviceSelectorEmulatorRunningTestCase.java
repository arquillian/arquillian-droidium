/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.arquillian.droidium.container.manual.emulator;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests connecting to the running emulator of specific avd and console port.
 *
 * You set the name of running AVD by specifying of</br>
 * <p>{@code -Demulator.running.avd.name=avd_name}</p>
 *
 * <p>{@code -Demulator.running.console.port=port_number}</p>
 *
 * at the Maven command line in connection with -Pmanual-test profile.
 * Default AVD name is "test01", default port number is 5554.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidDeviceSelectorEmulatorRunningTestCase extends AbstractContainerTestBase {

    private AndroidContainerConfiguration configuration;

    private AndroidSDK androidSDK;

    private final String RUNNING_EMULATOR_AVD_NAME = System.getProperty("emulator.running.avd.name", "test01");

    private final String RUNNING_EMULATOR_CONSOLE_PORT = System.getProperty("emulator.running.console.port", "5554");

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(RUNNING_EMULATOR_AVD_NAME);
        configuration.setConsolePort(RUNNING_EMULATOR_CONSOLE_PORT);
        androidSDK = new AndroidSDK(configuration);

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ContainerScoped.class, AndroidSDK.class, androidSDK);
    }

    @After
    public void disposeMocks() throws AndroidExecutionException {
        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bridge.disconnect();
    }

    @Test
    public void testGetRunningEmulator() {
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class)
                .getObjectStore().get(AndroidDevice.class);

        assertNotNull("Android device is null object!", runningDevice);

        assertEventFired(AndroidContainerStart.class, 1);
        assertEventFired(AndroidBridgeInitialized.class, 1);
        assertEventFired(AndroidDeviceReady.class, 1);
        assertEventFiredInContext(AndroidContainerStart.class, ContainerContext.class);
        assertEventFiredInContext(AndroidBridgeInitialized.class, ContainerContext.class);
        assertEventFiredInContext(AndroidDeviceReady.class, ContainerContext.class);
    }
}
