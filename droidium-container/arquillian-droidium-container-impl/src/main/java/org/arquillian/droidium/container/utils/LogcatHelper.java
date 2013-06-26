package org.arquillian.droidium.container.utils;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidManagedContainerConfiguration;
import org.arquillian.droidium.container.configuration.LogLevel;
import org.arquillian.droidium.container.configuration.LogType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class LogcatHelper {
    private static final Logger logger = Logger.getLogger(LogcatHelper.class.getName());

    private AndroidManagedContainerConfiguration configuration;

    private AndroidDevice androidDevice;

    public LogcatHelper(AndroidManagedContainerConfiguration configuration, AndroidDevice androidDevice) {
        this.configuration = configuration;
        this.androidDevice = androidDevice;
    }

    public Writer prepareWriter() { // TODO implement log4j support
        if(configuration.getLogtype().equals(LogType.OUTPUT)) {
            String prefix = configuration.isLogSerialId() ? "LOGCAT (" + androidDevice.getSerialNumber() + "): " : "LOGCAT: ";
            return new LogcatToConsoleWriter(prefix);
        } else if(configuration.getLogtype().equals(LogType.LOGGER)) {
            String name = configuration.isLogSerialId() ? "LOGCAT (" + androidDevice.getSerialNumber() + ")" : "LOGCAT";
            return new LogcatToLoggerWriter(Logger.getLogger(name));
        } else if(configuration.getLogtype().equals(LogType.FILE)) {
            String logPath = configuration.getLogFilePath();

            if(configuration.isLogSerialId()) {
                String[] logPathParts = logPath.split(".");

                if(logPathParts.length > 1) {
                    logPath = "";
                    for(int i = 0; i < logPathParts.length - 1; i++) {
                        logPath += logPathParts[i];
                    }

                    logPath += androidDevice.getSerialNumber();

                    logPath += logPathParts[logPathParts.length - 1];
                } else {
                    logPath += androidDevice.getSerialNumber();
                }
            }
            try {
                return new LogcatToFileWriter(logPath);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Couldn't open log file!", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public AndroidManagedContainerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AndroidManagedContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    public AndroidDevice getAndroidDevice() {
        return androidDevice;
    }

    public void setAndroidDevice(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    public class LogcatToConsoleWriter extends Writer {

        private String prefix;

        public LogcatToConsoleWriter() {
            this("LOGCAT: ");
        }

        public LogcatToConsoleWriter(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String line = String.copyValueOf(cbuf, off, len);
            System.out.println(prefix + line);
        }

        @Override
        public void flush() throws IOException {
            System.out.flush();
        }

        @Override
        public void close() throws IOException {
            System.out.flush();
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }
    }

    public class LogcatToLoggerWriter extends Writer {

        private Logger logger;

        public LogcatToLoggerWriter(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String line = String.copyValueOf(cbuf, off, len);
            if(line.startsWith(LogLevel.ERROR)) {
                logger.severe(line);
            } else if(line.startsWith(LogLevel.WARN)) {
                logger.warning(line);
            } else if(line.startsWith(LogLevel.INFO)) {
                logger.info(line);
            } else if(line.startsWith(LogLevel.DEBUG)) {
                logger.config(line);
            } else if(line.startsWith(LogLevel.VERBOSE)) {
                logger.fine(line);
            } else {
                logger.finer(line);
            }
        }

        @Override
        public void flush() throws IOException { }

        @Override
        public void close() throws IOException { }
    }

    public class LogcatToFileWriter extends FileWriter {
        public LogcatToFileWriter(String fileName) throws IOException {
            super(fileName);
        }

        @Override
        public void write(String str) throws IOException {
            super.write(str + "\n");
        }
    }

}
