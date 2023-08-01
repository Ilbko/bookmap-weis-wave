package org.bookmap.Events;


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
    private double open, close, low, high;
    private transient int bodyWidthPx;

    public BarEvent(long time) {
        this(time, Double.NaN);
    }

    public BarEvent(long time, double open) {
        this(time, open, -1);
    }

    public BarEvent(long time, double open, int bodyWidthPx) {
        this(time, open, open, open, open, bodyWidthPx);
    }

    public BarEvent(long time, double open, double low, double high, double close, int bodyWidthPx) {
        super();
        this.time = time;
        this.open = open;
        this.low = low;
        this.high = high;
        this.close = close;
        this.bodyWidthPx = bodyWidthPx;
    }

    public BarEvent(BarEvent other) {
        this(other.time, other.open, other.low, other.high, other.close, other.bodyWidthPx);
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
        return open;
    }

    @Override
    public double getMaxY() {
        return high;
    }

    @Override
    public double getValueY() {
        return low;
    }

    public double getClose() {
        return close;
    }

    @Override
    public String toString() {
        return "BarEvent{" +
                "time=" + time +
                ", open=" + open +
                ", close=" + close +
                ", low=" + low +
                ", high=" + high +
                '}';
    }

    @Override
    public OnlineCalculatable.Marker makeMarker(Function<Double, Integer> yDataCoordinateToPixelFunction) {
        int top = yDataCoordinateToPixelFunction.apply(high);
        int bottom = yDataCoordinateToPixelFunction.apply(low);
        int openPx = yDataCoordinateToPixelFunction.apply(open);
        int closePx = yDataCoordinateToPixelFunction.apply(close);

        int bodyLow = Math.min(openPx, closePx);
        int bodyHigh = Math.max(openPx, closePx);

        int imageHeight = top - bottom + 1;
        BufferedImage bufferedImage = new BufferedImage(bodyWidthPx, imageHeight, BufferedImage.TYPE_INT_ARGB);
        int imageCenterX = bufferedImage.getWidth() / 2;

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        graphics.setColor(Color.WHITE);
        graphics.drawLine(imageCenterX, 0, imageCenterX, imageHeight);

        graphics.setColor(open < close ? Color.GREEN : Color.RED);
        graphics.fillRect(0, top - bodyHigh, bodyWidthPx, bodyHigh - bodyLow + 1);

        graphics.dispose();


        int iconOffsetY = bottom - closePx;

        int iconOffsetX = -imageCenterX;
        return new OnlineCalculatable.Marker(close, iconOffsetX, iconOffsetY, bufferedImage);
    }

    @Override
    public Object clone() {
        return new BarEvent(time, open, low, high, close, bodyWidthPx);
    }

    public void update(double price) {
        if (Double.isNaN(price)) {
            return;
        }

        if (Double.isNaN(open)) {
            open = price;
            low = price;
            high = price;
        } else {
            low = Math.min(low, price);
            high = Math.max(high, price);
        }
        close = price;
    }

    public void update(BarEvent nextBar) {
        update(nextBar.open);
        update(nextBar.low);
        update(nextBar.high);
        update(nextBar.close);
    }

    public void applyPips(double pips) {
        open *= pips;
        low *= pips;
        high *= pips;
        close *= pips;
    }
}
