/**
 * 
 */
package org.ubimix.analyzer.scores;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kotelnikov
 */
public class ScoreGenerator<T> {

    /**
     * This class contains statistical information about tags.
     * 
     * @author kotelnikov
     */
    public static class TagInfo<T> {

        /**
         * The cumulated weight of this tag. This value equals to the own weight
         * of this tag multiplied by the cumulated weight of the parent.
         */
        private Channels fCumulatedWeight;

        /**
         * The full score of this tag. It is a sum of the own score of this tag
         * and all scores propagated from children to this node.
         */
        private Channels fFullScore;

        /**
         * The own score of this tag. This score is defined mostly by text
         * blocks contained directly in this tag.
         */
        private Channels fOwnScore;

        /**
         * The information about the tag.
         */
        private T fTag;

        /**
         * The absolute linear position of the current text in the DOM.
         */
        private int fTagPos;

        /**
         * Weight of this tag. All text scores in this tag are multiplied by
         * this value.
         */
        private Channels fWeight;

        /**
         * @param tag the tag corresponding to this object
         */
        public TagInfo(T tag) {
            fTag = tag;
        }

        /**
         * Appends a new key/value pair to the specified buffer
         * 
         * @param buf the string buffer to modify
         * @param key the key to append
         * @param value the value to append
         */
        private void append(StringBuilder buf, String key, Object value) {
            if (buf.length() > 0) {
                buf.append(",");
            }
            buf.append("\n  ");
            buf.append("\"");
            buf.append(key);
            buf.append("\":");
            buf.append(value);
        }

        /**
         * Returns the cumulated weight of this node. It is a product of the own
         * weight of this tag and the cumulated weight of the parent node.
         * 
         * @return the cumulated weight of this node
         */
        public Channels getCumulatedWeight() {
            return fCumulatedWeight;
        }

        /**
         * Returns the full score of this node.
         * 
         * @return the full score of this node
         */
        public Channels getFullScore() {
            return fFullScore;
        }

        /**
         * The own score of this tag. This score is defined mostly by text
         * blocks contained directly in this tag.
         * 
         * @return the own score of this tag;
         */
        public Channels getOwnScore() {
            return fOwnScore;
        }

        /**
         * @return the tag associated with this object
         */
        public T getTag() {
            return fTag;
        }

        /**
         * @return the absolute linear position of this tag in the DOM
         */
        public int getTagPos() {
            return fTagPos;
        }

        /**
         * Returns the weight of this tag. More important tags have bigger
         * weight and so they influence more on the final score.
         * 
         * @return the weight of this tag
         */
        public Channels getWeight() {
            return fWeight;
        }

        /**
         * Sets a new cumulated weight of this tag
         * 
         * @param cumulatedWeight a new value to set
         */
        public void setCumulatedWeight(Channels cumulatedWeight) {
            fCumulatedWeight = cumulatedWeight;
        }

        /**
         * Sets a new full score associated with this node. Initially this value
         * is empty (0 on all channels).
         * 
         * @param fullScore the initial full score.
         */
        public void setFullScore(Channels fullScore) {
            fFullScore = fullScore;
        }

        /**
         * Sets the own score of this tag. This value depends on the tag type as
         * well as on text attributes associated with this tag.
         * 
         * @param score the initial own score of this tag.
         */
        public void setOwnScore(Channels score) {
            fOwnScore = score;
        }

        /**
         * Sets a new absolute linear position of this tag in the DOM.
         * 
         * @param pos the absolute linear position of this tag in the DOM
         */
        public void setTagPos(int pos) {
            fTagPos = pos;
        }

        /**
         * Sets a new weight of the tag.
         * 
         * @param weight the weight of the tag
         */
        public void setTagWeight(Channels weight) {
            fWeight = weight;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            append(buf, "tagPos", fTagPos);
            append(buf, "weight", fWeight);
            append(buf, "cumulatedWeight", fCumulatedWeight);
            append(buf, "ownScore", fOwnScore);
            append(buf, "fullScore", fFullScore);
            buf.insert(0, "{");
            buf.append("\n}");
            return buf.toString();
        }

    }

