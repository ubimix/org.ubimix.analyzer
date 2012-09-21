/**
 * 
 */
package org.ubimix.analyzer.scores.impl;

import java.util.Map;

import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.ITagScoreDetector;

/**
 * @author kotelnikov
 */
public abstract class AbstractTagScoreDetector<ITag>
    implements
    ITagScoreDetector<ITag> {

    public interface ITagInfoProvider<ITag> {

        Iterable<Map.Entry<String, String>> getTagAttributes(ITag tag);

        String getTagName(ITag tag);
    }

    protected final IChannelsManager fChannelsManager;

    protected final ITagInfoProvider<ITag> fTagInfoProvider;

    public AbstractTagScoreDetector(
        IChannelsManager channelsManager,
        ITagInfoProvider<ITag> provider) {
        fChannelsManager = channelsManager;
        fTagInfoProvider = provider;
    }

    public IChannelsManager getChannelsManager() {
        return fChannelsManager;
    }

    protected Iterable<Map.Entry<String, String>> getTagAttributes(ITag tag) {
        return fTagInfoProvider.getTagAttributes(tag);
    }

    protected String getTagName(ITag tag) {
        return fTagInfoProvider.getTagName(tag);
    }

}
