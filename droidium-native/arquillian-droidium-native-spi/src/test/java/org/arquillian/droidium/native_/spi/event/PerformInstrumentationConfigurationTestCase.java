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
package org.arquillian.droidium.native_.spi.event;

import junit.framework.Assert;

import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.arquillian.droidium.native_.spi.exception.InvalidInstrumentationConfigurationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class PerformInstrumentationConfigurationTestCase {

    @Test
    public void testConfigurationEqualityWithNull() {
        InstrumentationConfiguration c1 = null;
        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        Assert.assertFalse(c2.equals(c1));
    }

    @Test
    public void testConfigurationInequality() {
        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort("8080");
        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        c2.setPort("8081");
        Assert.assertFalse(c2.equals(c1));
    }

    @Test
    public void testConfigurationEqualityWithStrings() {
        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort("8080");
        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        c2.setPort("8080");
        Assert.assertTrue(c1.equals(c2));
    }

    @Test
    public void testConfigurationEqualityWithIntAndString() {
        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort(8080);
        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        c2.setPort("8080");
        Assert.assertTrue(c1.equals(c2));
    }

    @Test(expected = InvalidInstrumentationConfigurationException.class)
    public void testConfigurationWithInvalidPort() {
        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort("abc");
    }

    @Test(expected = InvalidInstrumentationConfigurationException.class)
    public void testPerformInstrumentationEventWithUnsetPortForConfiguration() {
        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        new PerformInstrumentation("someDeployment", c1);
    }

}
