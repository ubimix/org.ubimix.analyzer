package org.ubimix.analyzer.scores;

/**
 * Instances of this type are used to detect the initial score of text
 * strings.
 * 
 * @author kotelnikov
 */
public interface ITextScoreDetector {

    /**
     * @param text the text for which the initial score should be detected
     * @return the text score
     */
    Channels getTextScore(String text);
}