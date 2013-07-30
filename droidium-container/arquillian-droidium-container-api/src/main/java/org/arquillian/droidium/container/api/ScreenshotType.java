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
package org.arquillian.droidium.container.api;

/**
 * Format of screenshot we want to have.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public enum ScreenshotType {
    JPEG {
        @Override
        public String toString() {
            return "jpeg";
        }
    },
    PNG {
        @Override
        public String toString() {
            return "png";
        }
    },
    BMP {
        @Override
        public String toString() {
            return "bmp";
        }
    },
    WBMP {
        @Override
        public String toString() {
            return "wbmp";
        }
    },
    GIF {
        @Override
        public String toString() {
            return "gif";
        }
    };
}
