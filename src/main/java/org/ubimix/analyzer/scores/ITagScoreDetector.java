package org.ubimix.analyzer.scores;

/**
 * Instances of this type are used to detect initial tag scores and tag weights.
 * By default for tags with empty parameters the initial score equals to 0 for
 * all channels. But some tags can have not-empty initial scores by some
 * channels. For example the <code>"img"</code> tag could have quite big initial
 * score in the <code>"media"</code> channel. Values of some attributes could
 * also contribute to the initial score. For example the <code>"alt"</code>
 * attribute of an image could change the initial score of this tag. Some
 * channels can depend on attribute values as well. For example the
 * <code>"navigation"</code> channel could have very big score for the attribute
 * like <code>class='menu'</code>
 * 
 * @author kotelnikov
 */
public interface ITagScoreDetector<T> {

    /**
     * Returns the initial score of an empty tag of the specified tag with the
     * given attributes.
     * 
     * @param tag the tag to analyse
     * @return the initial weight of the specified tag
     */
    Channels getTagScore(T tag);

}