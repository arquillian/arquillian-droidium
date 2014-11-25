/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.arquillian.droidium.container.configuration.target;

import java.util.List;

import org.arquillian.droidium.container.configuration.AndroidContainerConfiguration;
import org.arquillian.droidium.container.configuration.AndroidContainerConfigurationException;
import org.jboss.arquillian.core.spi.Validate;

/**
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class TargetPicker {

    private final TargetRegistry targetRegistry;

    private final AndroidContainerConfiguration configuration;

    public TargetPicker(TargetRegistry targetRegistry, AndroidContainerConfiguration configuration) {
        Validate.notNull(targetRegistry, "Target registry is null!");
        Validate.notNull(configuration, "Configuration is null!");

        this.targetRegistry = targetRegistry;
        this.configuration = configuration;
    }

    public Target pick() {

        if (targetRegistry.getTargets().isEmpty()) {
            throw new AndroidContainerConfigurationException("There are no targets to choose from!");
        }

        String target = configuration.getTarget();

        Target pickedTarget = null;

        if (target == null || target.isEmpty()) {
            pickedTarget = targetRegistry.getHighest(TARGET_TYPE.PLATFORM);

            if (pickedTarget == null) {
                pickedTarget = targetRegistry.getHighest(TARGET_TYPE.ADD_ON);
            }

            if (pickedTarget == null) {
                throw new AndroidContainerConfigurationException("You have not set target in the Android container "
                    + "configuration and it is impossible to choose it.");
            }
        } else {
            // try to resolve it
            pickedTarget = targetRegistry.getByIdLabel(target);

            if (pickedTarget == null) {
                // maybe you set just API level?
                if (isNumber(target)) {
                    // there can be platform and addon of the same api level
                    List<Target> pickedTargets = targetRegistry.getByApiLevel(target);

                    // just grab the platform of that API first
                    for (Target t : pickedTargets) {
                        if (t.isPlatorm()) {
                            pickedTarget = t;
                        }
                    }

                    // we do not have platform so everything will be add-on then
                    if (pickedTarget == null) {
                        pickedTarget = pickedTargets.iterator().next();
                    }
                }
            }
        }

        // we still failed to get the target
        if (pickedTarget == null) {
            throw new AndroidContainerConfigurationException("Unable to resolve target: " + target);
        }

        // setting what we just resolved
        configuration.setTarget(pickedTarget.getIdLabel());

        List<TagAbiPair> availableTagAbis = pickedTarget.getAbis();

        if (availableTagAbis.isEmpty()) {
            throw new AndroidContainerConfigurationException("There are not ABIs for the given platform!");
        }

        TagAbiPair resolvedTagAbiPair = TagAbiPair.construct(configuration.getAbi());

        // you have put no abi into configuration
        // or it does not have format "tag/abi"
        if (resolvedTagAbiPair == null) {
            // when you did not set abi in configuration, it will be
            // "default/null" which resolves to TagAbi pair of Tag.DEFAULT, ABI.NOT_DEFINED
            resolvedTagAbiPair = TagAbiPair.construct("default/" + configuration.getAbi());
        }

        // there is such pair for the given target
        if (availableTagAbis.contains(resolvedTagAbiPair)) {
            configuration.setAbi(resolvedTagAbiPair.toString());
        } else {
            // lets choose default
            List<TagAbiPair> defaultAbis = pickedTarget.getDefaultAbis();

            // there are not any default abis, so we have to pick whatever we have
            if (defaultAbis.isEmpty()) {
                configuration.setAbi(availableTagAbis.iterator().next().toString());
            } else {
                // otherwise pick some abi from these default ones
                configuration.setAbi(defaultAbis.iterator().next().toString());
            }
        }

        return pickedTarget;
    }

    private boolean isNumber(String number) {
        try {
            Integer.parseInt(number);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

}
