package org.bookmap.Events;

import velox.api.layer1.common.Log;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable.DataCoordinateMarker;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class BarEvent implements
        CustomGeneratedEvent,
        DataCoordinateMarker {
    private static final long serialVersionUID = 1L;

    private long time;
    private final double BAR_START = 0;
    private int volume;
    private double open, close;
    private transient int bodyWidthPx;

    public BarEvent(long time) {
        this(time, 0, Double.NaN);
    }

    public BarEvent(long time, int volume, double open) {
        this(time, volume, open, open,-1);
    }

    public BarEvent(long time, int volume, double open, double close, int bodyWidthPx) {
        super();
        this.time = time;
        this.volume = volume;
        this.open = open;
        this.close = close;
        this.bodyWidthPx = bodyWidthPx;
    }

    public BarEvent(BarEvent other) {
        this(other.time, other.volume, other.open, other.close, other.bodyWidthPx);
    }

    public void setBodyWidthPx(int bodyWidthPx) {
        this.bodyWidthPx = bodyWidthPx;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public double getMinY() {
        return BAR_START;
    }

    @Override
    public double getMaxY() {
        return volume;
    }

    @Override
    public double getValueY() {
        return BAR_START;
    }

    public int getVolume() {
        return volume;
    }

    public double getClose() {
        return close;
    }

    public double getMovement() {
        return close - open;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "[" + time + ": " + volume + "/" + open + "/" + close + "]";
    }
    @Override
    public OnlineCalculatable.Marker makeMarker(Function<Double, Integer> yDataCoordinateToPixelFunction) {
        /* May return minimum value of an integer upon rewinding a replay and then throwing OutOfMemory at line 80 due
        to abysmally large imageHeight. No idea why that value is being returned, because volume at that moment is within
        normal values. */
        int top = yDataCoordinateToPixelFunction.apply((double) volume);
        if (top < 0)
            return null;

        int bottom = yDataCoordinateToPixelFunction.apply(BAR_START);

        int imageHeight = top - bottom + 1;
        BufferedImage bufferedImage = new BufferedImage(bodyWidthPx, imageHeight, BufferedImage.TYPE_INT_ARGB);

        int imageCenterX = bufferedImage.getWidth() / 2;

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 0, bodyWidthPx, top);

        graphics.dispose();

        int iconOffsetX = -imageCenterX;
        return new OnlineCalculatable.Marker(0, iconOffsetX, 0, bufferedImage);
    }

    @Override
    public Object clone() {
        return new BarEvent(time, volume, open, close, bodyWidthPx);
    }

    public void update(int volume, double price) {
        this.volume += volume;

        if (Double.isNaN(open))
            open = price;

        close = price;
    }

    /*public void updatePrice(double price) {
        if (Double.isNaN(open))
            open = price;

        close = price;
    }

    public void updateVolume(int volume) {
        this.volume += volume;
    }*/

    public void update(BarEvent nextBar) {
        /*updateVolume(nextBar.volume);
        updatePrice(nextBar.open);
        updatePrice(nextBar.close);*/
        update(nextBar.volume, nextBar.open);
        update(nextBar.volume, nextBar.close);
    }

    public void swapMode() {
        volume = 0;
    }
}
