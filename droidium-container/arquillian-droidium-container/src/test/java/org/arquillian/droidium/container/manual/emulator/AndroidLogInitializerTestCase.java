package org.arquillian.droidium.container.manual.emulator;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.log.AndroidLogInitializer;
import org.arquillian.droidium.container.log.LogLevel;
import org.arquillian.droidium.container.log.LogcatHelper;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.process.ProcessExecutor;
import org.arquillian.spacelift.process.impl.DefaultProcessExecutorFactory;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.test.test.AbstractContainerTestTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("not stable test")
public class AndroidLogInitializerTestCase extends AbstractContainerTestTestBase {

    private AndroidContainerConfiguration configuration;

    private DroidiumPlatformConfiguration platformConfiguration;

    private AndroidSDK androidSDK;

    private ProcessExecutor processExecutor;

    private static final String RUNNING_EMULATOR_AVD_NAME = System.getProperty("emulator.running.avd.name", "test01");

    private static final String RUNNING_EMULATOR_CONSOLE_PORT = System.getProperty("emulator.running.console.port", "5554");

    @Override
    protected void addExtensions(List<Class<?>> extensions) {
        extensions.add(AndroidBridgeConnector.class);
        extensions.add(AndroidDeviceSelectorImpl.class);
        extensions.add(AndroidLogInitializer.class);
    }

    @Before
    public void setup() {
        configuration = new AndroidContainerConfiguration();
        configuration.setAvdName(RUNNING_EMULATOR_AVD_NAME);
        configuration.setConsolePort(RUNNING_EMULATOR_CONSOLE_PORT);
        configuration.setLogLevel(LogLevel.VERBOSE);

        platformConfiguration = new DroidiumPlatformConfiguration();

        processExecutor = new DefaultProcessExecutorFactory().getProcessExecutorInstance();

        androidSDK = new AndroidSDK(platformConfiguration, processExecutor);
        androidSDK.setupWith(configuration);

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ContainerScoped.class, AndroidSDK.class, androidSDK);
        bind(ContainerScoped.class, ProcessExecutor.class, processExecutor);
        bind(ContainerScoped.class, IdentifierGenerator.class, new AndroidIdentifierGenerator());
    }

    @After
    public void tearDown() {
        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        bridge.disconnect();
    }

    @Test(timeout = 30000)
    public void testLogInitialization() {

        TestWriter testWriter = new TestWriter();
        LogcatHelper mockedLogcatHelper = Mockito.mock(LogcatHelper.class);
        Mockito.when(mockedLogcatHelper.prepareWriter()).thenReturn(testWriter);

        bind(ContainerScoped.class, LogcatHelper.class, mockedLogcatHelper);

        fire(new AndroidContainerStart());

        AndroidBridge bridge = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidBridge.class);
        assertNotNull("Android Bridge should be created but is null!", bridge);

        AndroidDevice runningDevice = getManager().getContext(ContainerContext.class).getObjectStore().get(AndroidDevice.class);
        assertNotNull("Android Device should be created but is null!", runningDevice);

        assertEventFired(AndroidDeviceReady.class, 1);

        while (!testWriter.success) {

        }
    }

    private class TestWriter extends Writer {
        public volatile boolean success = false;

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (success) {
                return;
            }

            String line = String.copyValueOf(cbuf, off, len);
            if (line.matches("./.+?\\([\\s0-9]+?\\):.*")) {
                success = true;
            }

        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

}