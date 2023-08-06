package org.bookmap.Helpers;

import java.awt.*;

public class BullsStrategy implements IMovementCalculable {
    @Override
    public boolean doIncrementCounter(double movement) {
        return movement < 0;
    }

    @Override
    public Color getStrategyColor() {
        return Color.GREEN;
    }
}
