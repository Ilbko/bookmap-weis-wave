package org.bookmap.Events;

import velox.api.layer1.layers.strategies.interfaces.CustomEventAggregatble;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;

public class CustomEventAggregation implements CustomEventAggregatble {
    @Override
    public CustomGeneratedEvent getInitialValue(long l) {
        return new BarEvent(l);
    }

    @Override
    public void aggregateAggregationWithAggregation(CustomGeneratedEvent aggregation, CustomGeneratedEvent value) {
        BarEvent aggregationEvent = (BarEvent) aggregation;
        BarEvent valueEvent = (BarEvent) value;
        aggregationEvent.update(valueEvent);
    }

    @Override
    public void aggregateAggregationWithValue(CustomGeneratedEvent aggregation1, CustomGeneratedEvent aggregation2) {
        BarEvent aggregationEvent1 = (BarEvent) aggregation1;
        BarEvent aggregationEvent2 = (BarEvent) aggregation2;
        aggregationEvent1.update(aggregationEvent2);
    }
}
