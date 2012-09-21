/**
 * 
 */
package org.ubimix.analyzer.scores.impl;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.IScoreReductionProvider;
import org.ubimix.analyzer.scores.ITagScoreDetector;
import org.ubimix.analyzer.scores.ITagWeightDetector;
import org.ubimix.analyzer.scores.ITextScoreDetector;
import org.ubimix.analyzer.scores.ScoreGenerator;
import org.ubimix.analyzer.scores.ScoreGenerator.TagInfo;
import org.ubimix.analyzer.scores.impl.AbstractTagScoreDetector.ITagInfoProvider;

/**
 * @author kotelnikov
 */
public class ScoreGeneratorFactory<T> {

    private static <V> V[] array(V... values) {
        return values;
    }

    private IChannelsManager fChannelsManager;

    private IScoreReductionProvider fScoreReductionProvider;

    private ITagInfoProvider<T> fTagInfoProvider;

    private ITagScoreDetector<T> fTagScoreDetector;

    private ITagWeightDetector<T> fTagWeightDetector;

    private ITextScoreDetector fTextScoreDetector;

    /**
     *  
     */
    public ScoreGeneratorFactory(ITagInfoProvider<T> tagInfoProvider) {
        fTagInfoProvider = tagInfoProvider;
        fChannelsManager = newChannelsManager();
        fScoreReductionProvider = newDistanceReductionProvider();
        fTextScoreDetector = newTextScoreDetector();
        fTagScoreDetector = newTagScoreDetector();
        fTagWeightDetector = newTagWeightDetector();
    }

    protected IChannelsManager newChannelsManager() {
        return new ChannelsManager("content", "navigation", "media");
    }

    protected IScoreReductionProvider newDistanceReductionProvider() {
        return new ScoreReductionProvider(fChannelsManager);
    }

    public ScoreGenerator<T> newScoreGenerator() {
        return new ScoreGenerator<T>(
            fChannelsManager,
            fTagScoreDetector,
            fTagWeightDetector,
            fTextScoreDetector,
            fScoreReductionProvider) {
            @Override
            protected TagInfo<T> newTagInfo(T tag) {
                return ScoreGeneratorFactory.this.newTagInfo(tag);
            }
        };
    }

    protected TagInfo<T> newTagInfo(T tag) {
        return new TagInfo<T>(tag);
    }

    protected ITagScoreDetector<T> newTagScoreDetector() {
        SimpleTagScoreDetector<T> detector = new SimpleTagScoreDetector<T>(
            fTagInfoProvider,
            fChannelsManager);
        // Text attributes
        detector.addTextScore(fTextScoreDetector, "alt", "title");
        return detector;
    }

    protected ITagWeightDetector<T> newTagWeightDetector() {
        SimpleTagWeightDetector<T> weightDetector = new SimpleTagWeightDetector<T>(
            fTagInfoProvider,
            fChannelsManager);
        weightDetector.addTagWeight(
            s(0.5).setLevel(fChannelsManager.getChannelId("navigation"), 10.0),
            "a");
        weightDetector.addTagWeight(
            s(1).setLevel(fChannelsManager.getChannelId("content"), 1.1),
            "p");
        weightDetector.addTagWeight(s(0.5), "ul", "ol", "dl", "li", "dd", "dt");
        weightDetector.addTagWeight(s(2.5), "h1", "h2");
        weightDetector.addTagWeight(s(2.0), "h3", "h4");
        weightDetector.addTagWeight(s(1.5), "h5", "h6");
        weightDetector.addTagWeight(
            s(0),
            "button",
            "head",
            "iframe",
            "input",
            "noscript",
            "option",
            "script",
            "select",
            "style",
            "textarea");

        // Weights for individual tag attribute values
        weightDetector.addAttrWeights(array("id", "class"), s(0.5), "comment");
        weightDetector.addAttrWeights(
            array("id", "class"),
            s(1.1),
            "content",
            "body");
        return weightDetector;
    }

    protected ITextScoreDetector newTextScoreDetector() {
        return new TextScoreDetector(fChannelsManager);
    }

    private Channels s(double w) {
        return fChannelsManager.newChannels(w);
    }

}
