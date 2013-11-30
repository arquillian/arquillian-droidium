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
import org.arquillian.droidium.container.impl.AndroidEmulator;
import org.arquillian.droidium.container.impl.AndroidEmulatorShutdown;
import org.arquillian.droidium.container.impl.AndroidEmulatorStartup;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.spi.event.AndroidEmulatorShuttedDown;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests starting of an emulator when AVD is offline.
 *
 * You set the name of AVD you want to start by specifying of</br>
 * <p>{@code -Demulator.to.run.avd.name=avd_name}</p>
 *
 * <p>{@code -Demulator.to.run.console.port=port_number}</p>
 *
 * at the Maven command line in connection with android-manual-emulator profile.
 * Default AVD name is "test01", default port number is 5556.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidEmulatorStartupAVDcreatedTestCase extends AbstractContainerTestBase {

    private AndroidContainerConfiguration configuration;

    private AndroidSDK androidSDK;

    private ProcessExecutor processExecutor;

    private static final String EMULATOR_AVD_NAME = System.getProperty("emulator.to.run.avd.name", "test01");

    private static final String EMULATOR_CONSOLE_PORT = System.getProperty("emulator.to.run.console.port", "5556");

    private static final String EMULATOR_STARTUP_TIMEOUT = System.getProperty("emulator.startup.timeout", "600");

    private static final String EMULATOR_OPTIONS = "-no-audio -no-window -memory 343 -no-snapshot-save -no-snapstorage";

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
        extensions.add(AndroidEmulatorStartup.class);
        extensions.add(AndroidEmulatorShutdown.class);
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(EMULATOR_AVD_NAME);
        configuration.setConsolePort(EMULATOR_CONSOLE_PORT);
        configuration.setEmulatorBootupTimeoutInSeconds(Long.parseLong(EMULATOR_STARTUP_TIMEOUT));
        configuration.setEmulatorOptions(EMULATOR_OPTIONS);
        configuration.validate();

        androidSDK = new AndroidSDK(configuration);
        processExecutor = new ProcessExecutor();

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ContainerScoped.class, AndroidSDK.class, androidSDK);
        bind(ContainerScoped.class, ProcessExecutor.class, processExecutor);
    }

    @After
    public void disposeMocks() throws AndroidExecutionException {
        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bridge.disconnect();
    }

    @Test
    public void testStartEmulatorOfExistingAVD() throws InterruptedException {
        fire(new AndroidContainerStart());

        assertEventFired(AndroidContainerStart.class, 1);
        assertEventFired(AndroidBridgeInitialized.class, 1);

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull(bridge);
        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        assertEventFired(AndroidVirtualDeviceAvailable.class, 1);
        assertEventFired(AndroidDeviceReady.class, 1);

        List<AndroidDevice> devices = bridge.getDevices();
        Assert.assertFalse(devices.size() == 0);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidDevice.class);
        assertNotNull("Android device is null!", runningDevice);
        bind(ContainerScoped.class, AndroidDevice.class, runningDevice);

        AndroidEmulator emulator = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidEmulator.class);
        assertNotNull("Android emulator is null!", emulator);
        bind(ContainerScoped.class, AndroidEmulator.class, emulator);

        fire(new AndroidContainerStop());

        assertEventFired(AndroidContainerStop.class, 1);
        assertEventFired(AndroidEmulatorShuttedDown.class, 1);

        assertEventFiredInContext(AndroidContainerStart.class, ContainerContext.class);
        assertEventFiredInContext(AndroidBridgeInitialized.class, ContainerContext.class);
        assertEventFiredInContext(AndroidVirtualDeviceAvailable.class, ContainerContext.class);
        assertEventFiredInContext(AndroidDeviceReady.class, ContainerContext.class);
        assertEventFiredInContext(AndroidContainerStop.class, ContainerContext.class);
        assertEventFiredInContext(AndroidEmulatorShuttedDown.class, ContainerContext.class);
    }
}
