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
package org.arquillian.droidium.container.tool;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.arquillian.droidium.container.configuration.AndroidSDK;
import org.arquillian.spacelift.execution.Tasks;
import org.arquillian.spacelift.process.Command;
import org.arquillian.spacelift.process.CommandBuilder;
import org.arquillian.spacelift.process.impl.CommandTool;
import org.arquillian.spacelift.tool.Tool;

/**
 * Signs APKs.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKSignerTool extends Tool<File, File> {

    protected AndroidSDK androidSDK;

    protected File toSign;

    protected File signed;

    public APKSignerTool sdk(AndroidSDK androidSDK) {
        this.androidSDK = androidSDK;
        return this;
    }

    public APKSignerTool signed(File signed) {
        this.signed = signed;
        return this;
    }

    public APKSignerTool signed(String signed) {
        return signed(new File(signed));
    }

    public APKSignerTool toSign(File toSign) {
        this.toSign = toSign;
        return this;
    }

    public APKSignerTool toSign(String toSign) {
        return toSign(new File(toSign));
    }

    @Override
    protected Collection<String> aliases() {
        return Arrays.asList("apk_signer");
    }

    @Override
    protected File process(File input) throws Exception {

        if (toSign == null && input != null) {
            toSign = input;
        }

        if (toSign == null) {
            throw new IllegalStateException("You have not called toSign() method or you have chained this task and "
                + "its input is null.");
        }

        if (signed == null) {
            throw new IllegalStateException("You have not called signed() method.");
        }

        Command jarSignerCommand = new CommandBuilder(androidSDK.getPathForJavaTool("jarsigner"))
            .parameter("-sigalg").parameter("MD5withRSA")
            .parameter("-digestalg").parameter("SHA1")
            .parameter("-signedjar").parameter(signed.getAbsolutePath())
            .parameter("-storepass").parameter(androidSDK.getPlatformConfiguration().getStorepass())
            .parameter("-keystore").parameter(new File(androidSDK.getPlatformConfiguration().getKeystore()).getAbsolutePath())
            .parameter(toSign.getAbsolutePath())
            .parameter(androidSDK.getPlatformConfiguration().getAlias())
            .build();

        Tasks.prepare(CommandTool.class).command(jarSignerCommand).execute().await();

        return signed;
    }
}
