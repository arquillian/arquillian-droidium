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

    ANDROID {
        @Override
        public String toString() {
            return "android";
        }
    },
    DROIDIUM { // alias to android
        @Override
        public String toString() {
            return "droidium";
        }
    },
    JBOSS {
        @Override
        public String toString() {
            return "jboss";
        }
    },
    EAP { // alias to jboss
        @Override
        public String toString() {
            return "eap";
        }
    },
    WILDFLY { // alias to jboss
        @Override
        public String toString() {
            return "wildfly";
        }
    },
    TOMEE {
        @Override
        public String toString() {
            return "tomee";
        }
    },
    GLASSFISH {
        @Override
        public String toString() {
            return "glassfish";
        }
    },
    OPENSHIFT {
        @Override
        public String toString() {
            return "openshift";
        }
    };

    public static String getAdapterClassNamePrefix(ContainerType type) {
        switch (type) {
            case ANDROID:
                return "org.arquillian.droidium.container.AndroidDeployableContainer";
            case DROIDIUM:
                return "org.arquillian.droidium.container.AndroidDeployableContainer";
            case GLASSFISH:
                return "org.jboss.arquillian.container.glassfish";
            case JBOSS:
                return "org.jboss.as.arquillian.container";
            case EAP:
                return "org.jboss.as.arquillian.container";
            case WILDFLY:
                return "org.jboss.as.arquillian.container";
            case OPENSHIFT:
                return "org.jboss.arquillian.container.openshift";
            case TOMEE:
                return "org.apache.openejb.arquillian";
            default:
                break;
        }
        return null;
    }

    /**
     * @return all containers as one string separated by one space from each other
     */
    public static String getAll() {
        StringBuilder sb = new StringBuilder();

        for (ContainerType type : ContainerType.values()) {
            sb.append(type.toString());
            sb.append(" ");
        }

        return sb.toString().trim();
    }
}
