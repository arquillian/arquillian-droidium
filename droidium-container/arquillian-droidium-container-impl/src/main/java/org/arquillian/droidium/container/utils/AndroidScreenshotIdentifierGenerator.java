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
package org.arquillian.droidium.container.utils;

import java.util.UUID;

import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.api.IdentifierGeneratorException;
import org.arquillian.droidium.container.api.ScreenshotType;

/**
 * Gets random identifier for screenshot file according to its desired file extension.
 *
 *
 * @see {@link ScreenshotType}
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidScreenshotIdentifierGenerator implements IdentifierGenerator {

    /**
     * @param identifierType Takes classes of {@link ScreenshotType}.
     */
    @Override
    public String getIdentifier(Class<?> identifierType) throws IdentifierGeneratorException {
        String uuid = UUID.randomUUID().toString();

        if (identifierType.isInstance(ScreenshotType.BMP)) {
            return uuid + "." + ScreenshotType.BMP;
        }
        if (identifierType.isInstance(ScreenshotType.GIF)) {
            return uuid + "." + ScreenshotType.GIF;
        }
        if (identifierType.isInstance(ScreenshotType.JPEG)) {
            return uuid + "." + ScreenshotType.GIF;
        }
        if (identifierType.isInstance(ScreenshotType.PNG)) {
            return uuid + "." + ScreenshotType.PNG;
        }
        if (identifierType.isInstance(ScreenshotType.WBMP)) {
            return uuid + "." + ScreenshotType.WBMP;
        }

        throw new IdentifierGeneratorException("Not possible to generate any identifier of type " + identifierType.getName());
    }

}
