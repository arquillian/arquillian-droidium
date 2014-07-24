/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.arquillian.extension.recorder.screenshooter.droidium.impl;

import java.io.File;

import org.arquillian.extension.recorder.screenshooter.Screenshooter;
import org.arquillian.extension.recorder.screenshooter.Screenshot;
import org.arquillian.extension.recorder.screenshooter.ScreenshotType;
import org.arquillian.extension.recorder.screenshooter.event.TakeScreenshot;
import org.arquillian.extension.recorder.screenshooter.impl.ScreenshotReportEntryBuilder;
import org.arquillian.recorder.reporter.PropertyEntry;
import org.arquillian.recorder.reporter.event.PropertyReportEvent;
import org.arquillian.recorder.reporter.impl.TakenResourceRegister;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;

/**
 * @author <a href="mailto:pmensik@redhat.com">Stefan Miklosovic</a>
 */
public class DroidiumScreenshotTaker {

    @Inject
    private Instance<Screenshooter> screenshooter;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<PropertyReportEvent> propertyReportEvent;

    @Inject
    private Instance<TakenResourceRegister> takenScreenshotsRegister;

    public void onTakeScreenshot(@Observes TakeScreenshot event) {

        ScreenshotType type = screenshooter.get().getScreenshotType();

        File screenshotTarget = new File(
            new File(event.getMetaData().getTestClassName(), event.getMetaData().getTestMethodName()),
            event.getFileName());

        Screenshot screenshot = screenshooter.get().takeScreenshot(screenshotTarget, type);
        takenScreenshotsRegister.get().addTaken(screenshot);

        event.getMetaData().setHeight(screenshot.getHeight());
        event.getMetaData().setWidth(screenshot.getWidth());

        PropertyEntry propertyEntry = new ScreenshotReportEntryBuilder()
            .withWhen(event.getWhen())
            .withMetadata(event.getMetaData())
            .withScreenshot(screenshot)
            .build();

        takenScreenshotsRegister.get().addReported(screenshot);
        propertyReportEvent.fire(new PropertyReportEvent(propertyEntry));
    }
}
