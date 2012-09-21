package org.ubimix.analyzer.scores;

/**
 * Instances of this type are used to detect tag weights.
 * 
 * @author kotelnikov
 */
public interface ITagWeightDetector<T> {

    /**
     * Detects and returns the multiplication factor for all tag scores in tags
     * with the specified name and attributes
     * 
     * @param tag tag to analyse
     * @return a multiplication factor for all content of this tag
     */
    Channels getTagWeight(T tag);

}