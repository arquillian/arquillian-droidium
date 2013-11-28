package org.arquillian.droidium.container.impl;

/**
 * Represents a process interaction that is handled in non-interactive manner
 *
 * @author <a href="kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface ProcessInteraction {

    /**
     * Returns a string that should be used to reply the question
     *
     * @param outputLine the question
     * @return String to be used as answer, {@code null} otherwise
     */
    String repliesTo(String outputLine);

    /**
     * Checks if the current line should be propagate to standard output
     *
     * @param outputLine current line
     * @return {@code true} if output is to be printed out
     */
    boolean shouldOutput(String outputLine);

    /**
     * Checks if the current line should be propagate to standard error output
     *
     * @param outputLine current line
     * @return {@code true} if output is to be printed out to error output
     */
    boolean shouldOutputToErr(String outputLine);
}
