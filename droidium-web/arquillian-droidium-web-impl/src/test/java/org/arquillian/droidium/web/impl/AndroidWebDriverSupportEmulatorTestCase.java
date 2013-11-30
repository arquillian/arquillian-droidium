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

package org.arquillian.droidium.web.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.AndroidExecutionException;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.impl.AndroidEmulatorShutdown;
import org.arquillian.droidium.container.impl.AndroidEmulatorStartup;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidBridgeInitialized;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.web.AbstractAndroidTestTestBase;
import org.arquillian.droidium.web.configuration.DroidiumWebConfigurator;
import org.arquillian.droidium.web.spi.event.AndroidServerInstalled;
import org.arquillian.droidium.web.spi.event.AndroidServerUninstalled;
import org.arquillian.droidium.web.spi.event.DroidiumWebConfigured;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests installing of Android server APK file for web-related testing.
 *
 * This test expects that AVD is started and no Android server APK is installed on it.
 * You can specify name and console port of already started emulator by system properties
 * placed on the command line while testing with Maven like this:
 *
 * <p>
 * {@code mvn clean package -Pandroid-web -Demulator.name=nameOfAVD -Demulator.port=5554 -Dandroid.server.path=path_to_server}
 * </p>
 *
 * <p>
 * The default value of {@code emulator.avd.name} is "test01".</br>
 * The default value of {@code emulator.avd.port} is "5554".</br>
 * The default value of {@code android.server.path} is "android-server-2.6.0.apk".
 * </p>
 *
 * Please use Android 2.3.3 emulator since APK of version 2.6.0 is going to be installed.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidWebDriverSupportEmulatorTestCase extends AbstractAndroidTestTestBase {

    private AndroidContainerConfiguration configuration;

    private AndroidSDK androidSDK;

    private ProcessExecutor processExecutor;

    private static final String EMULATOR_AVD_NAME = System.getProperty("emulator.avd.name", "test01");

    private static final String EMULATOR_CONSOLE_PORT = System.getProperty("emulator.avd.port", "5554");

    private static final String EMULATOR_OPTIONS = "-no-audio -no-window -memory 343 -no-snapshot-save -no-snapstorage";
    
    private static final String ANDROID_SERVER_APK_PATH = System.getProperty("android.server.path",
        "src/test/resources/android-server-2.6.0.apk");

    private static final String APK_APP_NAME = "org.openqa.selenium.android.app";

    @Mock
    private ServiceLoader serviceLoader;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
        extensions.add(AndroidEmulatorStartup.class);
        extensions.add(AndroidEmulatorShutdown.class);
        extensions.add(DroidiumWebConfigurator.class);
        extensions.add(AndroidServerInstaller.class);
        extensions.add(AndroidServerUninstaller.class);
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(EMULATOR_AVD_NAME);
        configuration.setConsolePort(EMULATOR_CONSOLE_PORT);
        configuration.setEmulatorOptions(EMULATOR_OPTIONS);
        configuration.validate();
        
        androidSDK = new AndroidSDK(configuration);
        processExecutor = new ProcessExecutor();

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        ArquillianDescriptor desc = Descriptors.create(ArquillianDescriptor.class)
            .extension("droidium-web").property("serverApk", ANDROID_SERVER_APK_PATH);

        bind(ApplicationScoped.class, ServiceLoader.class, serviceLoader);
        bind(ApplicationScoped.class, ArquillianDescriptor.class, desc);
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
    public void testInstallUninstallAndroidWebDriverServer() {
        fire(new BeforeSuite());

        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull(bridge);

        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class)
            .getObjectStore().get(AndroidDevice.class);
        assertNotNull("Android device is null!", runningDevice);
        bind(ContainerScoped.class, AndroidDevice.class, runningDevice);

        assertTrue(runningDevice.isPackageInstalled(APK_APP_NAME));

        fire(new AndroidContainerStop());

        assertEventFired(BeforeSuite.class, 1);
        assertEventFired(DroidiumWebConfigured.class, 1);
        assertEventFired(AndroidContainerStart.class, 1);
        assertEventFired(AndroidBridgeInitialized.class, 1);
        assertEventFired(AndroidDeviceReady.class, 1);
        assertEventFired(AndroidServerInstalled.class, 1);
        assertEventFired(AndroidServerUninstalled.class, 1);
        assertEventFired(AndroidContainerStop.class, 1);

        assertEventFiredInContext(BeforeSuite.class, ApplicationContext.class);
        assertEventFiredInContext(DroidiumWebConfigured.class, ContainerContext.class);
        assertEventFiredInContext(AndroidContainerStart.class, ContainerContext.class);
        assertEventFiredInContext(AndroidBridgeInitialized.class, ContainerContext.class);
        assertEventFiredInContext(AndroidDeviceReady.class, ContainerContext.class);
        assertEventFiredInContext(AndroidServerInstalled.class, ContainerContext.class);
        assertEventFiredInContext(AndroidServerUninstalled.class, ContainerContext.class);
        assertEventFiredInContext(AndroidContainerStop.class, ContainerContext.class);
    }
}
