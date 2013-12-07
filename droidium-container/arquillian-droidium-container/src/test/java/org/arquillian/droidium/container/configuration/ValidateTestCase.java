/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.arquillian.droidium.container.configuration.AndroidContainerConfigurationException;
import org.arquillian.droidium.container.configuration.Validate;
import org.junit.Test;

/**
 * Validates basic helper validation methods.
 *
 * @author <a href="@mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class ValidateTestCase {

    @Test
    public void testStateNotNullMessageNull() throws Exception {
        try {
            Validate.notNull(null, null);
            fail();
        } catch (IllegalArgumentException ex) {
            String expected = "Exception message is a null object!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testStateNonNullObjectNull() throws Exception {
        Validate.notNull(null, "test");
    }

    @Test(expected = IllegalStateException.class)
    public void testNotNullsMessageNull() throws Exception {
        Validate.notNulls(null, null);
    }

    @Test
    public void testNotNullsObjectsNull() throws Exception {
        try {
            Validate.notNulls(null, "Array to check the nullity of objects is null object.");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "Array to check the nullity of objects is a null object.";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testNotNulls() throws Exception {
        try {
            Validate.notNulls(new Object[] { new Object(), null, new Object() }, "Some object you passed is null!");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "Some object you passed is null!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNotNullOrEmptyMessageNull() {
        Validate.notNullOrEmpty(null, null);
    }

    @Test
    public void testNotNullOrEmptyStringNull() {
        try {
            Validate.notNullOrEmpty(null, "String you passed is empty!");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "String you passed is empty!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testNotNullOrEmptyStringEmpty() {
        try {
            Validate.notNullOrEmpty("", "String you passed is empty!");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "String you passed is empty!";
            assertEquals(expected, ex.getMessage());
        }

        try {
            Validate.notNullOrEmpty("   ", "String you passed is empty!");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "String you passed is empty!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testNotAllNullsOrEmptyMessageNull() {
        Validate.notAllNullsOrEmpty(null, null);
    }

    @Test
    public void testNotAllNullsOrEmptyObjectNull() {
        try {
            Validate.notAllNullsOrEmpty(null, "All objects are null objects or are empty");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "Array to check the nullity of objects is null object!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testNotAllNullsOrEmptyAllObjectNull() {
        try {
            Validate.notAllNullsOrEmpty(new String[] { null, "", "  " },
                "All objects are null objects or are empty");
            fail();
        } catch (IllegalStateException ex) {
            String expected = "All objects are null objects or are empty";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSDCardFileName() {
        try {
            Validate.sdCardFileName("card", "SD card name is not valid!");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "SD card name is not valid!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSDCardFileNameBadSuffix() {
        try {
            Validate.sdCardFileName("card.abc", "SD card name is not valid!");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "SD card name is not valid!";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testSDCardFileNameOk() {
        Validate.sdCardFileName("card.img", "SD card name is not valid!");
        Validate.sdCardFileName("a.b.c.img", "SD card name is not valid!");
    }

    @Test
    public void testIsConsolePortValidBadFormat() {
        try {
            Validate.isConsolePortValid("ab2");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Unable to get console port number from the string 'ab2'.";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testIsConsolePortValidLowNumber() {
        try {
            Validate.isConsolePortValid("2000");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Console port is not in the right range or it is not an even number. It has to be in the range "
                + Validate.CONSOLE_PORT_MIN
                + "-" + Validate.CONSOLE_PORT_MAX + ".";
            assertEquals(expected, ex.getMessage());
        }
    }

    @Test
    public void testIsConsolePortValidOddNumber() {
        try {
            Validate.isConsolePortValid("5561");
            fail();
        } catch (AndroidContainerConfigurationException ex) {
            String expected = "Console port is not in the right range or it is not an even number. It has to be in the range "
                + Validate.CONSOLE_PORT_MIN
                + "-" + Validate.CONSOLE_PORT_MAX + ".";
            assertEquals(expected, ex.getMessage());
        }
    }

}
