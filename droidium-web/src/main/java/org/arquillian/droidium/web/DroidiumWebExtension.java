/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.droidium.web;

import org.arquillian.droidium.web.impl.AndroidWebDriverInstaller;
import org.arquillian.droidium.web.impl.AndroidWebDriverUninstaller;
import org.arquillian.droidium.web.impl.DroidiumWebConfigurator;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * An extension for Arquillian Droidium web testing support in Arquillian.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class DroidiumWebExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.observer(DroidiumWebConfigurator.class);
        builder.observer(AndroidWebDriverInstaller.class);
        builder.observer(AndroidWebDriverUninstaller.class);
    }
}