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

import org.arquillian.droidium.native_.spi.exception.InvalidInstrumentationConfigurationException;

/**
 * Encapsulates parsed configuration from {@code @Instrumentable} annotation placed on {@code @Deployment} method.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class InstrumentationConfiguration {

    private String port;

    /**
     *
     * @param port port via which the instrumentation will occur, port has to be set in the range 1024 - 65535
     * @throws InvalidInstrumentationConfigurationException when {@code port} is invalid
     */
    public void setPort(int port) throws InvalidInstrumentationConfigurationException {
        setPort(Integer.toString(port));
    }

    /**
     *
     * @param port port Selendroid server will be hooked to on the side of Android device. It has to be a number between 1024
     *        and 65535.
     * @throws InvalidInstrumentationConfigurationException when {@code port} is invalid
     */
    public void setPort(String port) throws InvalidInstrumentationConfigurationException {

        int portToValidate;

        try {
            portToValidate = Integer.parseInt(port);
            if (!isValidPortNumber(portToValidate)) {
                throw new InvalidInstrumentationConfigurationException("Unable to parse instrumentation port, you entered: "
                    + portToValidate);
            }
        } catch (NumberFormatException ex) {
            throw new InvalidInstrumentationConfigurationException(ex);
        }

        this.port = port;
    }

    public String getPort() {
        return port;
    }

    /**
     * Validates configuration, checking if port is set.
     */
    public void validate() {
        if (this.port == null) {
            throw new InvalidInstrumentationConfigurationException("Port not set!");
        }
    }

    /**
     * Checks if {@code port} is in a valid format - it is between 1024 and 65535 including.
     *
     * @param port port to check the validity of
     * @return true if {@code port} is valid, false otherwise
     */
    public static boolean isValidPortNumber(int port) {
        return port >= 1024 && port <= 65535;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((port == null) ? 0 : port.hashCode());
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
        InstrumentationConfiguration other = (InstrumentationConfiguration) obj;
        if (port == null) {
            if (other.port != null)
                return false;
        } else if (!port.equals(other.port))
            return false;
        return true;
    }
}