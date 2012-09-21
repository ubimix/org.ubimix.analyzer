package org.ubimix.analyzer.scores;

/**
 * This class calculates the score reduction factor when the text score is
 * propagated from the original tag to parents.
 * 
 * @author kotelnikov
 */
public interface IScoreReductionProvider {
    /**
     * Calculates and returns the factor of score reduction when the text score
     * is propagated from the original tag to tag parents.
     * 
     * @param distance the distance from the original tag; 0 is the tag itself;
     *        1 - corresponds to its direct parent etc.
     * @return the reduction factor
     */
    Channels getScoreReduction(int distance);
}