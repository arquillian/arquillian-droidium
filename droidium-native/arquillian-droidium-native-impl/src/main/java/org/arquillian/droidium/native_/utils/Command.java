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
package org.arquillian.droidium.native_.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents emulator command we are creating in order to execute some command on the command line.
 *
 * @author <a href="smikloso@redhat.com">Stefan Miklosovic</a>
 *
 */
public class Command {

    private List<String> command;

    public Command() {
        command = new ArrayList<String>();
    }

    /**
     *
     * @param command
     * @throws IllegalArgumentException if {@code command} is null
     */
    public Command(List<String> command) throws IllegalArgumentException {
        if (command == null) {
            throw new IllegalArgumentException("command can't be a null list!");
        }
        this.command = command;
    }

    /**
     * Adds a token to the command list.
     *
     * @param token token to add to the command list
     * @return instance of this {@code Command}
     */
    public Command add(String token) {
        if (token != null && !token.trim().equals("")) {
            command.add(token.trim());
        }
        return this;
    }

    /**
     * Add list of tokens to already existing list we are constructing, ignoring null and empty ones.
     *
     * @param tokens tokens we are adding to the already existing list
     * @return instance of this {@code Command}
     */
    public Command add(List<String> tokens) {
        for (String token : tokens) {
            add(token);
        }
        return this;
    }

    /**
     * Adds tokens written in the simple string, parsing tokens when delimiter is a space.
     *
     * @param tokens tokens to add, saved in the string and delimited by space(s)
     *
     * @return instance of this {@code Command}
     */
    public Command addAsString(String tokens) {
        if (tokens == null || tokens.trim().equals("")) {
            return this;
        }

        tokens = deleteTrailingSpaces(tokens);

        // we tokenize string so every word delimited by spaces will be one token
        List<String> tokenized = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(deleteTrailingSpaces(tokens), " ");
        while (tokenizer.hasMoreTokens()) {
            tokenized.add(tokenizer.nextToken().trim());
        }

        int quotesOnEnds = 0;
        int quotesTotal = 0;

        // we count number of quotes on the beginning and end of the every word
        for (int i = 0; i < tokenized.size(); i++) {
            if (tokenized.get(i).startsWith("\"")) {
                quotesOnEnds++;
            }
            if (tokenized.get(i).endsWith("\"")) {
                quotesOnEnds++;
            }
            // and we count number of quotes in the word as such
            for (int j = 0; j < tokenized.get(i).length(); j++) {
                if (tokenized.get(i).charAt(j) == '\"') {
                    quotesTotal++;
                }
            }
        }

        // if these two do not equal, it means there is quote somewhere in the
        // middle of a word which is not acceptable input
        if (quotesOnEnds != quotesTotal) {
            return this;
        }

        // if it is not divisible by two, it means there are unbalanced quotes
        // which is not acceptable input
        if (quotesOnEnds % 2 != 0) {
            return this;
        }

        // if some token starts and ends with quotes, add it to the list
        // otherwise take the next one until we have a word which ends with quotes
        for (int i = 0; i < tokenized.size(); i++) {
            if (tokenized.get(i).startsWith("\"")) {
                if (tokenized.get(i).endsWith("\"")) {
                    command.add(tokenized.get(i));
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(tokenized.get(i));
                    int j = i;
                    while (true) {
                        if (tokenized.get(++j).endsWith("\"")) {
                            sb.append(" ").append(tokenized.get(j));
                            break;
                        }
                        else {
                            sb.append(" ").append(tokenized.get(j)).append(" ");
                        }
                    }
                    command.add(sb.toString());
                    i++;
                }
            } else {
                command.add(tokenized.get(i));
            }
        }

        return this;
    }

    /**
     * Deletes all multiple spaces and preserve original sense of the text input.
     *
     * @param text
     * @return {@code text} without multiple spaces
     */
    private String deleteTrailingSpaces(String text) {
        if (text == null) {
            return null;
        }

        return text.replaceAll("^ +| +$|( )+", "$1")
            .replaceAll("\" ([^\"])", "\"$1")
            .replaceAll("\"([^ \"]) \"", "\"$1\"");
    }

    /**
     * Remove all occurences of {@code token} from the command list.
     *
     * @param token token to remove
     * @return instance of this {@code EmulatorCommand}
     */
    public Command remove(String token) {
        if (token == null || token.trim().equals("")) {
            return this;
        }

        while (command.remove(token)) {
        }

        return this;
    }

    /**
     * Clears the emulator command list.
     *
     * @return instance of this {@code EmulatorCommand}
     */
    public Command clear() {
        command.clear();
        return this;
    }

    /**
     * Return size of the command.
     *
     * @return number of tokens stored in the command
     */
    public int size() {
        return command.size();
    }

    /**
     *
     * @return command we constructed
     */
    public List<String> getAsList() {
        return command;
    }

    public String[] getAsArray() {
        return getAsList().toArray(new String[0]);
    }

    public String getAsString() {
        StringBuilder sb = new StringBuilder();
        for (String s : command) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * Returns token on i-th position
     *
     * @param i position of token we want to get
     * @return token on i-th position, null if we are out of bounds
     */
    public String get(int i) {
        try {
            return command.get(i);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     *
     * @return last token from the command
     */
    public String getLast() {
        if (command.isEmpty()) {
            return null;
        }
        return command.get(command.size() - 1);
    }

    /**
     *
     * @return first token from the command
     */
    public String getFirst() {
        if (command.isEmpty()) {
            return null;
        }
        return command.get(0);
    }

    @Override
    public String toString() {
        return getAsString();
    }
}
