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
package org.arquillian.droidium.scala.showcase.native_.test01

import org.arquillian.droidium.container.api.AndroidDevice
import org.arquillian.droidium.native_.api.Instrumentable
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.ShrinkWrap
import java.io.File
import org.jboss.arquillian.container.test.api.RunAsClient
import org.jboss.arquillian.container.test.api.TargetsContainer
import org.junit.Test
import org.jboss.arquillian.container.test.api.OperateOnDeployment
import org.jboss.arquillian.test.api.ArquillianResource
import org.arquillian.droidium.container.api.AndroidDevice
import org.jboss.arquillian.drone.api.annotation.Drone
import org.openqa.selenium.WebDriver
import org.junit.Assert._
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

/**
 * Android Droidium testing with Selendroid in Scala.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(classOf[Arquillian])
@RunAsClient
object SelendroidTestAppScalaTestCase {

    @Deployment
    @Instrumentable
    def deployment() : JavaArchive =
        ShrinkWrap createFromZipFile (classOf[JavaArchive], new File("selendroid-test-app-" + System.getProperty("selendroid.version", "0.9.0") + ".apk"))

}

@RunWith(classOf[Arquillian])
@RunAsClient
class SelendroidTestAppScalaTestCase {

    val USER_NAME : String = "john"

    val USER_EMAIL : String = "john@doe.com"

    val USER_PASSWORD : String = "p4ssw0rd"

    val USER_REAL_NAME : String = "John Doe"

    val USER_REAL_OLD : String = "Mr. Burns"

    val USER_PRGRAMMING_LANGUAGE : String = "Scala"

    @Test
    def test01(@ArquillianResource device : AndroidDevice, @Drone driver : WebDriver) {

        device.getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity")

        // Go to user registration
        driver findElement (By id "startUserRegistration") click

        // enter nick
        val userName : WebElement = driver findElement (By id "inputUserName")

        userName sendKeys USER_NAME
        assertEquals (userName getText, USER_NAME);

        // enter e-mail
        val inputEmail : WebElement = driver findElement (By id "inputEmail")
        inputEmail sendKeys USER_EMAIL
        assertEquals (inputEmail getText, USER_EMAIL);

        // enter password
        val inputPassword : WebElement = driver findElement (By id "inputPassword")
        inputPassword sendKeys USER_PASSWORD
        assertEquals(inputPassword getText, USER_PASSWORD);

        // check value in name field, clear it and write new one
        val inputName : WebElement = driver findElement (By id "inputName")
        assertEquals(inputName getText, USER_REAL_OLD)
        inputName clear;
        inputName sendKeys USER_REAL_NAME
        assertEquals(inputName getText, USER_REAL_NAME)

        // enter favorite language
        driver findElement (By id "input_preferedProgrammingLanguage") click

        driver findElement (By linkText USER_PRGRAMMING_LANGUAGE) click

        // accept adds checkbox
        val acceptAddsCheckbox : WebElement = driver findElement (By id "input_adds")
        assertEquals(acceptAddsCheckbox isSelected, false);
        acceptAddsCheckbox click

        // register
        driver findElement (By id "btnRegisterUser") click
    }
}
