package org.ubimix.analyzer.scores.impl;

import java.util.HashSet;
import java.util.Set;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.ITextScoreDetector;

public class TextScoreDetector implements ITextScoreDetector {

    private IChannelsManager fChannelsManager;

    private double fLowerCaseWeight = 1.0;

    private double fSpaceWeight = 0.8;

    private double fSymbolWeight = 0.9;

    private Set<String> fTextAttributes = new HashSet<String>();

    private double fUpperCaseWeight = 1.1;

    public TextScoreDetector(IChannelsManager channelsManager) {
        fChannelsManager = channelsManager;
    }

    public void addTextAttributes(String... attributes) {
        for (String attr : attributes) {
            fTextAttributes.add(attr);
        }
    }

    public Channels getAttributeWeight(
        String tagName,
        String attrName,
        String attrValue) {
        if (!fTextAttributes.contains(attrName)) {
            return null;
        }
        return getTextChannels(attrValue, true);
    }

    public IChannelsManager getChannelsManager() {
        return fChannelsManager;
    }

    public double getLowerCaseWeight() {
        return fLowerCaseWeight;
    }

    public double getSpaceWeight() {
        return fSpaceWeight;
    }

    public double getSymbolWeight() {
        return fSymbolWeight;
    }

    public Set<String> getTextAttributes() {
        return fTextAttributes;
    }

    protected Channels getTextChannels(String text, boolean reduceSpaces) {
        double weight = getTextWeight(text, reduceSpaces);
        Channels channels = fChannelsManager.newChannels(weight);
        return channels;
    }

    public Channels getTextScore(String text) {
        return getTextChannels(text, true);
    }

    protected double getTextWeight(String value, boolean reduceSpaces) {
        char[] array = value.toCharArray();
        int startPos = 0;
        int lastPos = array.length - 1;

        double weight = 0;
        boolean prevSpace = false;
        for (int i = startPos; i <= lastPos; i++) {
            char ch = array[i];
            if (Character.isSpaceChar(ch)
                || ch == '\n'
                || ch == '\r'
                || ch == '\t') {
                if (!reduceSpaces || !prevSpace) {
                    weight += fSpaceWeight;
                }
                prevSpace = true;
            } else {
                if (Character.isLetterOrDigit(ch)) {
                    if (Character.isUpperCase(ch)) {
                        weight += fUpperCaseWeight;
                    } else {
                        weight += fLowerCaseWeight;
                    }
                } else {
                    weight += fSymbolWeight;
                }
                prevSpace = false;
            }
        }
        return weight;
    }

    public double getUpperCaseWeight() {
        return fUpperCaseWeight;
    }

    public TextScoreDetector setChannelsManager(IChannelsManager channelsManager) {
        fChannelsManager = channelsManager;
        return this;
    }

    public TextScoreDetector setLowerCaseWeight(double lowerCaseWeight) {
        fLowerCaseWeight = lowerCaseWeight;
        return this;
    }

    public TextScoreDetector setSpaceWeight(double spaceWeight) {
        fSpaceWeight = spaceWeight;
        return this;
    }

    public TextScoreDetector setSymbolWeight(double symbolWeight) {
        fSymbolWeight = symbolWeight;
        return this;
    }

    public TextScoreDetector setTextAttributes(Set<String> textAttributes) {
        fTextAttributes = textAttributes;
        return this;
    }

    public TextScoreDetector setUpperCaseWeight(double upperCaseWeight) {
        fUpperCaseWeight = upperCaseWeight;
        return this;
    }
}