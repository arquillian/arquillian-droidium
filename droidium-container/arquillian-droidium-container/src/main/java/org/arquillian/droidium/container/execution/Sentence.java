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
 * Represents a line on standard output or standard error output
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class Sentence implements CharSequence {
    private final StringBuilder sb;

    /**
     * Creates a new sentence
     */
    public Sentence() {
        this.sb = new StringBuilder();
    }

    /**
     * Appends a character sequence to sentence
     *
     * @param s sequence
     * @return updated sentence
     */
    public Sentence append(CharSequence s) {
        sb.append(s);
        return this;
    }

    /**
     * Appends a character to sentence
     *
     * @param c character
     * @return updated sequence
     */
    public Sentence append(char c) {
        sb.append(c);
        return this;
    }

    /**
     * Checks whether sentence is finished by newline character(s)
     *
     * @return
     */
    // adb command writes its output with ends of lines as "\\n"
    // ignoring Windows conventions which recognize "\r\n" as the end of the line
    public boolean isFinished() {
        int count = sb.length();
        return (count > 0 && (sb.charAt(count - 1) == '\r' || sb.charAt(count - 1) == '\n'));
    }

    /**
     * Checks whether sentence is empty, that is does not contain any characters
     *
     * @return true if sentence is empty, false otherwise
     */
    public boolean isEmpty() {
        return sb.length() == 0;
    }

    /**
     * Removes a newline character(s) from the end of sentence, if any
     *
     * @return updated sentence
     */
    // adb command writes its output with ends of lines as "\\n"
    // ignoring Windows conventions which recognize "\r\n" as the end of the line
    public Sentence trim() {

        // ignore end whitespace
        int count = sb.length();
        while (count > 0 && (sb.charAt(count - 1) == '\r' || sb.charAt(count - 1) == '\n')) {
            sb.setLength(--count);
        }
        return this;
    }

    /**
     * Clears the sentence
     *
     * @return
     */
    public Sentence reset() {
        sb.setLength(0);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

    @Override
    public int length() {
        return sb.length();
    }

    @Override
    public char charAt(int index) {
        return sb.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return sb.subSequence(start, end);
    }

}