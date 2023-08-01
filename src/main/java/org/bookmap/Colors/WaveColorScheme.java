package org.bookmap.Colors;

import org.bookmap.IlbkoWeisWave;
import velox.api.layer1.messages.indicators.IndicatorColorScheme;

import java.awt.*;

public class WaveColorScheme implements IndicatorColorScheme {
    private static final String INDICATOR_LINE_COLOR_NAME = "Trade markers line";
    private static final Color INDICATOR_LINE_DEFAULT_COLOR = Color.RED;

    @Override
    public ColorDescription[] getColors() {
        return new ColorDescription[] {
                new ColorDescription(IlbkoWeisWave.class, INDICATOR_LINE_COLOR_NAME, INDICATOR_LINE_DEFAULT_COLOR, false),
        };
    }

    @Override
    public String getColorFor(Double aDouble) {
        return INDICATOR_LINE_COLOR_NAME;
    }

    @Override
    public ColorIntervalResponse getColorIntervalsList(double v, double v1) {
        return new ColorIntervalResponse(new String[] {INDICATOR_LINE_COLOR_NAME}, new double[] {});
    }
}
