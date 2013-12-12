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
 * Represents an non-interactive user input to a sentence. Answer automatically appends new line character
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class Answer implements CharSequence {

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Type of answer - this modifies behavior
     *
     * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
     *
     */
    public static enum AnswerType {
        /**
         * No answer
         */
        NONE,
        /**
         * Text input, as it would be written by user
         */
        TEXT,
        /**
         * Action represeting the last input action, flushing and closing input stream
         */
        EOF
    };

    private final AnswerType type;

    private final String answerText;

    private static final Answer EMPTY_ANSWER = new Answer(AnswerType.NONE, "");
    private static final Answer EOF = new Answer(AnswerType.EOF, "");

    public static Answer none() {
        return EMPTY_ANSWER;
    }

    public static Answer eof() {
        return EOF;
    }

    /**
     * Creates a line answer. Automatically adds Platform dependent line separator
     *
     * @param text
     * @return answer
     */
    public static Answer text(String text) {
        return new Answer(AnswerType.TEXT, text + LINE_SEPARATOR);
    }

    private Answer(AnswerType type, String answerText) {
        this.type = type;
        this.answerText = answerText;
    }

    @Override
    public int length() {
        return answerText.length();
    }

    @Override
    public char charAt(int index) {
        return answerText.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return answerText.subSequence(start, end);
    }

    public AnswerType getType() {
        return type;
    }

    public String getAnswerText() {
        return answerText;
    }

    public byte[] getBytes() {
        return answerText.getBytes();
    }

    @Override
    public String toString() {
        switch (type) {
            case NONE:
                return "()";
            case TEXT:
                return answerText;
            case EOF:
                return "(-EOF-)" + LINE_SEPARATOR;
            default:
                throw new UnsupportedOperationException("Answer type " + type + " is not supported");
        }
    }
}
