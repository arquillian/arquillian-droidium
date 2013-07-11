package org.arquillian.droidium.container.manual;

import org.arquillian.droidium.container.AbstractAndroidTestTestBase;
import org.arquillian.droidium.container.api.AndroidBridge;
import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.LogLevel;
import org.arquillian.droidium.container.impl.AndroidBridgeConnector;
import org.arquillian.droidium.container.impl.AndroidDeviceSelectorImpl;
import org.arquillian.droidium.container.impl.AndroidLogInitializer;
import org.arquillian.droidium.container.impl.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidContainerStart;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.arquillian.droidium.container.utils.AndroidIdentifierGenerator;
import org.arquillian.droidium.container.utils.LogcatHelper;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class AndroidLogInitializerTestCase extends AbstractAndroidTestTestBase {


    private AndroidContainerConfiguration configuration;

    private AndroidSDK androidSDK;

    private String RUNNING_EMULATOR_AVD_NAME = System.getProperty("emulator.running.avd.name", "test01");

    private String RUNNING_EMULATOR_CONSOLE_PORT = System.getProperty("emulator.running.console.port", "5554");

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
        androidSDK = new AndroidSDK(configuration);

        getManager().getContext(ContainerContext.class).activate("doesnotmatter");

        bind(ContainerScoped.class, AndroidContainerConfiguration.class, configuration);
        bind(ContainerScoped.class, AndroidSDK.class, androidSDK);
        bind(ContainerScoped.class, ProcessExecutor.class, new ProcessExecutor());
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

        while(!testWriter.success) {

        }
    }

    private class TestWriter extends Writer {
        public volatile boolean success = false;

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if(success) {
                return;
            }

            String line = String.copyValueOf(cbuf, off, len);
            if(line.matches("./.+?\\([\\s0-9]+?\\):.*")) {
                success = true;
            }

        }

        @Override
        public void flush() throws IOException { }

        @Override
        public void close() throws IOException { }
    }

}
