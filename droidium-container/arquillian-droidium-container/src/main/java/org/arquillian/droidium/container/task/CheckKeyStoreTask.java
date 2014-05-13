/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.container.task;

import java.io.File;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.droidium.platform.impl.DroidiumPlatformConfiguration;
import org.arquillian.spacelift.execution.Task;

/**
 * Checks if some keystore from {@link DroidiumPlatformConfiguration} is valid.
 *
 * When this task is to be chained, it returns {@link File} as keystore to create or null if it is already present.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class CheckKeyStoreTask extends Task<Object, File> {

    private AndroidSDK androidSDK;

    public CheckKeyStoreTask sdk(AndroidSDK androidSDK) {
        this.androidSDK = androidSDK;
        return this;
    }

    @Override
    protected File process(Object input) throws Exception {
        if (!Validate.isReadable(new File(androidSDK.getPlatformConfiguration().getKeystore()))) {
            File defaultKeyStore = new File(getDefaultKeyStorePath());
            if (!Validate.isReadable(defaultKeyStore)) {
                return defaultKeyStore;
            }
        }
        return null;
    }

    private String getDefaultKeyStorePath() {
        String separator = System.getProperty("file.separator");
        return androidSDK.getPlatformConfiguration().getAndroidSdkHome() + ".android" + separator + "debug.keystore";
    }

}
