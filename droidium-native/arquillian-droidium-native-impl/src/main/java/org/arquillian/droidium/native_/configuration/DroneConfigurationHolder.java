package org.arquillian.droidium.native_.configuration;

import org.jboss.arquillian.core.spi.Validate;

/**
 * Holder of information logically related together for some Drone instance. It holds drone type, qualifier and port.
 *
 * Qualifier reflects qualifier put on Drone instance in test case itself as well as the suffix in extension configuration.
 *
 * Port is parsed from {@code remoteAddress} configuration property, when not specified, original defaults from Drone extension
 * itself are used.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroneConfigurationHolder {

    private String droneType;

    private String qualifier;

    private String port;

    /**
     *
     * @param droneType
     * @param qualifier
     * @param port
     * @throws IllegalArgumentException if some argument is a null object or an empty string
     */
    public DroneConfigurationHolder(String droneType, String qualifier, String port) {
        Validate.notNullOrEmpty(droneType, "Drone type you want to set can not be a null object nor an empty string!");
        Validate.notNullOrEmpty(qualifier, "Qualifier you want to set can not be a null object nor an empyt string!");
        Validate.notNullOrEmpty(port, "Port you want to set can not be a null object nor an empty string!");
        this.droneType = droneType;
        this.qualifier = qualifier;
        this.port = port;
    }

    public String getDroneType() {
        return droneType;
    }

    public String getQualifier() {
        return qualifier;
    }

    public String getPort() {
        return port;
    }

}
