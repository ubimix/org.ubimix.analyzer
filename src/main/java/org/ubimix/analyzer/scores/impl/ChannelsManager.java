package org.ubimix.analyzer.scores.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;

/**
 * @author kotelnikov
 */
public class ChannelsManager implements IChannelsManager {

    private String[] fChannelNames;

    private LinkedHashMap<String, Integer> fChannels = new LinkedHashMap<String, Integer>();

    public ChannelsManager(Collection<String> channelNames) {
        int counter = 0;
        for (String name : channelNames) {
            if (name == null || "".equals(name)) {
                continue;
            }
            if (!fChannels.containsKey(name)) {
                fChannels.put(name, counter++);
            }
        }
        fChannelNames = fChannels
            .keySet()
            .toArray(new String[fChannels.size()]);
    }

    public ChannelsManager(String... channelNames) {
        this(Arrays.asList(channelNames));
    }

    @Override
    public int getChannelId(String channelName) {
        return fChannels.get(channelName);
    }

    @Override
    public String getChannelName(int channelId) {
        return fChannelNames[channelId];
    }

    @Override
    public int getChannelsNumber() {
        return fChannels.size();
    }

    public Channels newChannels() {
        return newChannels(1);
    }

    @Override
    public Channels newChannels(double value) {
        return new Channels(this, value);
    }
}