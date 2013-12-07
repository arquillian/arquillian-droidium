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
 * Test class which tests values which are of bad size of SD card.
 *
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Parameterized.class)
public class SDCardTestCaseSizesBad {

    String sdSizeBad;

    public SDCardTestCaseSizesBad(String sdSizeBad) {
        this.sdSizeBad = sdSizeBad;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
            { "1024G" },
            { "1048576M" },
            { "1073741824K" },
            { "8M" },
            { "9125K" }
        };
        return Arrays.asList(data);
    }

    @Test
    public void testSdSizeBadSize() {
        try {
            Validate.sdSize(sdSizeBad, "does not matter");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Maximum size is 1099511627264 bytes, 1073741823K, 1048575M or 1023G. Minimum size is 9M. " +
                "The Android emulator cannot use smaller images.";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSdSizeWithoutUnit() {
        try {
            Validate.sdSize("1099511627265", "does not matter");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Minimum size is 9M. Maximum size is 1023G. The Android emulator cannot use smaller or bigger images.";
            assertEquals(expected, ex.getMessage());
        }
        try {
            Validate.sdSize("9437183", "does not matter");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Minimum size is 9M. Maximum size is 1023G. The Android emulator cannot use smaller or bigger images.";
            assertEquals(expected, ex.getMessage());
        }
    }
}
