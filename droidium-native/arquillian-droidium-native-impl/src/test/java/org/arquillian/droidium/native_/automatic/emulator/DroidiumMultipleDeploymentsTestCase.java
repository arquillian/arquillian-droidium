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
package org.arquillian.droidium.native_.automatic.emulator;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.arquillian.droidium.container.AndroidDeployableContainer;
import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.impl.AndroidEmulatorShutdown;
import org.arquillian.droidium.container.impl.AndroidEmulatorStartup;
import org.arquillian.droidium.container.impl.AndroidVirtualDeviceManager;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidContainerStop;
import org.arquillian.droidium.container.spi.event.AndroidDeployArchive;
import org.arquillian.droidium.container.spi.event.AndroidUndeployArchive;
import org.arquillian.droidium.multiplecontainers.MultipleLocalContainersRegistry;
import org.arquillian.droidium.native_.AbstractAndroidTestTestBase;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfiguration;
import org.arquillian.droidium.native_.configuration.DroidiumNativeConfigurator;
import org.arquillian.droidium.native_.deployment.impl.DeploymentController;
import org.arquillian.droidium.native_.instrumentation.impl.InstrumentationDecider;
import org.arquillian.droidium.native_.instrumentation.impl.InstrumentationResolver;
import org.arquillian.droidium.native_.instrumentation.impl.InstrumentationMapperException;
import org.arquillian.droidium.native_.spi.event.InstrumentationPerformed;
import org.arquillian.droidium.native_.spi.event.InstrumentationRemoved;
import org.arquillian.droidium.native_.spi.event.PerformInstrumentation;
import org.arquillian.droidium.native_.spi.event.RemoveInstrumentation;
import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.context.TestContext;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeSuite;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Multiple deployments & multiple instrumentations tests.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class DroidiumMultipleDeploymentsTestCase extends AbstractAndroidTestTestBase {

    private static final Logger logger = Logger.getLogger(DroidiumMultipleDeploymentsTestCase.class.getName());

    private AndroidContainerConfiguration configuration;

    private DroidiumNativeConfiguration droidiumConfiguration;

    private AndroidSDK androidSDK;

    private ProcessExecutor processExecutor;

    @Inject
    private Instance<Injector> injector;

    @Mock
    private ServiceLoader serviceLoader;

    private static final String EMULATOR_AVD_NAME = System.getProperty("emulator.running.avd.name", "test01");

    private static final String EMULATOR_CONSOLE_PORT = System.getProperty("emulator.running.console.port", "5554");

    private static final String EMULATOR_STARTUP_TIMEOUT = System.getProperty("emulator.startup.timeout", "600");

    private static final String EMULATOR_OPTIONS = "-no-audio -no-window -memory 256 -nocache -no-snapshot-save -no-snapstorage";

    private static final String SELENDROID_SERVER_APK = "src/test/resources/selendroid-server-0.5.0.apk";

    private static final String SELENDROID_TEST_APP_APK = "src/test/resources/selendroid-test-app-0.5.0.apk";

    // just to have something to deploy, it does not matter at all what apk we are deploying here
    private static final String AEROGEAR_APP = "src/test/resources/aerogear-test-android.apk";

    private DeployableContainer<AndroidContainerConfiguration> deployableContainer = new AndroidDeployableContainer();

    private ContainerRegistry registry;
    
    @SuppressWarnings("rawtypes")
    private Collection<DeployableContainer> containers;

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
        extensions.add(AndroidEmulatorStartup.class);
        extensions.add(AndroidEmulatorShutdown.class);
        extensions.add(AndroidVirtualDeviceManager.class);
        extensions.add(DroidiumNativeConfigurator.class);
        extensions.add(DeploymentController.class);
        extensions.add(InstrumentationResolver.class);
        extensions.add(InstrumentationDecider.class);
    }

    @SuppressWarnings("rawtypes")
    @org.junit.Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(EMULATOR_AVD_NAME);
        configuration.setConsolePort(EMULATOR_CONSOLE_PORT);
        configuration.setEmulatorBootupTimeoutInSeconds(Long.parseLong(EMULATOR_STARTUP_TIMEOUT));
        configuration.setEmulatorOptions(EMULATOR_OPTIONS);
        configuration.setDroneGuestPort(8080);
        configuration.setDroneHostPort(8080);
        // configuration.setAbi(ANDROID_EMULATOR_ABI);
        androidSDK = new AndroidSDK(configuration);
        processExecutor = new ProcessExecutor();

        droidiumConfiguration = new DroidiumNativeConfiguration();
        droidiumConfiguration.setProperty("serverApk", SELENDROID_SERVER_APK);

        containers = new ArrayList<DeployableContainer>();
        containers.add(deployableContainer);

        Mockito.when(serviceLoader.all(DeployableContainer.class)).thenReturn(containers);

        registry = new MultipleLocalContainersRegistry(injector.get());
        registry.create(Descriptors.create(ArquillianDescriptor.class)
            .group("containers")
            .container("android1").setDefault()
            .property("avdName", EMULATOR_AVD_NAME)
            .property("droneHostPort", "8080")
            .property("droneGuestPort", "8080"),
            // .property("abi", ANDROID_EMULATOR_ABI),
            serviceLoader);

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ContainerScoped.class, DroidiumNativeConfiguration.class, droidiumConfiguration);
        bind(ContainerScoped.class, AndroidSDK.class, androidSDK);
        bind(ContainerScoped.class, ProcessExecutor.class, processExecutor);
    }

    @org.junit.BeforeClass
    public static void portsAreFreeToBindTo() {
        List<Integer> ports = new ArrayList<Integer>();
        
        ports.add(8080);
        ports.add(8081);
        ports.add(8082);
        ports.add(8083);
        
        for (Integer port : ports) {
            if (!isPortFree(port)) {
                throw new IllegalStateException("Be sure port " + port + " is free to bind to prior to the test execution!");
            }
        }
    }

    @org.junit.After
    public void disposeMocks() {
        // AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        // bridge.disconnect();

        TestClassActivator.deactivate(getManager(), DummyTestClass.class);
        TestClassActivator.deactivate(getManager(), DummyTestClassWithoutDeploymentMethod.class);
        TestClassActivator.deactivate(getManager(), DummyInstrumentableTestClass.class);
        TestClassActivator.deactivate(getManager(), DummyMultipleDeploymentsInstrumentableTestClass.class);
        TestClassActivator.deactivate(getManager(), DummyInstrumentableTestClassWithPort.class);
        TestClassActivator.deactivate(getManager(), DummyMultipleInstrumentableDeploymentsTestClass.class);
        TestClassActivator.deactivate(getManager(), DummyMultipleInstrumentableDeploymentsTestClassWithWrongPorts.class);

        getManager().getContext(ClassContext.class).destroy(DummyTestClass.class);
        getManager().getContext(ClassContext.class).destroy(DummyTestClassWithoutDeploymentMethod.class);
        getManager().getContext(ClassContext.class).destroy(DummyInstrumentableTestClass.class);
        getManager().getContext(ClassContext.class).destroy(DummyMultipleDeploymentsInstrumentableTestClass.class);
        getManager().getContext(ClassContext.class).destroy(DummyInstrumentableTestClassWithPort.class);
        getManager().getContext(ClassContext.class).destroy(DummyMultipleInstrumentableDeploymentsTestClass.class);
        getManager().getContext(ClassContext.class)
            .destroy(DummyMultipleInstrumentableDeploymentsTestClassWithWrongPorts.class);
    }

    @Test
    public void testScenarioWithoutDeploymentMethod() {

        logger.info("TEST CASE : testScenarioWithoutDeploymentMethod");

        TestClassActivator.activate(getManager(), DummyTestClassWithoutDeploymentMethod.class);

        executeZeroDeploymentTest(DummyTestClassWithoutDeploymentMethod.class);

        assertEventFired(PerformInstrumentation.class, 0);
        assertEventFired(InstrumentationPerformed.class, 0);
        assertEventFired(RemoveInstrumentation.class, 0);
        assertEventFired(InstrumentationRemoved.class, 0);

        TestClassActivator.deactivate(getManager(), DummyTestClassWithoutDeploymentMethod.class);
    }

    @Test
    public void testMultipleDeploymentsScenarioWithDummyTestClass() {

        logger.info("TEST CASE : testMultipleDeploymentsScenarioWithDummyTestClass");

        TestClassActivator.activate(getManager(), DummyTestClass.class);

        executeOneDeploymentTest(DummyTestClass.class);

        assertEventFired(PerformInstrumentation.class, 1);
        assertEventFired(InstrumentationPerformed.class, 1);
        assertEventFired(RemoveInstrumentation.class, 1);
        assertEventFired(InstrumentationRemoved.class, 1);

        TestClassActivator.deactivate(getManager(), DummyTestClass.class);
    }

    @Test
    public void testMultipleDeploymentsScenarioWithDummyInstrumentableTestClass() {

        logger.info("TEST CASE : testMultipleDeploymentsScenarioWithDummyInstrumentableTestClass");

        TestClassActivator.activate(getManager(), DummyInstrumentableTestClass.class);

        executeOneDeploymentTest(DummyInstrumentableTestClass.class);

        assertEventFired(PerformInstrumentation.class, 1);
        assertEventFired(InstrumentationPerformed.class, 1);
        assertEventFired(RemoveInstrumentation.class, 1);
        assertEventFired(InstrumentationRemoved.class, 1);

        TestClassActivator.deactivate(getManager(), DummyInstrumentableTestClass.class);
    }

    @Test
    public void testMultipleDeploymentScenarioWithDummyMultipleDeploymentsInstrumentableTestClass() {

        logger.info("TEST CASE : testMultipleDeploymentScenarioWithDummyMultipleDeploymentsInstrumentableTestClass");

        TestClassActivator.activate(getManager(), DummyMultipleDeploymentsInstrumentableTestClass.class);

        executeTwoDeploymentsTest(DummyMultipleDeploymentsInstrumentableTestClass.class);

        assertEventFired(PerformInstrumentation.class, 1);
        assertEventFired(InstrumentationPerformed.class, 1);
        assertEventFired(RemoveInstrumentation.class, 1);
        assertEventFired(InstrumentationRemoved.class, 1);

        TestClassActivator.deactivate(getManager(), DummyMultipleDeploymentsInstrumentableTestClass.class);
    }

    @Test
    public void testMultipleDeploymentsScenarioWithDummyInstrumentableTestClassWithPort() {

        logger.info("TEST CASE : testMultipleDeploymentsScenarioWithDummyInstrumentableTestClassWithPort");

        TestClassActivator.activate(getManager(), DummyInstrumentableTestClassWithPort.class);

        executeOneDeploymentTest(DummyInstrumentableTestClassWithPort.class);

        assertEventFired(PerformInstrumentation.class, 1);
        assertEventFired(InstrumentationPerformed.class, 1);
        assertEventFired(RemoveInstrumentation.class, 1);
        assertEventFired(InstrumentationRemoved.class, 1);

        TestClassActivator.deactivate(getManager(), DummyInstrumentableTestClassWithPort.class);
    }

    @Test
    public void testDumyMultipleInstrumentableDeploymentsTestClassWithDifferentPorts() {

        logger.info("TEST CASE : testDumyMultipleInstrumentableDeploymentsTestClassWithDifferentPorts");

        TestClassActivator.activate(getManager(), DummyMultipleInstrumentableDeploymentsTestClass.class);

        executeTwoDeploymentsTest(DummyMultipleInstrumentableDeploymentsTestClass.class);

        assertEventFired(PerformInstrumentation.class, 2);
        assertEventFired(InstrumentationPerformed.class, 2);
        assertEventFired(RemoveInstrumentation.class, 2);
        assertEventFired(InstrumentationRemoved.class, 2);

        TestClassActivator.deactivate(getManager(), DummyMultipleInstrumentableDeploymentsTestClass.class);
    }

