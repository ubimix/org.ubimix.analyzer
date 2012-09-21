package org.ubimix.analyzer.scores;

/**
 * This object contains re-distribution of weights by channels and provides some
 * common operations used to manipulate with these values.
 * 
 * @author kotelnikov
 */
public class Channels {

    /**
     * Redistribution of weights by channels.
     */
    private final double[] fArray;

    /**
     * The channel manager created this object.
     */
    private final IChannelsManager fManager;

    /**
     * A copy constructor
     * 
     * @param channels
     */
    protected Channels(Channels channels) {
        fManager = channels.fManager;
        fArray = new double[channels.fArray.length];
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] = channels.fArray[i];
        }
    }

    /**
     * This constructor initializes internal fields..
     * 
     * @param manager the channel manager owning this object
     * @param value the initial value
     */
    public Channels(IChannelsManager manager, double value) {
        fManager = manager;
        int size = manager.getChannelsNumber();
        fArray = new double[size];
        if (value != 0) {
            for (int i = 0; i < fArray.length; i++) {
                fArray[i] = value;
            }
        }
    }

    /**
     * Adds the given value to the internal channel array.
     * 
     * @param value the source of values
     */
    public Channels add(Channels value) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] += value.fArray[i];
        }
        return this;
    }

    /**
     * Adds the given value to all filter channels.
     * 
     * @param value the source of values
     */
    public Channels add(double value) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] += value;
        }
        return this;
    }

    /**
     * Multiplies the given value to all filter channels and adds the resulting
     * values to the internal channel array.
     * 
     * @param value the source of values
     * @param filter
     */
    public Channels add(double value, Channels filter) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] += value * filter.fArray[i];
        }
        return this;
    }

    public Channels add(int channelId, double level) {
        fArray[channelId] += level;
        return this;
    }

    protected void appendChannel(StringBuilder buf, int channelId) {
        String name = fManager.getChannelName(channelId);
        buf.append(name);
        buf.append("=");
        buf.append(fArray[channelId]);
    }

    /**
     * This method returns <code>true</code> if values in all channels are in
     * the specified range.
     * 
     * @param min minimal range value
     * @param max maximal range value
     * @return
     */
    public boolean checkValuesInRange(double min, double max) {
        boolean result = true;
        for (int i = 0; result && i < fArray.length; i++) {
            result = min <= fArray[i] && max >= fArray[i];
        }
        return result;
    }

    /**
     * Returns the result of comparison of values on the specified channel.
     * 
     * @param levels the object to compare with
     * @param channelId the ID of the channel
     * @return the result of comparison of values on the specified channel.
     */
    public int compareTo(Channels levels, int channelId) {
        double diff = fArray[channelId] - levels.fArray[channelId];
        return diff > 0 ? 1 : diff < 0 ? -1 : 0;
    }

    /**
     * Returns the result of comparison of values on the specified channel.
     * 
     * @param levels the object to compare with
     * @param channelName the name of the channel
     * @return the result of comparison of values on the specified channel
     */
    public int compareTo(Channels levels, String channelName) {
        int channelId = fManager.getChannelId(channelName);
        return compareTo(levels, channelId);
    }

    public int countChannelNumberLessThan(Channels channels) {
        int result = 0;
        for (int i = 0; i < fArray.length; i++) {
            if (fArray[i] < channels.fArray[i]) {
                result++;
            }
        }
        return result;
    }

    public int countChannelNumberMoreThan(Channels channels) {
        int result = 0;
        for (int i = 0; i < fArray.length; i++) {
            if (fArray[i] > channels.fArray[i]) {
                result++;
            }
        }
        return result;
    }

    public Channels div(Channels levels) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] /= levels.fArray[i];
        }
        return this;
    }

    /**
     * Divide all channels by the specified value
     * 
     * @param value
     */
    public Channels div(double value) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] /= value;
        }
        return this;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Channels)) {
            return false;
        }
        Channels o = (Channels) obj;
        boolean result = fArray.length == o.fArray.length;
        for (int i = 0; result && i < fArray.length; i++) {
            result = fArray[i] == o.fArray[i];
        }
        return result;
    }

    /**
     * @return the channels manager
     */
    public IChannelsManager getChannelManager() {
        return fManager;
    }

    public int getChannelsNumber() {
        return fArray.length;
    }

    /**
     * @return
     */
    public Channels getCopy() {
        return new Channels(this);
    }

    /**
     * Returns the weight of the specified channel
     * 
     * @param channelId the identifier of the channel to return
     * @return the value of the specified channel
     */
    public double getLevel(int channelId) {
        return fArray[channelId];
    }

    /**
     * Returns the weight of the specified channel.
     * 
     * @param channelName the name of the channel to return
     * @return the value of the specified channel
     */
    public double getLevel(String channelName) {
        int channelId = fManager.getChannelId(channelName);
        return getLevel(channelId);
    }

    public boolean hasValuesInRange(Channels min, Channels max) {
        boolean result = false;
        for (int i = 0; !result && i < fArray.length; i++) {
            result = fArray[i] >= min.fArray[i] && fArray[i] <= max.fArray[i];
        }
        return result;
    }

    public boolean lessThanOrEqualsTo(Channels channels) {
        boolean result = true;
        for (int i = 0; result && i < fArray.length; i++) {
            result = (fArray[i] <= channels.fArray[i]);
        }
        return result;
    }

    /**
     * For each channel it gets the maximal value and sets it
     * 
     * @param score
     */
    public Channels max(Channels score) {
        for (int i = 0; i < fArray.length; i++) {
            if (fArray[i] < score.fArray[i]) {
                fArray[i] = score.fArray[i];
            }
        }
        return this;
    }

    /**
     * For each channel it gets the minimal value and sets it
     * 
     * @param score
     */
    public Channels min(Channels score) {
        for (int i = 0; i < fArray.length; i++) {
            if (fArray[i] > score.fArray[i]) {
                fArray[i] = score.fArray[i];
            }
        }
        return this;
    }

    public boolean moreThanOrEqualsTo(Channels channels) {
        boolean result = true;
        for (int i = 0; result && i < fArray.length; i++) {
            result = (fArray[i] >= channels.fArray[i]);
        }
        return result;
    }

    /**
     * Multiplies all channel values to the corresponding values from the given
     * object
     * 
     * @param levels source of the channel values used to multiple internal
     *        channels
     */
    public Channels multiply(Channels levels) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] *= levels.fArray[i];
        }
        return this;
    }

    /**
     * Multiplies all channels to the specified value.
     */
    public Channels multiply(double value) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] *= value;
        }
        return this;
    }

    /**
     * Resets all channels to the specified value
     * 
     * @param val the default value to set
     */
    public Channels set(double val) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] = val;
        }
        return this;
    }

    /**
     * Multiplies the given value to all channels of the filter and sets results
     * in the internal array. This method will replace existing values.
     * 
     * @param value the value to multiply
     * @param filter the filter used as a source of levels
     */
    public Channels set(double value, Channels filter) {
        for (int i = 0; i < fArray.length; i++) {
            fArray[i] = value * filter.fArray[i];
        }
        return this;
    }

    /**
     * Replaces the value of the specified channel
     * 
     * @param channelId the identifier of the channel
     * @param weight the level of the channel to set
     */
    public Channels setLevel(int channelId, double weight) {
        fArray[channelId] = weight;
        return this;
    }

    /**
     * Sets the value of the specified channel.
     * 
     * @param channelName the name of the channel
     * @param weight the level of the channel to set
     */
    public Channels setLevel(String channelName, double weight) {
        int channelId = fManager.getChannelId(channelName);
        setLevel(channelId, weight);
        return this;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        for (int i = 0; i < fArray.length; i++) {
            if (i > 0) {
                buf.append(",");
            }
            appendChannel(buf, i);
        }
        buf.append("}");
        return buf.toString();
    }

    public String toString(String... channels) {
        IChannelsManager manager = getChannelManager();
        StringBuilder buf = new StringBuilder();
        for (String channel : channels) {
            int channelId = manager.getChannelId(channel);
            if (buf.length() > 0) {
                buf.append(",");
            }
            appendChannel(buf, channelId);
        }
        return buf.toString();
    }

    /**
     * Checks that values of all channels are in the specified range. If a value
     * is out of range then this method sets it to the corresponding limit
     * value.
     * 
     * @param min minimal possible value
     * @param max maximal value
     */
    public Channels trimToRange(double min, double max) {
        for (int i = 0; i < fArray.length; i++) {
            if (fArray[i] > max) {
                fArray[i] = max;
            }
            if (fArray[i] < min) {
                fArray[i] = min;
            }
        }
        return this;
    }
}