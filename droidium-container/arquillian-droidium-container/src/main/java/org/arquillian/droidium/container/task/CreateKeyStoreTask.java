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
import org.arquillian.spacelift.execution.Task;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;

/**
 * Creates keystore by keytool command.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class CreateKeyStoreTask extends Task<File, File> {

    private AndroidSDK androidSDK;

    private File keyStoreToCreate;

    public CreateKeyStoreTask sdk(AndroidSDK androidSDK) {
        this.androidSDK = androidSDK;
        return this;
    }

    public CreateKeyStoreTask keyStoreToCreate(File keyStoreToCreate) {
        this.keyStoreToCreate = keyStoreToCreate;
        return this;
    }

    public CreateKeyStoreTask keyStoreToCreate(String keyStoreToCreate) {
        return keyStoreToCreate(new File(keyStoreToCreate));
    }

    @Override
    protected File process(File input) throws Exception {

        if (input == null && keyStoreToCreate != null) {
            input = keyStoreToCreate;
        }

        Command createKeyStoreCommand = new CommandBuilder(androidSDK.getPathForJavaTool("keytool"))
            .parameter("-genkey")
            .parameter("-v")
            .parameter("-keystore")
            .parameter(input.getAbsolutePath())
            .parameter("-storepass")
            .parameter(androidSDK.getPlatformConfiguration().getStorepass())
            .parameter("-alias")
            .parameter(androidSDK.getPlatformConfiguration().getAlias())
            .parameter("-keypass")
            .parameter(androidSDK.getPlatformConfiguration().getKeypass())
            .parameter("-dname")
            .parameter("CN=Android,O=Android,C=US")
            .parameter("-storetype")
            .parameter("JKS")
            .parameter("-sigalg")
            .parameter(androidSDK.getPlatformConfiguration().getSigalg())
            .parameter("-keyalg")
            .parameter(androidSDK.getPlatformConfiguration().getKeyalg())
            .build();

        Tasks.prepare(CommandTool.class).command(createKeyStoreCommand).execute().await();

        return keyStoreToCreate;
    }

}
