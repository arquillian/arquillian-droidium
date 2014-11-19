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
package org.arquillian.droidium.showcase.multiple.test01;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class MultipleContainersTestCase {

    @Deployment(name = "android_1a")
    @TargetsContainer("android_1")
    public static Archive<?> createAndroidDeployment_1a() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("selendroid.test.app")));
    }

    @Deployment(name = "android_1b")
    @TargetsContainer("android_1")
    public static Archive<?> createAndroidDeployment_1b() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("aerogear.android.test.app")));
    }

    @Deployment(name = "android_2")
    @TargetsContainer("android_2")
    public static Archive<?> createAndroidDeployment_2() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("selendroid.test.app")));
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("android_1a")
    public void test01a(@ArquillianResource AndroidDevice android) {
        android.getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("android_1b")
    public void test01b(@ArquillianResource AndroidDevice android) {
        android.getActivityManager().startActivity("org.jboss.aerogear.pushtest.MainActivity");
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("android_2")
    public void test02(@ArquillianResource AndroidDevice android) {
        android.getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");
    }

}
