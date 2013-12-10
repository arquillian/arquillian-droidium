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
package org.arquillian.droidium.container.execution;

/**
 * Represents a process interaction that is handled in non-interactive manner
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface ProcessInteraction {

    /**
     * Checks whether process requires any interaction on its input
     *
     * @return {@code true} if so, {@code false} otherwise
     */
    boolean requiresInputInteraction();

    /**
     * Returns a string that should be used to reply the question
     *
     * @param sentence the question
     * @return Answer to be used to response to sentence
     */
    Answer repliesTo(Sentence sentence);

    /**
     * Checks if the current line should be propagate to standard output
     *
     * @param sentence current line
     * @return {@code true} if output is to be printed out
     */
    boolean shouldOutput(Sentence sentence);

    /**
     * Checks if the current line should be propagate to standard error output
     *
     * @param sentence current line
     * @return {@code true} if output is to be printed out to error output
     */
    boolean shouldOutputToErr(Sentence sentence);

}