//    @Test(expected = InstrumentationMapperException.class)
//    public void testDumyMultipleInstrumentableDeploymentsTestClassWithWrongPorts() {
//
//        logger.info("TEST CASE : testDumyMultipleInstrumentableDeploymentsTestClassWithWrongPorts");
//
//        TestClassActivator.activate(getManager(), DummyMultipleInstrumentableDeploymentsTestClassWithWrongPorts.class);
//        executeTwoDeploymentsTest(DummyMultipleInstrumentableDeploymentsTestClassWithWrongPorts.class);
//    }
//
//    @Test(expected = InstrumentationMapperException.class)
//    public void testMultipleDeploymentsScenarioWithoutInstrumentableTestClass() {
//
//        logger.info("TEST CASE : testMultipleDeploymentsScenarioWithoutInstrumentableTestClass");
//
//        TestClassActivator.activate(getManager(), DummyMutlipleDeploymentWithoutInstrumentableTestClass.class);
//
//        executeTwoDeploymentsTest(DummyMutlipleDeploymentWithoutInstrumentableTestClass.class);
//    }

    private static Archive<?> getSelendroidDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
    }

    private static Archive<?> getAerogearDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(AEROGEAR_APP));
    }

    // TEST CLASSES TO TEST MULTIPLE DEPLOYMENT SCENARIOS OF

    private static final class DummyTestClassWithoutDeploymentMethod {

        @Test
        public void dummyTest() {
        }
    }

    private static final class DummyTestClass {

        @org.jboss.arquillian.container.test.api.Deployment
        public static Archive<?> getDeployment() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Test
        public void dummyTest() {
        }
    }

    private static final class DummyInstrumentableTestClass {

        @org.jboss.arquillian.container.test.api.Deployment
        @Instrumentable
        public static Archive<?> getInstrumentableDeployment() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Test
        public void dummyTest() {
        }
    }

    // we do not know which deployment will be instrumented, exception should be thrown
    private static final class DummyMutlipleDeploymentWithoutInstrumentableTestClass {

        @org.jboss.arquillian.container.test.api.Deployment(name = "selendroid")
        public static Archive<?> getDeployment1() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @org.jboss.arquillian.container.test.api.Deployment(name = "aerogear")
        public static Archive<?> getDeployment2() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(AEROGEAR_APP));
        }

        @Test
        public void dummyTest() {
        }
    }

    private static final class DummyMultipleDeploymentsInstrumentableTestClass {

        @Deployment(name = "selendroid")
        @Instrumentable
        public static Archive<?> getInstrumentableDeployment() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Deployment(name = "aerogear")
        public static Archive<?> getDeployment() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(AEROGEAR_APP));
        }

        @Test
        public void dummyTest() {
        }
    }

    private static final class DummyInstrumentableTestClassWithPort {

        @org.jboss.arquillian.container.test.api.Deployment
        @Instrumentable(viaPort = 8082)
        public static Archive<?> getInstrumentableDeployment() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Test
        public void dummyTest() {
        }
    }

    private static final class DummyMultipleInstrumentableDeploymentsTestClass {

        @Deployment(name = "selendroid")
        @Instrumentable(viaPort = 8081)
        public static Archive<?> getInstrumentableDeployment1() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Deployment(name = "aerogear")
        @Instrumentable(viaPort = 8082)
        public static Archive<?> getInstrumentableDeployment2() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(AEROGEAR_APP));
        }

        @Test
        public void dummyTest() {
        }
    }

    // same port in both methods, exception should be thrown
    private static final class DummyMultipleInstrumentableDeploymentsTestClassWithWrongPorts {

        @Deployment(name = "selendroid")
        @Instrumentable(viaPort = 8081)
        public static Archive<?> getInstrumentableDeployment1() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(SELENDROID_TEST_APP_APK));
        }

        @Deployment(name = "aerogear")
        @Instrumentable(viaPort = 8081)
        public static Archive<?> getInstrumentableDeployment2() {
            return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(AEROGEAR_APP));
        }

        @Test
        public void dummyTest() {
        }
    }

    // END OF TEST CLASSES TO TEST MULTIPLE DEPLOYMENT SCENARIOS OF

    private void executeZeroDeploymentTest(Class<?> testClass) {
        fire(new BeforeSuite());
        fire(new SetupContainer(registry.getContainer("android1")));
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull(bridge);

        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        DeployableContainer<?> container = (DeployableContainer<?>) (containers.toArray())[0];

        fire(new AfterStart(container));
        fire(new BeforeClass(testClass));
        fire(new Before(TestClassActivator.getInstance(), TestClassActivator.getMethod()));

        fire(new After(TestClassActivator.getInstance(), TestClassActivator.getMethod()));
        fire(new AfterClass(testClass));
        fire(new AndroidContainerStop());
        fire(new AfterStop(container));
        fire(new AfterSuite());
    }

    private void executeOneDeploymentTest(Class<?> testClass) {
        fire(new BeforeSuite());
        fire(new SetupContainer(registry.getContainer("android1")));
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull(bridge);

        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        org.jboss.arquillian.container.spi.client.deployment.Deployment deployment =
            new org.jboss.arquillian.container.spi.client.deployment.Deployment(new DeploymentDescription("_DEFAULT_",
                getSelendroidDeployment()));

        DeployableContainer<?> container = (DeployableContainer<?>) (containers.toArray())[0];

        fire(new AfterStart(container));
        fire(new BeforeClass(testClass));
        fire(new GenerateDeployment(new TestClass(testClass)));
        fire(new DeployDeployment(registry.getContainer("android1"), deployment));
        fire(new BeforeDeploy(container, deployment.getDescription()));
        fire(new AndroidDeployArchive(deployment.getDescription().getArchive()));
        fire(new AfterDeploy(container, deployment.getDescription()));
        fire(new Before(TestClassActivator.getInstance(), TestClassActivator.getMethod()));

        fire(new After(TestClassActivator.getInstance(), TestClassActivator.getMethod()));
        fire(new AfterClass(testClass));
        fire(new AndroidUndeployArchive(deployment.getDescription().getArchive()));
        fire(new AfterUnDeploy(container, deployment.getDescription()));

        fire(new AndroidContainerStop());
        fire(new AfterStop(container));
        fire(new AfterSuite());
    }

    private void executeTwoDeploymentsTest(Class<?> testClass) {
        fire(new BeforeSuite());
        fire(new SetupContainer(registry.getContainer("android1")));
        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull(bridge);

        bind(ContainerScoped.class, AndroidBridge.class, bridge);

        org.jboss.arquillian.container.spi.client.deployment.Deployment deployment1 =
            new org.jboss.arquillian.container.spi.client.deployment.Deployment(new DeploymentDescription("selendroid",
                getSelendroidDeployment()));

        org.jboss.arquillian.container.spi.client.deployment.Deployment deployment2 =
            new org.jboss.arquillian.container.spi.client.deployment.Deployment(new DeploymentDescription("aerogear",
                getAerogearDeployment()));

        DeployableContainer<?> container = (DeployableContainer<?>) (containers.toArray())[0];

        fire(new AfterStart(container));
        fire(new BeforeClass(testClass));
        fire(new GenerateDeployment(new TestClass(testClass)));

        // deploy selendroid app
        fire(new DeployDeployment(registry.getContainer("android1"), deployment1));
        fire(new BeforeDeploy(container, deployment1.getDescription()));
        fire(new AndroidDeployArchive(deployment1.getDescription().getArchive()));
        fire(new AfterDeploy(container, deployment1.getDescription()));

        // deploy aerogear app
        fire(new DeployDeployment(registry.getContainer("android1"), deployment2));
        fire(new BeforeDeploy(container, deployment2.getDescription()));
        fire(new AndroidDeployArchive(deployment2.getDescription().getArchive()));
        fire(new AfterDeploy(container, deployment2.getDescription()));

        fire(new Before(TestClassActivator.getInstance(), TestClassActivator.getMethod()));

        fire(new After(TestClassActivator.getInstance(), TestClassActivator.getMethod()));
        fire(new AfterClass(testClass));

        // undeploy selendroid app
        fire(new AndroidUndeployArchive(deployment1.getDescription().getArchive()));
        fire(new AfterUnDeploy(container, deployment1.getDescription()));

        // undeploy aerogear app
        fire(new AndroidUndeployArchive(deployment2.getDescription().getArchive()));
        fire(new AfterUnDeploy(container, deployment2.getDescription()));

        fire(new AndroidContainerStop());
        fire(new AfterStop(container));
        fire(new AfterSuite());
    }

    private static final class TestClassActivator {

        private static Object instance;

        private static Method method;

        public static void activate(Manager manager, Class<?> testClass) {
            manager.getContext(ClassContext.class).activate(testClass);
            TestClassActivator.instance = SecurityActions.newInstance(testClass, new Class<?>[] {}, new Object[] {});
            try {
                TestClassActivator.method = testClass.getMethod("dummyTest");
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            }
            manager.getContext(TestContext.class).activate(TestClassActivator.instance);
        }

        public static void deactivate(Manager manager, Class<?> testClass) {
            manager.getContext(ClassContext.class).destroy(testClass);
            manager.getContext(TestContext.class).destroy(instance);
        }

        public static Object getInstance() {
            return instance;
        }

        public static Method getMethod() {
            return method;
        }
    }
    
    
    private static boolean isPortFree(Integer port) {
        Socket socket = new Socket();
        try {
            socket.bind(new InetSocketAddress(port));
            socket.close();
        } catch (IOException ex) {
            return false;
        }
        return true;
    }

}
