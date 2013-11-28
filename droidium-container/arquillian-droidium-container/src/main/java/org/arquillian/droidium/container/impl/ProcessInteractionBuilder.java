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
package org.arquillian.droidium.container.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Builder API for process interaction. It uses regular expression to match allowed and error output.
 *
 * @see ProcessInteraction
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class ProcessInteractionBuilder {

    /**
     * No interaction instance
     */
    public static final ProcessInteraction NO_INTERACTION = new ProcessInteractionBuilder().build();

    private Map<String, String> replyMap;

    private List<Pattern> allowedOutput;

    private List<Pattern> errorOutput;

    private Tuple tuple;

    /**
     * Creates empty interaction builder
     */
    public ProcessInteractionBuilder() {
        this.replyMap = new LinkedHashMap<String, String>();
        this.allowedOutput = new ArrayList<Pattern>();
        this.errorOutput = new ArrayList<Pattern>();
        this.tuple = new Tuple();
    }

    /**
     * Marks a line that should be considered as a question to be answered. Must be followed by {@see this#with(String)} call
     *
     * @param outputLine The question
     * @return current instance to allow chaining
     */
    public ProcessInteractionBuilder replyTo(String outputLine) {
        if (tuple.first != null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please append with(String) call");
        }
        tuple.first = outputLine;

        return this;
    }

    /**
     * Stores an answer for question defined by {@see this#replyTo(String)} call
     *
     * @param response the answer
     * @return current instance to allow chaining
     */
    public ProcessInteractionBuilder with(String response) {
        if (tuple.first == null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please prepend replyTo(String) call");
        }

        tuple.last = response;

        replyMap.put(tuple.first, tuple.last);
        tuple = new Tuple();

        return this;
    }

    /**
     * Adds {@code outputLine} that should be printed out to standard output
     *
     * @param pattern the line
     * @return current instance to allow chaining
     */
    public ProcessInteractionBuilder outputs(String pattern) {

        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        allowedOutput.add(p);
        return this;
    }

    /**
     * Adds {@code outputLine} that should be printed out to standard error output
     *
     * @param pattern the line
     * @return current instance to allow chaining
     */
    public ProcessInteractionBuilder errors(String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.DOTALL);
        errorOutput.add(p);
        return this;
    }

    /**
     * Builds {@see ProcessInteraction} object from defined data
     *
     * @return {@link ProcessInteraction}
     */
    public ProcessInteraction build() {
        if (tuple.first != null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please append with(String) call");
        }

        return new ProcessInteractionImpl(replyMap, allowedOutput, errorOutput);
    }

    private static class Tuple {
        String first;
        String last;
    }

    private static class ProcessInteractionImpl implements ProcessInteraction {

        private final Map<String, String> replyMap;

        private final List<Pattern> allowedOutput;

        private final List<Pattern> errorOutput;

        public ProcessInteractionImpl(Map<String, String> replyMap, List<Pattern> allowedOutput, List<Pattern> errorOutput) {
            this.replyMap = replyMap;
            this.allowedOutput = allowedOutput;
            this.errorOutput = errorOutput;
        }

        @Override
        public String repliesTo(String outputLine) {
            return replyMap.get(outputLine);
        }

        @Override
        public boolean shouldOutput(String outputLine) {
            for (Pattern p : allowedOutput) {
                if (p.matcher(outputLine).matches()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldOutputToErr(String outputLine) {
            for (Pattern p : errorOutput) {
                if (p.matcher(outputLine).matches()) {
                    return true;
                }
            }
            return false;

        }

    }
}
