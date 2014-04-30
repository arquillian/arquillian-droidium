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
package org.arquillian.droidium.container.tool;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;

import org.arquillian.droidium.container.configuration.Validate;
import org.arquillian.spacelift.tool.Tool;

/**
 * Checks if some port is free or not to hook to.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class FreePortTool extends Tool<Object, Boolean> {

    private int port = -1;

    /**
     * Sets port to check.
     *
     * @param port port to check
     * @throws IllegalArgumentException if {@code port} is null object or an empty string.
     * @throws NumberFormatException if {@code port} is not a number
     * @return
     */
    public FreePortTool port(String port) {
        if (port == null || port.isEmpty()) {
            throw new IllegalArgumentException("Port to check can not be a null object nor an empty string");
        }
        try {
            this.port = Integer.parseInt(port);
            return this;
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("Port '" + port + "' is not a number.");
        }
    }

    /**
     * Checks if some port is free or not.
     *
     * @param port port to check the availability of
     * @return true if {@code port} is free, false otherwise
     */
    private boolean isPortFree(int port) {
        if (!Validate.isPortValid(port)) {
            throw new IllegalArgumentException("Specified port " + port + " is not a valid port.");
        }

        ServerSocket ss = null;
        Socket s = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ds = new DatagramSocket(port);
            // has to be there for Macs
            s = new Socket("localhost", port);
            return true;
        } catch (IOException e) {
        } finally {
            if (ds != null) {
                try {
                    ds.close();
                } catch (Exception ex) {
                }
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }

            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                }
            }
        }

        return false;
    }

    @Override
    protected Collection<String> aliases() {
        return Arrays.asList("free_port");
    }

    @Override
    protected Boolean process(Object input) throws Exception {
        return isPortFree(port);
    }
}
