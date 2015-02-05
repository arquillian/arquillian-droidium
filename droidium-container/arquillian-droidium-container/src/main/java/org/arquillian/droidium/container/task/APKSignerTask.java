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
import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.arquillian.spacelift.Spacelift;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.task.Task;
import org.arquillian.spacelift.task.os.CommandTool;

/**
 * Signs APKs.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKSignerTask extends Task<File, File> {

    protected AndroidSDK androidSDK;

    public APKSignerTask sdk(AndroidSDK androidSDK) {
        this.androidSDK = androidSDK;
        return this;
    }

    @Override
    protected File process(File toSign) throws Exception {

        if (toSign == null || !toSign.exists()) {
            throw new IllegalStateException("File to be signed is either null or it does not exists");
        }

        Spacelift.task(CheckKeyStoreTask.class).sdk(androidSDK).execute().await();

        File signed = new File(androidSDK.getPlatformConfiguration().getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());

        Command jarSignerCommand = new CommandBuilder(androidSDK.getPathForJavaTool("jarsigner"))
            .parameter("-sigalg").parameter("MD5withRSA")
            .parameter("-digestalg").parameter("SHA1")
            .parameter("-signedjar").parameter(signed.getAbsolutePath())
            .parameter("-storepass").parameter(androidSDK.getPlatformConfiguration().getStorepass())
            .parameter("-keystore").parameter(new File(androidSDK.getPlatformConfiguration().getKeystore()).getAbsolutePath())
            .parameter(toSign.getAbsolutePath())
            .parameter(androidSDK.getPlatformConfiguration().getAlias())
            .build();

        Spacelift.task(CommandTool.class)
            .addEnvironment(androidSDK.getPlatformConfiguration().getAndroidSystemEnvironmentProperties())
            .command(jarSignerCommand).execute().await();

        return signed;
    }
}
