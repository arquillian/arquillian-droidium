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
package org.arquillian.droidium.native_.instrumentation;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.arquillian.droidium.native_.AbstractAndroidTestTestBase;
import org.arquillian.droidium.native_.instrumentation.InstrumentationScanner;
import org.arquillian.droidium.native_.instrumentation.InstrumentationMapperException;
import org.arquillian.droidium.native_.spi.InstrumentationConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class InstrumentationMapperTestCase extends AbstractAndroidTestTestBase {

    @Test
    public void testEmptyMap() {
        Map<String, InstrumentationConfiguration> map = new HashMap<String, InstrumentationConfiguration>();
        Assert.assertTrue(InstrumentationScanner.validate(map));
    }

    @Test(expected = InstrumentationMapperException.class)
    public void testDuplicates() {
        Map<String, InstrumentationConfiguration> map = new HashMap<String, InstrumentationConfiguration>();

        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort(8080);

        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        c2.setPort(8080);

        map.put("deployment1", c1);
        map.put("deployment2", c2);

        InstrumentationScanner.validate(map);
    }

    @Test
    public void testInstrumentationMap() {
        Map<String, InstrumentationConfiguration> map = new HashMap<String, InstrumentationConfiguration>();

        InstrumentationConfiguration c1 = new InstrumentationConfiguration();
        c1.setPort(8080);

        InstrumentationConfiguration c2 = new InstrumentationConfiguration();
        c2.setPort(8081);

        map.put("deployment1", c1);
        map.put("deployment2", c2);

        Assert.assertTrue(InstrumentationScanner.validate(map));
    }
}
