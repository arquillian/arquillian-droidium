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
package org.arquillian.droidium.native_.impl;

import java.io.File;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Holds Selendroid deployment resources.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class SelendroidDeployment {

    private File workingCopy;

    private File rebuilt;

    private File resigned;

    private String basePackage;

    /**
     *
     * @param selendroidWorkingCopy
     * @throws IllegalArgumentException if {@code selendroidWorkingCopy} is a null object
     */
    public void setWorkingCopy(File selendroidWorkingCopy) {
        Validate.notNull(selendroidWorkingCopy, "File to set can not be a null object!");
        this.workingCopy = selendroidWorkingCopy;
    }

    /**
     *
     * @param rebuiltSelendroid
     * @throws IllegalArgumentException if {@code rebuiltSelendroid} is a null object
     */
    public void setRebuilt(File rebuiltSelendroid) {
        Validate.notNull(rebuiltSelendroid, "File to set can not be a null object!");
        this.rebuilt = rebuiltSelendroid;
    }

    /**
     *
     * @param resignedSelendroid
     * @throws IllegalArgumentException if {@code resignedSelendroid} is a null object
     */
    public void setResigned(File resignedSelendroid) {
        Validate.notNull(resignedSelendroid, "File to set can not be a null object!");
        this.resigned = resignedSelendroid;
    }

    /**
     *
     * @param basePackage
     * @throws IllegalArgumentException if {@code basePackage} is a null object or an empty string
     */
    public void setBasePackage(String basePackage) {
        Validate.notNullOrEmpty(basePackage, "Base package to set can not be a null object or an empty string!");
        this.basePackage = basePackage;
    }

    public File getWorkingCopy() {
        return workingCopy;
    }

    public File getRebuilt() {
        return rebuilt;
    }

    public File getResigned() {
        return resigned;
    }

    public String getBasePackage() {
        return basePackage;
    }

}
