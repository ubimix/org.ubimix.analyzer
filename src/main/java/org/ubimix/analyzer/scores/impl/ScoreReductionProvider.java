package org.ubimix.analyzer.scores.impl;

import org.ubimix.analyzer.scores.Channels;
import org.ubimix.analyzer.scores.IChannelsManager;
import org.ubimix.analyzer.scores.IScoreReductionProvider;

/**
 * This implementation of the {@link IScoreReductionProvider} interface uses
 * Fibonacci sequence to calculate the score reduction factors.
 * 
 * @author kotelnikov
 */
public class ScoreReductionProvider implements IScoreReductionProvider {

    private static int[] FIBONACCI;

    private static int[] getFibonacci() {
        if (FIBONACCI == null) {
            FIBONACCI = new int[30];
            // N: 0-25
            // 0,1,1,2,3,5,8,13,21,34,55,89,144,233,377,610,987,1597,2584,4181,6765,10946,17711,28657,46368,75025
            for (int i = 0; i < FIBONACCI.length; i++) {
                int c = (i >= 2) ? FIBONACCI[i - 2] + FIBONACCI[i - 1] : 1;
                FIBONACCI[i] = c;
            }
        }
        return FIBONACCI;
    }

    private Channels[] fDistanceReductions;

    public ScoreReductionProvider(IChannelsManager channelsManager) {
        int[] f = getFibonacci();
        fDistanceReductions = new Channels[f.length];
        for (int i = 0; i < f.length; i++) {
            Channels channel = channelsManager.newChannels(1);
            channel.div(f[i]);
            fDistanceReductions[i] = channel;
        }
    }

    /**
     * @see org.ubimix.analyzer.scores.IScoreReductionProvider#getScoreReduction(int)
     */
    public Channels getScoreReduction(int distance) {
        if (distance >= fDistanceReductions.length) {
            return null;
        }
        return fDistanceReductions[distance];
    }
}