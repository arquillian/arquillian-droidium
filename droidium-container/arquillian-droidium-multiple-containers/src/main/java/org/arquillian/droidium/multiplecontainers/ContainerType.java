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
package org.arquillian.droidium.multiplecontainers;

/**
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public enum ContainerType {

    ANDROID("android", "org.arquillian.droidium.container.AndroidDeployableContainer"),
    DROIDIUM("droidium", "org.arquillian.droidium.container.AndroidDeployableContainer"),
    JBOSS("jboss", "org.jboss.as.arquillian.container"),
    EAP("eap", "org.jboss.as.arquillian.container"),
    WILDFLY("wildfly", "org.wildfly.arquillian.container"),
    TOMEE("tomee", "org.apache.openejb.arquillian"),
    GLASSFISH("glassfish", "org.jboss.arquillian.container.glassfish"),
    OPENSHIFT("openshift", "org.jboss.arquillian.container.openshift");

    private String qualifier;
    private String adapterImplClassPrefix;

    private ContainerType(String qualifier, String adapterImplClassPrefix) {
        this.qualifier = qualifier;
        this.adapterImplClassPrefix = adapterImplClassPrefix;
    }

    public static String getAdapterClassNamePrefix(ContainerType type) {
        switch (type) {
            case ANDROID:
                return ANDROID.adapterImplClassPrefix;
            case DROIDIUM:
                return DROIDIUM.adapterImplClassPrefix;
            case GLASSFISH:
                return GLASSFISH.adapterImplClassPrefix;
            case JBOSS:
                return JBOSS.adapterImplClassPrefix;
            case EAP:
                return EAP.adapterImplClassPrefix;
            case WILDFLY:
                return WILDFLY.adapterImplClassPrefix;
            case OPENSHIFT:
                return OPENSHIFT.adapterImplClassPrefix;
            case TOMEE:
                return TOMEE.adapterImplClassPrefix;
            default:
                break;
        }
        return null;
    }

    @Override
    public String toString() {
        return qualifier;
    }

    /**
     * @return all containers as one string separated by one space from each other
     */
    public static String getAll() {
        StringBuilder sb = new StringBuilder();

        for (ContainerType type : ContainerType.values()) {
            sb.append(type.qualifier);
            sb.append(" ");
        }

        return sb.toString().trim();
    }
}
