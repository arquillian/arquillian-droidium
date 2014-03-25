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
import org.arquillian.droidium.showcase.classes.Foo;
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
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Proof of concept test for showing multiple containers on classpath.
 * 
 * While testing web application, you can omit deployment for Android completely.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Arquillian.class)
@RunAsClient
public class MultipleContainersTestCase {

    @Deployment(name = "android")
    @TargetsContainer("android")
    public static Archive<?> createAndroidDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class,
            new File("selendroid-test-app-" + System.getProperty("selendroid.version", "0.9.0") + ".apk"));
    }

    @Deployment(name = "jbossas")
    @TargetsContainer("jbossas")
    public static Archive<?> createJBossASDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "jbossas.jar").addClass(Foo.class);
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("android")
    public void test01(@ArquillianResource AndroidDevice android) {
        Assert.assertTrue(android != null);
        Assert.assertTrue(android.isPackageInstalled("io.selendroid.testapp"));
    }

    @Test
    @InSequence(2)
    @OperateOnDeployment("jbossas")
    public void test02() {
        Assert.assertTrue(true);
    }

}
