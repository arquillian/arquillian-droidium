/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.log;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;

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

    private AndroidContainerConfiguration configuration;

    private AndroidDevice androidDevice;

    public LogcatHelper(AndroidContainerConfiguration configuration, AndroidDevice androidDevice) {
        this.configuration = configuration;
        this.androidDevice = androidDevice;
    }

    public Writer prepareWriter() { // TODO implement log4j support
        if (configuration.getLogType().equals(LogType.OUTPUT)) {
            String prefix = configuration.isLogSerialId() ? "LOGCAT (" + androidDevice.getSerialNumber() + "): " : "LOGCAT: ";
            return new LogcatToConsoleWriter(prefix);
        } else if (configuration.getLogType().equals(LogType.LOGGER)) {
            String name = configuration.isLogSerialId() ? "LOGCAT (" + androidDevice.getSerialNumber() + ")" : "LOGCAT";
            return new LogcatToLoggerWriter(Logger.getLogger(name));
        } else if (configuration.getLogType().equals(LogType.FILE)) {
            String logPath = configuration.getLogFilePath();

            if (configuration.isLogSerialId()) {
                String[] logPathParts = logPath.split("\\.");

                if (logPathParts.length > 1) {
                    logPath = "";
                    for (int i = 0; i < logPathParts.length - 1; i++) {
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

    public AndroidContainerConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(AndroidContainerConfiguration configuration) {
        this.configuration = configuration;
    }

    public AndroidDevice getAndroidDevice() {
        return androidDevice;
    }

    public void setAndroidDevice(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }

    private static class LogcatToConsoleWriter extends Writer {

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

    private static class LogcatToLoggerWriter extends Writer {

        private Logger logger;

        public LogcatToLoggerWriter(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            String line = String.copyValueOf(cbuf, off, len);
            if (line.startsWith(LogLevel.ERROR)) {
                logger.severe(line);
            } else if (line.startsWith(LogLevel.WARN)) {
                logger.warning(line);
            } else if (line.startsWith(LogLevel.INFO)) {
                logger.info(line);
            } else if (line.startsWith(LogLevel.DEBUG)) {
                logger.config(line);
            } else if (line.startsWith(LogLevel.VERBOSE)) {
                logger.fine(line);
            } else {
                logger.finer(line);
            }
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void close() throws IOException {
        }
    }

    private static class LogcatToFileWriter extends FileWriter {
        public LogcatToFileWriter(String fileName) throws IOException {
            super(fileName);
        }

        @Override
        public void write(String str) throws IOException {
            super.write(str + "\n");
        }
    }

}
