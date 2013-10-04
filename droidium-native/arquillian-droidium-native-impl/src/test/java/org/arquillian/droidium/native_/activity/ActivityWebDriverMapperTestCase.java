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
package org.arquillian.droidium.native_.activity;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.WebDriver;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(JUnit4.class)
public class ActivityWebDriverMapperTestCase {

    private ActivityWebDriverMapper mapper;


    @Before
    public void setup() {
        mapper = new ActivityWebDriverMapper();

        mapper.put(new DummyWebDriver(), Arrays.asList("foo.bar.Baz","foo.bar.Hello","foo.bar.Hi"));
        mapper.put(new DummyWebDriver(), Arrays.asList("abc.def.Baz","abc.def.Bla","abc.def.Hi"));
    }

    @After
    public void tearDown() {
        mapper = null;
    }

    @Test
    public void getSameInstanceOfFQDNActivities() {
        WebDriver instance = mapper.getInstance("foo.bar.Baz");
        WebDriver instance2 = mapper.getInstance("foo.bar.Hello");

        Assert.assertNotNull(instance);
        Assert.assertNotNull(instance2);

        Assert.assertSame(instance, instance2);
    }

    @Test
    public void getUniqueInstanceOfFQDNActivity() {
        WebDriver instance = mapper.getInstance("foo.bar.Baz");
        Assert.assertNotNull(instance);

        WebDriver instance2 = mapper.getInstance("abc.def.Baz");
        Assert.assertNotNull(instance2);

        Assert.assertNotSame(instance, instance2);
    }

    @Test
    public void getUniqueInstanceOfSimpleNameActivity() {
        WebDriver instance = mapper.getInstance("Hello");
        Assert.assertNotNull(instance);

        WebDriver instance2 = mapper.getInstance("Bla");
        Assert.assertNotNull(instance2);

        Assert.assertNotSame(instance, instance2);
    }

    @Test(expected = NotUniqueWebDriverInstanceException.class)
    public void duplicateWebDriverInstances1() {
        mapper.getInstance("Baz");
    }

    @Test(expected = NotUniqueWebDriverInstanceException.class)
    public void duplicateWebDriverInstances2() {
        mapper.getInstance("Hi");
    }

    @Test(expected = WebDriverInstanceNotFoundException.class)
    public void webdriverInstanceNotFound() {
        mapper.getInstance("SomeActivity");
    }

    @Test
    public void getUniqueWebDriverInstanceOfSuffixName() {
        WebDriver instance = mapper.getInstance("bar.Baz");
        Assert.assertNotNull(instance);

        WebDriver instance2 = mapper.getInstance("def.Baz");
        Assert.assertNotNull(instance2);

        Assert.assertNotSame(instance, instance2);
    }
}
