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
package org.arquillian.droidium.showcase.web.test01;

import java.io.File;
import java.net.URL;

import org.arquillian.droidium.container.api.AndroidDevice;
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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.android.AndroidDriver;

/**
 * Shows basic testing of hello-world like application deployed into JBoss AS and tested from Android container point of view.
 * 
 * If you really want, you can install packages on Android device as you are used to.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DroidiumWebTestCase {

    @Deployment(name = "android")
    @TargetsContainer("android")
    public static Archive<?> getAndroidDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("selendroid-test-app-0.5.1.apk"));
    }

    @Deployment(name = "jbossas", testable = false)
    @TargetsContainer("jbossas")
    public static Archive<?> getJBossASDeployment() {
        return ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/jboss-as-helloworld.war"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("jbossas")
    public void test01(@Drone AndroidDriver driver, @ArquillianResource URL deploymentURL) {
        // get deployment URL
        driver.get(deploymentURL.toString());

        // assert message is seen
        Assert.assertTrue(driver.getPageSource().contains("Hello World!"));
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("android")
    public void test02(@ArquillianResource AndroidDevice device) {
        Assert.assertNotNull(device);
        Assert.assertTrue(device.isPackageInstalled("io.selendroid.testapp"));
    }
}
