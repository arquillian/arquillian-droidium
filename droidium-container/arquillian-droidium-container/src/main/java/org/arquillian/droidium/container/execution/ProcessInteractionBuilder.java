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
package org.arquillian.droidium.container.execution;

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

    private Map<Pattern, Answer> replyMap;

    private List<Pattern> allowedOutput;

    private List<Pattern> errorOutput;

    private Tuple tuple;

    /**
     * Creates empty interaction builder
     */
    public ProcessInteractionBuilder() {
        this.replyMap = new LinkedHashMap<Pattern, Answer>();
        this.allowedOutput = new ArrayList<Pattern>();
        this.errorOutput = new ArrayList<Pattern>();
        this.tuple = new Tuple();
    }

    /**
     * Marks a line that should be considered as a question to be answered. Must be followed by
     * {@link ProcessInteractionBuilder#with(String)} call
     *
     * @param outputLine The question
     * @return current instance to allow chaining
     */
    public ProcessInteractionBuilder replyTo(String outputLine) {
        if (tuple.question != null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please append with(String) call");
        }
        tuple.question = Pattern.compile(outputLine);

        return this;
    }

    /**
     * Stores an answer for question defined by {@code replyTo} call
     *
     * @param response the answer
     * @return current instance to allow chaining
     * @see ProcessInteractionBuilder#replyTo(String)
     */
    public ProcessInteractionBuilder with(String response) {
        if (tuple.question == null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please prepend replyTo(String) call");
        }
        tuple.answer = Answer.text(response);

        replyMap.put(tuple.question, tuple.answer);
        tuple = new Tuple();

        return this;
    }

    /**
     * Stores an answer for question defined by {@code replyTo} call
     *
     * @param response the answer
     * @return current instance to allow chaining
     * @see ProcessInteractionBuilder#replyTo(String)
     */
    public ProcessInteractionBuilder with(Answer response) {
        if (tuple.question == null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please prepend replyTo(String) call");
        }
        tuple.answer = response;

        replyMap.put(tuple.question, tuple.answer);
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

        Pattern p = Pattern.compile(pattern);
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
        Pattern p = Pattern.compile(pattern);
        errorOutput.add(p);
        return this;
    }

    /**
     * Builds {@link ProcessInteraction} object from defined data
     *
     * @return {@link ProcessInteraction}
     */
    public ProcessInteraction build() {
        if (tuple.question != null) {
            throw new IllegalStateException("Unfinished replyTo().with() sequence, please append with(String) call");
        }

        return new ProcessInteractionImpl(replyMap, allowedOutput, errorOutput);
    }

    private static class Tuple {
        Pattern question;
        Answer answer;
    }

    private static class ProcessInteractionImpl implements ProcessInteraction {

        private final Map<Pattern, Answer> replyMap;

        private final List<Pattern> allowedOutput;

        private final List<Pattern> errorOutput;

        public ProcessInteractionImpl(Map<Pattern, Answer> replyMap, List<Pattern> allowedOutput, List<Pattern> errorOutput) {
            this.replyMap = replyMap;
            this.allowedOutput = allowedOutput;
            this.errorOutput = errorOutput;
        }

        @Override
        public Answer repliesTo(Sentence sentence) {
            for (Map.Entry<Pattern, Answer> entry : replyMap.entrySet()) {
                if (entry.getKey().matcher(sentence).matches()) {
                    return entry.getValue();
                }
            }
            return Answer.none();
        }

        @Override
        public boolean shouldOutput(Sentence sentence) {
            for (Pattern p : allowedOutput) {
                if (p.matcher(sentence).matches()) {
                    return true;
                }
            }
            return false;

        }

        @Override
        public boolean shouldOutputToErr(Sentence sentence) {
            for (Pattern p : errorOutput) {
                if (p.matcher(sentence).matches()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean requiresInputInteraction() {
            return !replyMap.isEmpty();
        }

    }
}
