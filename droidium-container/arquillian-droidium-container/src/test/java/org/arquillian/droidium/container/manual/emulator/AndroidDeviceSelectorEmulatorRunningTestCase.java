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

import java.io.File;
import java.io.IOException;
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
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Tests connecting to the running emulator of specific avd and console port.
 *
 * You set the name of running AVD by specifying of</br>
 * <p>
 * {@code -Demulator.running.avd.name=avd_name}
 * </p>
 *
 * <p>
 * {@code -Demulator.running.console.port=port_number}
 * </p>
 *
 * at the Maven command line in connection with -Pmanual-test profile. Default AVD name is "test01", default port number is
 * 5554.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidDeviceSelectorEmulatorRunningTestCase extends AbstractContainerTestBase {

    private AndroidContainerConfiguration configuration;

    private DroidiumPlatformConfiguration platformConfiguration;

    private AndroidSDK androidSDK;

    private static final String RUNNING_EMULATOR_AVD_NAME = System.getProperty("emulator.running.avd.name", "test01");

    private static final String RUNNING_EMULATOR_CONSOLE_PORT = System.getProperty("emulator.running.console.port", "5554");

    private File pushFile1;

    private File pushFile2;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
    }

    @BeforeClass
    public static void initializateExecutionService() {
        Tasks.setDefaultExecutionServiceFactory(new DefaultExecutionServiceFactory());
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(RUNNING_EMULATOR_AVD_NAME);
        configuration.setConsolePort(RUNNING_EMULATOR_CONSOLE_PORT);
        configuration.setGenerateSDCard(true);
        configuration.validate();

        platformConfiguration = new DroidiumPlatformConfiguration();
        platformConfiguration.validate();

        androidSDK = new AndroidSDK(platformConfiguration);
        androidSDK.setupWith(configuration);

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ApplicationScoped.class, DroidiumPlatformConfiguration.class, platformConfiguration);
        bind(ApplicationScoped.class, AndroidSDK.class, androidSDK);

        pushFile1 = getTemporaryFile();
        pushFile2 = getTemporaryFile();
    }

    @After
    public void disposeMocks() throws AndroidExecutionException {
        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bridge.disconnect();

        if (!pushFile1.delete()) {
            throw new RuntimeException("Unable to delete temporary file " + pushFile1.getAbsolutePath());
        }

        if (!pushFile2.delete()) {
            throw new RuntimeException("Unable to delete temporary file " + pushFile2.getAbsolutePath());
        }
    }

    @Test
    public void testGetRunningEmulator() {
        getAndroidDevice();
        executeAsserts();
    }

    @Test
    public void testFileManipulation() throws Exception {
        AndroidDevice device = getAndroidDevice();

        File remoteFile1 = new File("/sdcard/", pushFile1.getName());
        File remoteFile2 = new File("/sdcard/", pushFile2.getName());

        device.push(pushFile1, remoteFile1);
        device.push(pushFile2.getAbsolutePath(), "/sdcard/" + pushFile2.getName());

        pushFile1.delete();
        pushFile2.delete();

        assertThat(pushFile1.exists(), is(false));
        assertThat(pushFile2.exists(), is(false));

        device.pull(remoteFile1, pushFile1);
        device.pull(remoteFile2, pushFile2);

        assertThat(pushFile1.exists(), is(true));
        assertThat(pushFile2.exists(), is(true));

        device.remove(remoteFile1);
        device.remove(remoteFile2);

        executeAsserts();
    }

    // helpers

    private AndroidDevice getAndroidDevice() {
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull("AndroidBridge is null object!", bridge);
        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidDevice.class);
        assertNotNull("Android device is a null object!", runningDevice);

        return runningDevice;
    }

    private void executeAsserts() {
        assertEventFired(AndroidContainerStart.class, 1);
        assertEventFired(AndroidBridgeInitialized.class, 1);
        assertEventFired(AndroidDeviceReady.class, 1);
        assertEventFiredInContext(AndroidContainerStart.class, ContainerContext.class);
        assertEventFiredInContext(AndroidBridgeInitialized.class, ContainerContext.class);
        assertEventFiredInContext(AndroidDeviceReady.class, ContainerContext.class);
    }

    private File getTemporaryFile() {
        File tempFile;

        try {
            tempFile = File.createTempFile("droidium_push_file-", null, platformConfiguration.getTmpDir());
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create temporary file for Droidium file manipulation test.");
        }

        return tempFile;
    }
}
