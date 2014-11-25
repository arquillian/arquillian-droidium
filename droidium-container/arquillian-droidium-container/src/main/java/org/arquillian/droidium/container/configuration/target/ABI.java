/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.arquillian.droidium.container.configuration.target;

/**
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public enum ABI {

    NOT_DEFINED("not-defined"),
    X86("x86"),
    X86_64("x86_64"),
    ARMEABI("armeabi"),
    ARMEABI_V7A("armeabi-v7a"),
    MIPS("mips");

    private String name;

    private ABI(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ABI match(String name) {
        if (name == null) {
            return null;
        }

        for (ABI type : values()) {
            if (type.toString().equals(name)) {
                return type;
            }
        }

        return null;
    }
}
