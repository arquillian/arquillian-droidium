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

import java.util.Arrays;
import java.util.Collection;

import org.arquillian.droidium.container.configuration.Validate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class which tests all border values which are of valid size of SD card.
 *
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Parameterized.class)
public class SDCardTestCaseSizesOK {

    private String sdSizeOk;

    public SDCardTestCaseSizesOK(String sdSize) {
        this.sdSizeOk = sdSize;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { "9126K" },
                { "9M" },
                { "9437184" },
                { "1099511627264" },
                { "1073741823K" },
                { "1048575M" },
                { "1023G" }
        };
        return Arrays.asList(data);
    }

    @Test
    public void testSdSizeOkSize() {
        Validate.sdSize(sdSizeOk, "Not valid size of sd card!");
    }
}
