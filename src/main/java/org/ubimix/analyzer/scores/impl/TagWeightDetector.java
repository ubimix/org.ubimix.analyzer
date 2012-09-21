package org.ubimix.analyzer.scores.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.ITextScoreDetector;

/**
 * @author kotelnikov
 */
public class TagWeightDetector<T> extends AbstractTagScoreDetector<T> {

    public static interface IMatcher {
        boolean match(String value);
    }

    public static class RegexMatcher extends TagWeightDetector.TextMatcher {

        public RegexMatcher(String regex) {
            super(regex);
        }

        @Override
        public boolean match(String value) {
            if (super.match(value)) {
                return true;
            }
            return value.matches(fValue);
        }
    }

    public static class TextMatcher implements TagWeightDetector.IMatcher {

        protected String fValue;

        public TextMatcher(String value) {
            if (value == null) {
                value = "";
            }
            fValue = value;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TagWeightDetector.TextMatcher)) {
                return false;
            }
            TagWeightDetector.TextMatcher o = (TagWeightDetector.TextMatcher) obj;
            return fValue.equals(o.fValue);
        }

        @Override
        public int hashCode() {
            return fValue.hashCode();
        }

        @Override
        public boolean match(String value) {
            if (value == null) {
                return false;
            }
            return fValue.equals(value);
        }

        @Override
        public String toString() {
            return fValue;
        }

    }

    private Map<TagWeightDetector.IMatcher, Channels> fTagScores = new LinkedHashMap<TagWeightDetector.IMatcher, Channels>();

    private Map<TagWeightDetector.IMatcher, ITextScoreDetector> fTextScores = new LinkedHashMap<TagWeightDetector.IMatcher, ITextScoreDetector>();

    public TagWeightDetector(
        ITagInfoProvider<T> provider,
        IChannelsManager channelsManager) {
        super(channelsManager, provider);
    }

    public TagWeightDetector<T> addTagScore(
        TagWeightDetector.IMatcher tagMatcher,
        Channels tagScore) {
        fTagScores.put(tagMatcher, tagScore);
        return this;
    }

    public TagWeightDetector<T> addTextScore(
        TagWeightDetector.IMatcher attrMatcher,
        ITextScoreDetector textScoretDetector) {
        fTextScores.put(attrMatcher, textScoretDetector);
        return this;
    }

    private <N> N get(Map<TagWeightDetector.IMatcher, N> map, String key) {
        TagWeightDetector.IMatcher matcher = new TextMatcher(key);
        N result = map.get(matcher);
        if (result == null) {
            for (Map.Entry<TagWeightDetector.IMatcher, N> entry : map
                .entrySet()) {
                matcher = entry.getKey();
                if (matcher.match(key)) {
                    result = entry.getValue();
                    break;
                }
            }
        }
        return result;
    }

    /**
     * @see org.ubimix.analyzer.scores.ITagScoreDetector#getTagScore(T)
     */
    @Override
    public Channels getTagScore(T tag) {
        String tagName = getTagName(tag);
        Channels weight = get(fTagScores, tagName);
        if (weight == null) {
            weight = fChannelsManager.newChannels(0);
        }
        Iterable<Map.Entry<String, String>> attributes = getTagAttributes(tag);
        for (Map.Entry<String, String> entry : attributes) {
            String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (attrValue != null && !"".equals(attrValue)) {
                ITextScoreDetector detector = get(fTextScores, attrName);
                if (detector != null) {
                    Channels value = detector.getTextScore(attrValue);
                    if (value != null) {
                        weight.add(value);
                    }
                }
            }
        }
        return weight;
    }

}