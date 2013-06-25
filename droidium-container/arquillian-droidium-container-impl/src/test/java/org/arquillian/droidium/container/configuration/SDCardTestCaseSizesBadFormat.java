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
package org.arquillian.droidium.container.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.arquillian.droidium.container.configuration.AndroidContainerConfigurationException;
import org.arquillian.droidium.container.configuration.Validate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class which tests values in bad format for SD size of Android SD card.
 *
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Parameterized.class)
public class SDCardTestCaseSizesBadFormat {

    String sdSizeBadFormat;

    public SDCardTestCaseSizesBadFormat(String sdSizeBadFormat) {
        this.sdSizeBadFormat = sdSizeBadFormat;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { "3" },
                { "abc" },
                { "abcM" },
                { "GG" },
                { "1x" }
        };
        return Arrays.asList(data);
    }

    @Test
    public void testBadFormat() {
        try {
            Validate.sdSize(sdSizeBadFormat,
                    "Size of SD card is not in the propper format. Consult mksdcard command for the help!");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Size of SD card is not in the propper format. Consult mksdcard command for the help!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testEmptyOrNullSDSize() {
        try {
            Validate.sdSize(" ", "does not matter");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "Size of the Android SD card to check is null object or empty string";
            assertEquals(expected, ex.getMessage());
        }
        try {
            Validate.sdSize(null, "does not matter");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "Size of the Android SD card to check is null object or empty string";
            assertEquals(expected, ex.getMessage());
        }
    }
}
