package org.bookmap.Strategies;

import org.bookmap.Events.BarEvent;
import org.bookmap.Helpers.BearsStrategy;
import org.bookmap.Helpers.BullsStrategy;
import org.bookmap.Helpers.IMovementCalculable;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TrendDetectionForAlias {
    private BarEvent lastBar;
    private final AtomicInteger trendDetectionCounter;
    private IMovementCalculable movementStrategy;

    private int lastVolumeAtInterval;

    public TrendDetectionForAlias() {
        trendDetectionCounter = new AtomicInteger(0);
        movementStrategy = new BullsStrategy();
    }

    public BarEvent getLastBar() {
        return lastBar;
    }

    public AtomicInteger getTrendDetectionCounter() {
        return trendDetectionCounter;
    }

    public void setLastBar(BarEvent lastBar) {
        this.lastBar = lastBar;
    }

    public int getLastVolumeAtInterval() {
        return lastVolumeAtInterval;
    }

    public void setLastVolumeAtInterval(int lastVolumeAtInterval) {
        this.lastVolumeAtInterval = lastVolumeAtInterval;
    }

    public boolean doIncrementCounter(double priceMovement) {
        return movementStrategy.doIncrementCounter(priceMovement);
    }

    public void changeTrendDetectionStrategy() {
        if (movementStrategy instanceof BullsStrategy)
            movementStrategy = new BearsStrategy();
        else
            movementStrategy = new BullsStrategy();
    }

    public Color getColorFromStrategy() {
        return movementStrategy.getStrategyColor();
    }
}
