package org.bookmap.Helpers;

public class BearsStrategy implements IMovementCalculable {
    @Override
    public boolean doIncrementCounter(double movement) {
        if (movement > 0)
            return true;

        return false;
    }
}
