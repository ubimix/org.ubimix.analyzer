/**
 * 
 */
package org.ubimix.analyzer.scores.impl;

import java.util.HashMap;
import java.util.Map;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.ITextScoreDetector;

/**
 * @author kotelnikov
 */
public class SimpleTagScoreDetector<T> extends AbstractTagScoreDetector<T> {

    private Map<String, Channels> fTagScores = new HashMap<String, Channels>();

    private Map<String, ITextScoreDetector> fTextScoreDetectors = new HashMap<String, ITextScoreDetector>();

    /**
     * @param channelsManager
     */
    public SimpleTagScoreDetector(
        ITagInfoProvider<T> provider,
        IChannelsManager channelsManager) {
        super(channelsManager, provider);
    }

    public void addTagScore(Channels weight, String... tags) {
        for (String tag : tags) {
            fTagScores.put(tag, weight);
        }
    }

    public void addTextScore(
        ITextScoreDetector textScoreDetector,
        String... attrNames) {
        for (String attr : attrNames) {
            fTextScoreDetectors.put(attr, textScoreDetector);
        }
    }

    /**
     * @see org.ubimix.analyzer.scores.ITagScoreDetector#getTagScore(T)
     */
    @Override
    public Channels getTagScore(T tag) {
        String tagName = getTagName(tag);
        Channels score = fTagScores.get(tagName);
        if (score == null) {
            score = fChannelsManager.newChannels(0);
        }
        Iterable<Map.Entry<String, String>> attributes = getTagAttributes(tag);
        for (Map.Entry<String, String> entry : attributes) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (attrValue != null && !"".equals(attrValue)) {
                ITextScoreDetector detector = fTextScoreDetectors.get(attrName);
                if (detector != null) {
                    Channels value = detector.getTextScore(attrValue);
                    if (value != null) {
                        score.add(value);
                    }
                }
            }
        }
        return score;
    }

}