    public static boolean isEmpty(String str) {
        return str == null || "".equals(str.trim());
    }

    private IChannelsManager fChannelsManager;

    private Channels fMaxScore;

    private int fNodeNumber;

    private IScoreReductionProvider fScoreReductionProvider;

    private List<TagInfo<T>> fTagInfoStack = new ArrayList<TagInfo<T>>();

    private ITagScoreDetector<T> fTagScoreDetector;

    private ITagWeightDetector<T> fTagWeightDetector;

    private ITextScoreDetector fTextScoreDetector;

    public ScoreGenerator(
        IChannelsManager channelsManager,
        ITagScoreDetector<T> tagScoreDetector,
        ITagWeightDetector<T> tagWeightDetector,
        ITextScoreDetector textScoreDetector,
        IScoreReductionProvider scoreReductionProvider) {
        fChannelsManager = channelsManager;
        fTagScoreDetector = tagScoreDetector;
        fTagWeightDetector = tagWeightDetector;
        fTextScoreDetector = textScoreDetector;
        fScoreReductionProvider = scoreReductionProvider;
        fMaxScore = fChannelsManager.newChannels(0);
        fNodeNumber = 0;
    }

    public TagInfo<T> beginTag(T tag) {
        TagInfo<T> tagInfo = newTagInfo(tag);

        Channels score = fTagScoreDetector.getTagScore(tag);
        tagInfo.setOwnScore(score);

        Channels tagWeight = fTagWeightDetector.getTagWeight(tag);
        tagInfo.setTagWeight(tagWeight);

        Channels cumulatedWeight = tagWeight.getCopy();
        TagInfo<T> parentInfo = peek();
        if (parentInfo != null) {
            Channels parentCumulatedWeight = parentInfo.getCumulatedWeight();
            cumulatedWeight.multiply(parentCumulatedWeight);
        }
        tagInfo.setCumulatedWeight(cumulatedWeight);

        Channels fullScore = fChannelsManager.newChannels(0);
        tagInfo.setFullScore(fullScore);

        tagInfo.setTagPos(fNodeNumber++);
        push(tagInfo);

        return tagInfo;
    }

    public TagInfo<T> endTag() {
        TagInfo<T> tagScore = peek();
        Channels delta = tagScore.getOwnScore();
        delta = delta.getCopy();

        Channels cumulatedWeight = tagScore.getCumulatedWeight();
        delta.multiply(cumulatedWeight);

        int distance = 0;
        for (int i = fTagInfoStack.size() - 1; i >= 0; i--) {
            TagInfo<T> container = fTagInfoStack.get(i);
            Channels distanceReduction = fScoreReductionProvider
                .getScoreReduction(distance);
            if (distanceReduction == null) {
                break;
            }
            delta.multiply(distanceReduction);
            Channels score = container.getFullScore();
            score.add(delta);
            if (delta.checkValuesInRange(0, 1)) {
                break;
            }
            distance++;
        }

        Channels channels = tagScore.getFullScore();
        fMaxScore.max(channels);

        pop();
        return tagScore;
    }

    public Channels getAverageScore() {
        Channels averageScore = fMaxScore.getCopy();
        averageScore.div(fNodeNumber);
        return averageScore;
    }

    public IChannelsManager getChannelManager() {
        return fChannelsManager;
    }

    public Channels getMaxScore() {
        return fMaxScore;
    }

    protected TagInfo<T> newTagInfo(T tag) {
        return new TagInfo<T>(tag);
    }

    public void onText(String content) {
        Channels weight = fTextScoreDetector.getTextScore(content);
        TagInfo<T> tagInfo = peek();
        Channels score = tagInfo.getOwnScore();
        score.add(weight);
    }

    private TagInfo<T> peek() {
        return !fTagInfoStack.isEmpty() ? fTagInfoStack.get(fTagInfoStack
            .size() - 1) : null;
    }

    private TagInfo<T> pop() {
        TagInfo<T> result = null;
        if (!fTagInfoStack.isEmpty()) {
            result = fTagInfoStack.remove(fTagInfoStack.size() - 1);
        }
        return result;
    }

    private void push(TagInfo<T> info) {
        fTagInfoStack.add(info);
    }
}
