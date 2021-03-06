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
package org.arquillian.droidium.showcase.selection;

import java.io.File;

import org.arquillian.droidium.container.api.AndroidDevice;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Android Droidium - controlling of activities without native plugin.
 *
 * Even we do not specify avdName, serialId nor consolePort in arquillian.xml, we are able to select some running device when it
 * is the only one started and connected.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
@RunWith(Arquillian.class)
@RunAsClient
public class AndroidDeviceSelectionTestCase {

    @ArquillianResource
    AndroidDevice android;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.createFromZipFile(JavaArchive.class, new File(System.getProperty("selendroid.test.app")));
    }

    @Test
    public void startAndStopSomeActivityTest() {
        android.getActivityManager().startActivity("io.selendroid.testapp.HomeScreenActivity");
        android.getActivityManager().stopActivity("io.selendroid.testapp.HomeScreenActivity");
    }

}
