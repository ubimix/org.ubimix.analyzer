/**
 * 
 */
package org.ubimix.analyzer.scores.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.ITagWeightDetector;
import org.ubimix.analyzer.scores.impl.AbstractTagScoreDetector.ITagInfoProvider;

/**
 * @author kotelnikov
 */
public class SimpleTagWeightDetector<T> implements ITagWeightDetector<T> {

    private static class AttrValueWeights {

        private Map<String, Channels> fMap = new LinkedHashMap<String, Channels>();

        public AttrValueWeights() {
        }

        public void add(String value, Channels weight) {
            fMap.put(value, weight);
        }

        public Channels getWeight(String attrValue) {
            Channels result = null;
            for (Map.Entry<String, Channels> entry : fMap.entrySet()) {
                String value = entry.getKey();
                if ("".equals(value) || attrValue.indexOf(value) > 0) {
                    result = entry.getValue();
                    break;
                }
            }
            return result;
        }
    }

    private Map<String, AttrValueWeights> fAttrWeights = new HashMap<String, AttrValueWeights>();

    private IChannelsManager fChannelsManager;

    private ITagInfoProvider<T> fTagInfoProvider;

    private Map<String, Channels> fTagWeights = new HashMap<String, Channels>();

    /**
     * @param channelsManager
     */
    public SimpleTagWeightDetector(
        ITagInfoProvider<T> provider,
        IChannelsManager channelsManager) {
        fChannelsManager = channelsManager;
        fTagInfoProvider = provider;
    }

    public void addAttrWeight(
        String attr,
        Channels weight,
        String... attrValues) {
        AttrValueWeights w = fAttrWeights.get(attr);
        if (w == null) {
            w = new AttrValueWeights();
            fAttrWeights.put(attr, w);
        }
        for (String value : attrValues) {
            w.add(value, weight);
        }
    }

    public void addAttrWeights(
        String[] attrs,
        Channels weight,
        String... attrValues) {
        for (String attr : attrs) {
            addAttrWeight(attr, weight, attrValues);
        }
    }

    public void addTagWeight(Channels weight, String... tags) {
        for (String tag : tags) {
            fTagWeights.put(tag, weight);
        }
    }

    /**
     * @see org.ubimix.analyzer.scores.ITagWeightDetector#getTagWeight(java.lang.Object)
     */
    @Override
    public Channels getTagWeight(T tag) {
        String tagName = fTagInfoProvider.getTagName(tag);
        Iterable<Map.Entry<String, String>> attributes = fTagInfoProvider
            .getTagAttributes(tag);
        Channels weight = fTagWeights.get(tagName);
        if (weight == null) {
            weight = fChannelsManager.newChannels(1);
        }
        for (Map.Entry<String, String> entry : attributes) {
            String attrName = entry.getKey();
            AttrValueWeights w = fAttrWeights.get(attrName);
            if (w != null) {
                String attrValue = entry.getValue();
                Channels attrWeight = w.getWeight(attrValue);
                if (attrWeight != null) {
                    weight.multiply(attrWeight);
                }
            }
        }
        return weight;
    }

}
