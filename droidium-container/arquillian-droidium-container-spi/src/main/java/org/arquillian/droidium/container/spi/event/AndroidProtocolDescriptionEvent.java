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
package org.arquillian.droidium.container.spi.event;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;

/**
 * Event fired in Android container lifecycle.
 *
 * Serves as a hook to extensions which implements various testing scenarios to provide {@link ProtocolDescription} to Android
 * container so the contaienr is agnostic to testing scenario.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class AndroidProtocolDescriptionEvent {

    private ProtocolDescription protocolDescription;

    /**
     *
     * @param protocolDescription
     * @throws IllegalArgumentException if {@code protocolDescription} is a null object
     */
    public void setProtocolDescription(ProtocolDescription protocolDescription) throws IllegalArgumentException {
        if (protocolDescription == null) {
            throw new IllegalArgumentException("Protocol description to set can't be a null object");
        }
        this.protocolDescription = protocolDescription;
    }

    /**
     *
     * @return description of the protocol the extension is providing
     */
    public ProtocolDescription getProtocolDescription() {
        return protocolDescription;
    }
}
