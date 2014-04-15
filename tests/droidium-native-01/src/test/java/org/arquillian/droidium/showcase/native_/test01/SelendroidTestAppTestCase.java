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
import org.arquillian.droidium.native_.api.Instrumentable;
import org.arquillian.droidium.native_.webdriver.AndroidDriver;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Android Droidium testing with Selendroid - proof of concept.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SelendroidTestAppTestCase {

    @ArquillianResource
    private AndroidDevice android;

    @Drone
    @Selendroid
    private AndroidDriver test_app;

    @Deployment(name = "selendroid-test-app")
    @Instrumentable
    public static Archive<?> SelendroidDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class,
            new File("selendroid-test-app-" + System.getProperty("selendroid.version", "0.9.0") + ".apk"));
    }

    @Deployment(name = "aerogear-test-app")
    @Instrumentable(viaPort = 8082)
    public static Archive<?> createAerogeadDepoyment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("aerogear-test-android.apk"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("selendroid-test-app")
    public void test01() {
        test_app.startActivity("io.selendroid.testapp.HomeScreenActivity");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("selendroid-test-app")
    public void test02() {
        test_app.startActivity(".HomeScreenActivity");
    }

    @Test
    @InSequence(3)
    @OperateOnDeployment("selendroid-test-app")
    public void test03() {
        android.getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");
    }

    @Test
    @InSequence(4)
    @OperateOnDeployment("selendroid-test-app")
    public void test04(@Drone WebDriver aerogear) {
        android.getActivityManager().startActivity("org.jboss.aerogear.pushtest.MainActivity");
    }

    @Test
    @InSequence(5)
    @OperateOnDeployment("aerogear-test-app")
    public void test05(@Drone AndroidDriver driver) {
        driver.startActivity("org.jboss.aerogear.pushtest.MainActivity");
    }

    @Test
    @InSequence(6)
    @OperateOnDeployment("aerogear-test-app")
    public void test06(@Drone WebDriver driver) {
        android.getActivityManager().startActivity("org.jboss.aerogear.pushtest.MainActivity");
    }

    @Test
    @InSequence(7)
    @OperateOnDeployment("aerogear-test-app")
    public void test07(@Drone WebDriver driver) {
        android.getActivityManager().startActivity(".MainActivity");
    }
}
