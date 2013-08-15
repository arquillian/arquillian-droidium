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
package org.arquillian.droidium.native_;

import org.arquillian.droidium.native_.impl.DeploymentController;
import org.arquillian.droidium.native_.impl.InstrumentationController;
import org.arquillian.droidium.native_.impl.DroidiumNativeConfigurator;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * The extension for Droidium native testing support in Arquillian.
 *
 * @author <a href="mailto:smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumNativeExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(DroidiumNativeConfigurator.class);
        builder.observer(DeploymentController.class);
        builder.observer(InstrumentationController.class);
    }

}
