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

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.arquillian.droidium.container.configuration.Validate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests various file name of SD card againts it's validity.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Parameterized.class)
public class SDCardTestCaseValidNames {

    String sdName;

    public SDCardTestCaseValidNames(String sdName) {
        this.sdName = sdName;
    }

    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
            { "/" }, // slash itself
            { "/tmp/" }, // ends with slash
            { "/tmp/foo/bar" }, // without img
            { "/tmp/foo/bar/withoutdotimg" }, // does not end with .img
            { "/tmp/.img" }, // file name is null
            { "//" }, // double slash
            { "/tmp/foo/bar/" } // does not exist and ends with slash
        };
        return Arrays.asList(data);
    }

    @Test(expected = AndroidContainerConfigurationException.class)
    public void testInvalidFileNames() {
        Validate.sdCardFileName(sdName, "invalidity message");
        fail("Fail! This filename is considered to be valid but is not: " + sdName);
    }
}
