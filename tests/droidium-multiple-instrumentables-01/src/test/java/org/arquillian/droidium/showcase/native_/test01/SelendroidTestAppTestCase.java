/**
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
package org.arquillian.droidium.showcase.native_.test01;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.arquillian.droidium.container.api.Screenshooter;
import org.arquillian.droidium.native_.api.Instrumentable;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * This is highly experimental project and besides this functionality it is unknown what happens.
 * 
 * Do not try this at home!
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SelendroidTestAppTestCase {

    @ArquillianResource
    AndroidDevice android;

    @ArquillianResource
    Screenshooter screenshooter;

    @Deployment(name = "aerogear", order = 1)
    @TargetsContainer("android")
    public static Archive<?> getEmailValidator() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("email-validator-app.apk"));
    }

    @Deployment(name = "testApp", order = 2)
    @Instrumentable(viaPort = 8080)
    @TargetsContainer("android")
    public static Archive<?> getInstrumented() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("instrumented-app.apk"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("testApp")
    public void test01(@Drone @TestApp WebDriver testApp) {
        Assert.assertNotNull(testApp);
        Assert.assertNotNull(android);
        Assert.assertNotNull(screenshooter);
    }

}
