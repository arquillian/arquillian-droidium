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

import org.arquillian.droidium.container.utils.DroidiumFileUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * Resigns APK.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKResignerTool extends APKSignerTool {

    @Override
    protected Collection<String> aliases() {
        return Arrays.asList("apk_resigner");
    }

    @Override
    protected File process(File toResign) throws Exception {

        Archive<?> apk = ShrinkWrap.createFromZipFile(JavaArchive.class, toResign);
        apk.delete("META-INF");

        File toSign = new File(androidSDK.getPlatformConfiguration().getTmpDir(), DroidiumFileUtils.getRandomAPKFileName());
        DroidiumFileUtils.export(apk, toSign);

        return super.process(toSign);
    }
}
