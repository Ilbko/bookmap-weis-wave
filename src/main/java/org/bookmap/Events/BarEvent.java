package org.bookmap.Events;

import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable.DataCoordinateMarker;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class BarEvent implements
        CustomGeneratedEvent,
        DataCoordinateMarker {
    @Serial
    private static final long serialVersionUID = 1L;

    private long time;
    private final double BAR_START = 0;
    private int volume;
    private double open, close;
    private Color barColor;
    private transient int bodyWidthPx;
    private static final int CACHE_MAX_SIZE = 100;
    private static Map<Integer, BufferedImage> barsCache = new HashMap<>();

    public BarEvent(long time) {
        this(time, 0, Double.NaN, null);
    }

    public BarEvent(long time, int volume, double open, Color barColor) {
        this(time, volume, open, open, barColor, -1);
    }

    public BarEvent(long time, int volume, double open, double close, Color barColor) {
        this(time, volume, open, close, barColor, -1);
    }

    public BarEvent(long time, int volume, double open, double close, Color barColor, int bodyWidthPx) {
        super();
        this.time = time;
        this.volume = volume;
        this.open = open;
        this.close = close;
        this.barColor = barColor;
        this.bodyWidthPx = bodyWidthPx;
    }

    public BarEvent(BarEvent other) {
        this(other.time, other.volume, other.open, other.close, other.barColor, other.bodyWidthPx);
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

    public double getOpen() {
        return open;
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

        BufferedImage bufferedImage = barsCache.get(imageHeight);
        if (bufferedImage == null) {
            if (barsCache.size() >= CACHE_MAX_SIZE)
                barsCache.clear();

            bufferedImage = new BufferedImage(bodyWidthPx, imageHeight, BufferedImage.TYPE_INT_ARGB);
            barsCache.put(imageHeight, bufferedImage);
        }
        //BufferedImage bufferedImage = new BufferedImage(bodyWidthPx, imageHeight, BufferedImage.TYPE_INT_ARGB);

        int imageCenterX = bufferedImage.getWidth() / 2;

        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        graphics.setColor(barColor);
        graphics.fillRect(0, 0, bodyWidthPx, top);

        graphics.dispose();

        int iconOffsetX = -imageCenterX;
        return new OnlineCalculatable.Marker(0, iconOffsetX, 0, bufferedImage);
    }

    @Override
    public Object clone() {
        return new BarEvent(time, volume, open, close, barColor, bodyWidthPx);
    }

    public void updatePrice(double price) {
        if (Double.isNaN(price)) {
            return;
        }

        if (Double.isNaN(open))
            open = price;

        close = price;
    }

    public void updateVolume(int volume) {
        this.volume += volume;
    }

    public void setBarColor(Color barColor) {
        if (this.barColor == null)
            this.barColor = barColor;
    }

    public void changeBarColor(Color barColor) {
        this.barColor = barColor;
    }

    public Color getBarColor() {
        return barColor;
    }

    public void update(BarEvent nextBar) {
        updateVolume(nextBar.volume);
        updatePrice(nextBar.open);
        updatePrice(nextBar.close);
        setBarColor(nextBar.barColor);
    }

    public void applySizeMultiplier(double multiplier) {
        this.volume /= multiplier;
    }

    public static void clearCache() {
        barsCache.clear();
    }
}
