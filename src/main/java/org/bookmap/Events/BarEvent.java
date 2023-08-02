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
    private final double OPEN = 0;
    private int volume;
    private transient int bodyWidthPx;

    public BarEvent(long time) {
        this(time, 0);
    }

    public BarEvent(long time, int volume) {
        this(time, volume, -1);
    }

    public BarEvent(long time, int volume, int bodyWidthPx) {
        super();
        this.time = time;
        this.volume = volume;
        this.bodyWidthPx = bodyWidthPx;
    }

    public BarEvent(BarEvent other) {
        this(other.time, other.volume, other.bodyWidthPx);
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
        return OPEN;
    }

    @Override
    public double getMaxY() {
        return volume;
    }

    @Override
    public double getValueY() {
        return OPEN;
    }

    public int getVolume() {
        return volume;
    }

    @Override
    public OnlineCalculatable.Marker makeMarker(Function<Double, Integer> yDataCoordinateToPixelFunction) {
        /* May return minimum value of an integer upon rewinding a replay and then throwing OutOfMemory at line 80 due
        to abysmally large imageHeight. No idea why that value is being returned, because volume at that moment is within
        normal values. */
        int top = yDataCoordinateToPixelFunction.apply((double) volume);
        if (top < 0)
            return null;

        int bottom = yDataCoordinateToPixelFunction.apply(OPEN);

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
        return new BarEvent(time, volume, bodyWidthPx);
    }

    public void update(int volume) {
        this.volume += volume;
    }

    public void update(BarEvent nextBar) {
        update(nextBar.volume);
    }
}
