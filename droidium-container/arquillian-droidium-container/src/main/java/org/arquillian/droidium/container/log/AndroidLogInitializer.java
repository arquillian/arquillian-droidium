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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Command;
import org.arquillian.droidium.container.execution.ProcessExecutor;
import org.arquillian.droidium.container.spi.event.AndroidBridgeTerminated;
import org.arquillian.droidium.container.spi.event.AndroidDeviceReady;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;

/**
 *
 *
 * @author <a href="mailto:tkriz@redhat.com">Tadeas Kriz</a>
 */
public class AndroidLogInitializer {
    private static final Logger logger = Logger.getLogger(AndroidLogInitializer.class.getName());

    @Inject
    @ContainerScoped
    private InstanceProducer<LogcatReader> logcat;

    @Inject
    @ContainerScoped
    private InstanceProducer<LogcatHelper> logcatHelper;

    @Inject
    private Instance<AndroidContainerConfiguration> configuration;

    @Inject
    private Instance<ProcessExecutor> executor;

    @Inject
    private Instance<AndroidSDK> androidSDK;

    @Inject
    private Instance<AndroidDevice> androidDevice;

    private Future<Void> logcatFuture;

    public void initAndroidLog(@Observes AndroidDeviceReady event) {
        logger.info("Initializing Android LogcatReader");

        ProcessExecutor executor = this.executor.get();

        if (logcatHelper.get() == null) {
            logcatHelper.set(new LogcatHelper(configuration.get(), androidDevice.get()));
        }

        LogcatReader logcat = new LogcatReader(configuration.get(), androidSDK.get(), androidDevice.get());
        logcat.setWriter(logcatHelper.get().prepareWriter());

        logcatFuture = executor.submit(logcat);

        this.logcat.set(logcat);
    }

    public void terminateAndroidLog(@Observes AndroidBridgeTerminated event) {
        logcatFuture.cancel(true);

        try {
            logcat.get().getWriter().close();
        } catch (IOException e) {
        } catch (NullPointerException e) {
        }
    }

    private static class LogcatReader implements Callable<Void> {
        private AndroidContainerConfiguration configuration;
        private AndroidSDK androidSDK;
        private AndroidDevice androidDevice;

        private Writer writer;

        private Pattern pattern;

        private List<String> whiteList = new ArrayList<String>();
        private List<String> blackList = new ArrayList<String>();
        private Map<Integer, String> processMap = new HashMap<Integer, String>();

        public LogcatReader(AndroidContainerConfiguration configuration, AndroidSDK androidSDK, AndroidDevice androidDevice) {
            this.configuration = configuration;
            this.androidSDK = androidSDK;
            this.androidDevice = androidDevice;

            if (configuration.getLogPackageWhitelist() != null) {
                String[] whiteList = configuration.getLogPackageWhitelist().split(",");
                for (String packageName : whiteList) {
                    this.whiteList.add(escapePackageName(packageName));
                }
            }

            if (configuration.getLogPackageBlacklist() != null) {
                String[] blackList = configuration.getLogPackageBlacklist().split(",");
                for (String packageName : blackList) {
                    this.blackList.add(escapePackageName(packageName));
                }
            }
        }

        @Override
        public Void call() throws Exception {
            if (writer == null) {
                return null;
            }

            try {
                Command command = new Command();
                command
                    .add(androidSDK.getAdbPath())
                    .add("-s")
                    .add(androidDevice.getSerialNumber())
                    .add("logcat")
                    .add("-c");

                ProcessBuilder builder = new ProcessBuilder(command.getAsList());
                Process process = builder.start();

                command.clear();

                command
                    .add(androidSDK.getAdbPath())
                    .add("-s")
                    .add(androidDevice.getSerialNumber())
                    .add("logcat")
                    .add("*:" + configuration.getLogLevel());

                builder = new ProcessBuilder(command.getAsList());
                process = builder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    if (shouldWrite(line)) {
                        writer.write(line);
                        writer.flush();
                    }
                }
                writer.close();
                reader.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error with logcat logging!", e);
            }

            return null;
        }

        private String escapePackageName(String packageName) {
            return packageName
                .replace("\\", "\\\\")
                .replace(".", "\\.")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("?", "\\?")
                .replace("+", "\\+")
                .replace("*", ".*?");
        }

        private boolean shouldWrite(String line) {
            if (!configuration.isLogFilteringEnabled()) {
                return true;
            }

            if (pattern == null) {
                // This pattern will fetch us process id from logcat line
                pattern = Pattern.compile("./.+?\\(([\\s0-9]+?)\\):.*");
            }

            Matcher matcher = pattern.matcher(line);
            if (!matcher.matches()) {
                return false;
            }

            String processIdString = matcher.group(1).trim();
            Integer processId = Integer.valueOf(processIdString);

            if (!processMap.containsKey(processId)) {
                loadProcessMap();
            }

            String processName = processMap.get(processId);
            if (processName == null) {
                processName = "";
            }

            for (String regex : whiteList) {
                if (processName.matches(regex)) {
                    return true;
                }
            }

            for (String regex : blackList) {
                if (processName.matches(regex)) {
                    return false;
                }
            }

            return true;
        }

        private void loadProcessMap() {
            try {
                processMap.clear();

                Command command = new Command();
                command
                    .add(androidSDK.getAdbPath())
                    .add("-s")
                    .add(androidDevice.getSerialNumber())
                    .add("shell")
                    .add("ps");

                ProcessBuilder processBuilder = new ProcessBuilder(command.getAsList());
                Process process = processBuilder.start();

                // Ugly pattern, which helps us parse PS table
                Pattern pattern = Pattern
                    .compile(".*?\\s+([0-9]+)\\s+[0-9]+\\s+[0-9]+\\s+[0-9]+\\s+[0-9a-f]+\\s+[0-9a-f]+\\s.?\\s(.*)");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = pattern.matcher(line);

                    if (!matcher.matches()) {
                        continue;
                    }

                    Integer processId = Integer.valueOf(matcher.group(1));
                    String processName = matcher.group(2);

                    processMap.put(processId, processName);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Couldn't load process map!", e);

            }
        }

        public Writer getWriter() {
            return writer;
        }

        public void setWriter(Writer writer) {
            this.writer = writer;
        }
    }

}
