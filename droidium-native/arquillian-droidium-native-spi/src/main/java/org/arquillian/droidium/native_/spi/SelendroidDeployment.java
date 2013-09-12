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
package org.arquillian.droidium.native_.spi;

import java.io.File;

import org.jboss.arquillian.core.spi.Validate;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Holds deployment resources of Selendroid server. Note that you can install more than one Selendroid server so there is the
 * same concept behind this logic as behind ordinary {@link AndroidDeployment}.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDeployment extends DroidiumDeployment {

    private File workingCopy;

    private File rebuilt;

    private File resigned;

    private String serverBasePackage;

    private AndroidDeployment instrumentedDeployment;

    private InstrumentationConfiguration configuration;

    private String androidDeploymentName;

    private String selendroidPackageName;

    /**
     *
     * @param selendroidWorkingCopy
     * @throws IllegalArgumentException if {@code selendroidWorkingCopy} is a null object
     * @return this
     */
    public SelendroidDeployment setWorkingCopy(File selendroidWorkingCopy) {
        Validate.notNull(selendroidWorkingCopy, "Selendroid working copy to set can not be a null object!");
        this.workingCopy = selendroidWorkingCopy;
        return this;
    }

    public File getWorkingCopy() {
        return workingCopy;
    }

    /**
     * Sets rebuilt Selendroid server after its modification which reflects Android application to instrument.
     *
     * @param rebuiltSelendroid
     * @throws IllegalArgumentException if {@code rebuiltSelendroid} is a null object
     * @return this
     */
    public SelendroidDeployment setRebuilt(File rebuiltSelendroid) {
        Validate.notNull(rebuiltSelendroid, "Rebuilt Selendroid server to set can not be a null object!");
        this.rebuilt = rebuiltSelendroid;
        return this;
    }

    public File getRebuilt() {
        return rebuilt;
    }

    /**
     * Sets resigned Selendroid server after it is rebuilt. This resource will be actually sent to Android device and used to
     * instrument paired package.
     *
     * @param resignedSelendroid
     * @throws IllegalArgumentException if {@code resignedSelendroid} is a null object
     * @return this
     */
    public SelendroidDeployment setResigned(File resignedSelendroid) {
        Validate.notNull(resignedSelendroid, "Resigned Selendroid server to set can not be a null object!");
        this.resigned = resignedSelendroid;
        return this;
    }

    public File getResigned() {
        return resigned;
    }

    /**
     *
     * @param serverBasePackage
     * @throws IllegalArgumentException if {@code serverBasePackage} is a null object or an empty string
     * @return this
     */
    public SelendroidDeployment setServerBasePackage(String serverBasePackage) {
        Validate.notNullOrEmpty(serverBasePackage,
            "Selendroid server base package to set can not be a null object nor an empty string!");
        this.serverBasePackage = serverBasePackage;
        return this;
    }

    public String getServerBasePackage() {
        return serverBasePackage;
    }

    @Override
    public Archive<?> getDeployment() {
        if (getInstrumentedDeployment() == null) {
            throw new IllegalStateException("You have not set what deployment you want to instrument yet. Please "
                + "call setInstrumentedDeployment(AndroidDeployment) firstly.");
        }
        return getInstrumentedDeployment().getDeployment();
    }

    /**
     * Sets {@link AndroidDeployment} meant to be instrumented by this Selendroid server.
     *
     * @param instrumentedDeployment
     * @throws IllegalArgumentException if {@code instrumentedDeployment} is a null object
     * @return this
     */
    public SelendroidDeployment setInstrumentedDeployment(AndroidDeployment instrumentedDeployment) {
        Validate.notNull(instrumentedDeployment, "Android deployment can not be a null object!");
        this.instrumentedDeployment = instrumentedDeployment;
        return this;
    }

    public AndroidDeployment getInstrumentedDeployment() {
        return this.instrumentedDeployment;
    }

    /**
     * Sets instrumentation configuration for this Selendroid deployment. Instrumentation configuration is tied to Android
     * deployment which the instance of Selendroid server is instrumenting.
     *
     * @param configuration
     * @throws IllegalArgumentException if {@code configuration} is a null object
     * @return this
     */
    public SelendroidDeployment setInstrumentationConfiguration(InstrumentationConfiguration configuration) {
        Validate.notNull(configuration, "Instrumentation configuration for Selendroid deployment can not be a null object!");
        this.configuration = configuration;
        return this;
    }

    public InstrumentationConfiguration getInstrumentationConfiguration() {
        return this.configuration;
    }

    @Override
    public String getDeploymentName() {
        return androidDeploymentName;
    }

    /**
     *
     * @param androidDeploymentName
     * @return this
     */
    public SelendroidDeployment setDeploymentName(String androidDeploymentName) {
        Validate.notNullOrEmpty(androidDeploymentName, "Android deployment name for Selendroid server deployment can not "
            + "be a null object nor an empty string!");
        this.androidDeploymentName = androidDeploymentName;
        return this;
    }

    /**
     *
     * @param selendroidPackageName name of the Selendroid package as in its AndroidManifest.xml
     * @return this
     */
    public SelendroidDeployment setSelendroidPackageName(String selendroidPackageName) {
        Validate.notNullOrEmpty(selendroidPackageName,
            "Selendroid package name to set can not be a null object not an empty string!");
        this.selendroidPackageName = selendroidPackageName;
        return this;
    }

    public String getSelendroidPackageName() {
        return this.selendroidPackageName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((selendroidPackageName == null) ? 0 : selendroidPackageName.hashCode());
        result = prime * result + ((serverBasePackage == null) ? 0 : serverBasePackage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SelendroidDeployment other = (SelendroidDeployment) obj;
        if (selendroidPackageName == null) {
            if (other.selendroidPackageName != null)
                return false;
        } else if (!selendroidPackageName.equals(other.selendroidPackageName))
            return false;
        if (serverBasePackage == null) {
            if (other.serverBasePackage != null)
                return false;
        } else if (!serverBasePackage.equals(other.serverBasePackage))
            return false;
        return true;
    }

}
