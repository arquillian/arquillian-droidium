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

package org.arquillian.droidium.container.automatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidDeviceRegister;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.api.FileType;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.deployment.AndroidDeviceContext;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceRegisterImpl;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.impl.AndroidEmulatorShutdown;
import org.arquillian.droidium.container.impl.AndroidEmulatorStartup;
import org.arquillian.droidium.container.impl.AndroidVirtualDeviceManager;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidEmulatorShuttedDown;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceAvailable;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceCreate;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDelete;
import org.arquillian.droidium.container.spi.event.AndroidVirtualDeviceDeleted;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.execution.impl.DefaultExecutionServiceFactory;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.test.AbstractContainerTestBase;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests creating of AVD name from scratch and staring of emulator of newly created AVD.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidEmulatorStartupAVDtoBeCreatedTestCase extends AbstractContainerTestBase {

    private static final String AVD_GENERATED_NAME = "ab1be336-d30f-4d3c-90de-56bdaf198a3e";

    private static final String AVD_ABI = System.getProperty("emulator.startup.abi", "x86");

    private static final String TARGET = System.getProperty("emulator.startup.target", "10");

    private static final String EMULATOR_STARTUP_TIMEOUT = System.getProperty("emulator.startup.timeout", "600");

    private static final String EMULATOR_OPTIONS = "-no-skin -no-audio -no-window -memory 343 -wipe-data -no-snapshot-save -no-snapstorage";

    private AndroidContainerConfiguration configuration;

    private DroidiumPlatformConfiguration platformConfiguration;

    private AndroidSDK androidSDK;

    private AndroidDeviceRegister androidDeviceRegister;

    @Mock
    private IdentifierGenerator<FileType> idGenerator;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
        extensions.add(AndroidEmulatorStartup.class);
        extensions.add(AndroidEmulatorShutdown.class);
        extensions.add(AndroidVirtualDeviceManager.class);
        extensions.add(AndroidDeviceContext.class);
    }

    @BeforeClass
    public static void initializateExecutionService() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAbi(AVD_ABI);
        configuration.setEmulatorBootupTimeoutInSeconds(Integer.parseInt(EMULATOR_STARTUP_TIMEOUT));
        configuration.setEmulatorOptions(EMULATOR_OPTIONS);
        configuration.setTarget(TARGET);
        configuration.setAvdName(AVD_GENERATED_NAME);

        configuration.validate();

        platformConfiguration = new DroidiumPlatformConfiguration();

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        androidDeviceRegister = new AndroidDeviceRegisterImpl();

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        Mockito.when(idGenerator.getIdentifier(FileType.AVD)).thenReturn(AVD_GENERATED_NAME);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);
        bind(ApplicationScoped.class, IdentifierGenerator.class, idGenerator);
        bind(ApplicationScoped.class, AndroidDeviceRegister.class, androidDeviceRegister);
    }

    @After
    public void disposeMocks() throws AndroidExecutionException {
        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bridge.disconnect();
    }

    @Test
    public void testCreateAVDandStartEmulator() {
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class)
            .getObjectStore().get(AndroidDevice.class);
        assertNotNull("Android device is null!", runningDevice);
        bind(ContainerScoped.class, AndroidDevice.class, runningDevice);

        AndroidContainerConfiguration configuration = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidContainerConfiguration.class);
        assertNotNull(configuration);
        assertTrue("AVD was generated by test", configuration.isAVDGenerated());
        assertEquals(AVD_GENERATED_NAME, runningDevice.getAvdName());

        fire(new AndroidContainerStop());

        assertEventFired(AndroidContainerStart.class, 1);
        assertEventFired(AndroidBridgeInitialized.class, 1);
        assertEventFired(AndroidVirtualDeviceCreate.class, 1);
        assertEventFired(AndroidVirtualDeviceAvailable.class, 1);
        assertEventFired(AndroidContainerStop.class, 1);
        assertEventFired(AndroidEmulatorShuttedDown.class, 1);
        assertEventFired(AndroidVirtualDeviceDelete.class, 1);
        assertEventFired(AndroidVirtualDeviceDeleted.class, 1);

        assertEventFiredInContext(AndroidContainerStart.class, ContainerContext.class);
        assertEventFiredInContext(AndroidBridgeInitialized.class, ContainerContext.class);
        assertEventFiredInContext(AndroidVirtualDeviceCreate.class, ContainerContext.class);
        assertEventFiredInContext(AndroidVirtualDeviceAvailable.class, ContainerContext.class);
        assertEventFiredInContext(AndroidContainerStop.class, ContainerContext.class);
        assertEventFiredInContext(AndroidEmulatorShuttedDown.class, ContainerContext.class);
        assertEventFiredInContext(AndroidVirtualDeviceDelete.class, ContainerContext.class);
        assertEventFiredInContext(AndroidVirtualDeviceDeleted.class, ContainerContext.class);
    }
}
