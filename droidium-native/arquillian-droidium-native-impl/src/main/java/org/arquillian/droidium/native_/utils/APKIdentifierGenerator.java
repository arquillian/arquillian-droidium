/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.native_.utils;

import java.util.UUID;

import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.api.IdentifierGeneratorException;

/**
 * Generates random identifier for APK file.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class APKIdentifierGenerator implements IdentifierGenerator {

    private static String apkSuffix = ".apk";

    @Override
    public String getIdentifier(Class<?> identifierType) throws IdentifierGeneratorException {
        String uuid = UUID.randomUUID().toString();

        if (identifierType.isInstance(IdentifierType.APK)) {
            return uuid + apkSuffix;
        }
        throw new IdentifierGeneratorException("Not possible to generate any identifier for APK file of type "
            + identifierType.getName());
    }

}
