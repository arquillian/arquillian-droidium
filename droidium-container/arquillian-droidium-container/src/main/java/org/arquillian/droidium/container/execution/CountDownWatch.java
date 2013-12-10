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
package org.arquillian.droidium.container.execution;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * A simple utility to measure time left from an timeout.
 *
 * Note that MICROSECONDS and MILLISECONDS are not supported as time units.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class CountDownWatch {

    private long timeStart;
    private long timeout;
    private TimeUnit unit;

    /**
     * Creates a countdown watch and starts it
     *
     * @param timeout timeout
     * @param unit timeout unit
     */
    public CountDownWatch(long timeout, TimeUnit unit) {

        if (EnumSet.of(TimeUnit.MICROSECONDS, TimeUnit.MILLISECONDS).contains(unit)) {
            throw new IllegalArgumentException(MessageFormat.format("Time Unit {0} is not supported", unit));
        }

        this.timeStart = System.currentTimeMillis();
        this.timeout = timeout;
        this.unit = unit;
    }

    /**
     * @return Time left till timeout
     */
    public long timeLeft() {
        long currentTime = System.currentTimeMillis();
        long timeoutInMillis = TimeUnit.MILLISECONDS.convert(timeout, unit);
        return unit.convert(timeoutInMillis - (currentTime - timeStart), TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @return time elapsed since started
     */
    public long timeElapsed() {
        long currentTime = System.currentTimeMillis();
        return unit.convert(currentTime - timeStart, TimeUnit.MILLISECONDS);
    }

    /**
     * Restarts clocks
     */
    public void reset() {
        this.timeStart = System.currentTimeMillis();
    }

    /**
     *
     * @return timeout set during initialization
     */
    public long timeout() {
        return timeout;
    }

    /**
     *
     * @return timeout unit
     */
    public TimeUnit getTimeUnit() {
        return unit;
    }
}
