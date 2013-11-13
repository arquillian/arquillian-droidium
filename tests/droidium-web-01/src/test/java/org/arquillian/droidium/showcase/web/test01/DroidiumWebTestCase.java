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

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.archive.importer.MavenImporter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

/**
 * Shows basic testing of hello-world like application deployed into JBoss AS and tested from Android container point of view.
 * 
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 * 
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DroidiumWebTestCase {

    @Deployment(name = "jbossas", testable = false)
    @TargetsContainer("jbossas")
    public static WebArchive getJBossASDeployment() {
        return ShrinkWrap.create(MavenImporter.class).loadPomFromFile("pom.xml").importBuildOutput().as(WebArchive.class);
    }

    @Test
    @InSequence(1)
    @OperateOnDeployment("jbossas")
    public void test01(@Drone WebDriver driver, @ArquillianResource URL deploymentURL) {
        // get deployment URL
        driver.get(deploymentURL.toString());

        // assert message is seen
        Assert.assertTrue(driver.getPageSource().contains("Hello World!"));
    }

}
