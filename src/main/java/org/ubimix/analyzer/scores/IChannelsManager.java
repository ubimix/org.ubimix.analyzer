package org.ubimix.analyzer.scores;

/**
 * @author kotelnikov
 */
public interface IChannelsManager {

    int getChannelId(String channelName);

    String getChannelName(int channelId);

    int getChannelsNumber();

    Channels newChannels(double weight);

}
