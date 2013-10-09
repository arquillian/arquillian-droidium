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

    // injection for switching between activities
    @ArquillianResource
    AndroidDevice android;

    // class scoped Drone, it will be available to use
    // during whole test execution
    @Drone
    @Selendroid
    WebDriver test_app;

    // port put here matches the one in arquillian.xml
    // and in turn "selendroid" suffix has to match annotation
    // put on WebDriver
    @Deployment(name = "selendroid-test-app")
    @Instrumentable(viaPort = 8081) // by default, port is 8080, it has to match with extension's remoteAddress port
    @TargetsContainer("android")
    public static Archive<?> SelendroidDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("selendroid-test-app-0.5.1.apk"));
    }

    // port put here matches the one in arquillian.xml
    // and in turn "aerogear" suffix has to match annotation
    // put on WebDriver in test02 method
    @Deployment(name = "aerogear-test-app")
    @Instrumentable(viaPort = 8082)
    @TargetsContainer("android")
    public static Archive<?> createAerogeadDepoyment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File("aerogear-test-android.apk"));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("selendroid-test-app")
    public void test01() {
        // activities are automatically scanned upon deployment installation and Android
        // activity manager knows on which WebDriver instance it should start that activity up
        android.getActivityManagerProvider()
            .getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");

        // ... tests
    }

    // Showing of method scoped Drone, it will be possible to use it only in this method
    @Test
    @InSequence(2)
    @OperateOnDeployment("aerogear-test-app")
    public void test02(@Drone @Aerogear WebDriver aerogear) {
        android.getActivityManagerProvider()
            .getActivityManager().startActivity("org.jboss.aerogear.pushtest.MainActivity");

        // ... tests

        // you can do something like this after you want to switch to another activity
        // android.getActivityManagerProvider().getActivityManager().startActivity("another.activity")

        // since you have both Drones available here (class scoped and method scoped as well) you can
        // choose whatever activity from both deployments you want. After this method ends, you can
        // start activities only from the selendroid-test-app deployment since the second WebDriver
        // is destroyed
        //
        // Selendroid server for particular WebDriver instance is uninstalled upon every destruction
        // of that instance. For class scoped Drone, it occurs in the end of the class (AfterClass).
        // For method scoped Drones, it occurs in the end of the method (After). AfterClass and
        // After are here mentioned in Arquillian sense.
    }

}
