/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.arquillian.droidium.container.utils;

import java.util.UUID;

import org.arquillian.droidium.container.api.IdentifierGenerator;
import org.arquillian.droidium.container.api.IdentifierGeneratorException;
import org.arquillian.droidium.container.configuration.Validate;

/**
 * Generates random identifier for AVD, SD Card or label of SD Card.
 *
 * Please consult {@link IdentifierType} for possible identifier options.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidIdentifierGenerator implements IdentifierGenerator {

    private String sdCardSuffix = ".img";

    @Override
    public String getIdentifier(Class<?> identifierType) {
        String uuid = UUID.randomUUID().toString();

        if (identifierType.isInstance(IdentifierType.AVD)) {
            return uuid;
        }
        if (identifierType.isInstance(IdentifierType.SD_CARD)) {
            return uuid + sdCardSuffix;
        }
        if (identifierType.isInstance(IdentifierType.SD_CARD_LABEL)) {
            return uuid;
        }
        throw new IdentifierGeneratorException("Not possible to generate any identifier of type " + identifierType.getName());
    }

    /**
     * Sets suffix of SD card file name.
     *
     * @param suffix suffix of SD card file
     * @return this
     * @throws IllegalArgumentException if {@code suffix} is null object or empty string
     */
    public AndroidIdentifierGenerator setSdCardSuffix(String suffix) {
        Validate.notNullOrEmpty(suffix, "suffix to set for SD card can not be a null object nor an empty string.");

        if (!suffix.startsWith(".")) {
            sdCardSuffix = "." + suffix;
        }

        return this;
    }

}
