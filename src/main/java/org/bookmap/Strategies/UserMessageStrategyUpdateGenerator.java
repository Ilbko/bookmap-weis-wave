package org.bookmap.Strategies;

import org.bookmap.Events.BarEvent;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEventAliased;
import velox.api.layer1.messages.indicators.StrategyUpdateGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class UserMessageStrategyUpdateGenerator implements StrategyUpdateGenerator {
    private final long candleIntervalNs;

    private Consumer<CustomGeneratedEventAliased> consumer;

    private long time = 0;

    private Map<String, BarEvent> aliasToLastBar = new HashMap<>();

    public UserMessageStrategyUpdateGenerator(long candleIntervalNs) {
        this.candleIntervalNs = candleIntervalNs;
    }

    @Override
    public void setGeneratedEventsConsumer(Consumer<CustomGeneratedEventAliased> consumer) {
        this.consumer = consumer;
    }

    @Override
    public Consumer<CustomGeneratedEventAliased> getGeneratedEventsConsumer() {
        return consumer;
    }

    @Override
    public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
        BarEvent bar = aliasToLastBar.get(alias);

        long barStartTime = getBarStartTime(time);

        if (bar == null) {
            bar = new BarEvent(barStartTime);
            aliasToLastBar.put(alias, bar);
        }

        if (barStartTime != bar.getTime()) {
            bar.setTime(time);
            consumer.accept(new CustomGeneratedEventAliased(bar, alias));
            bar = new BarEvent(barStartTime, bar.getVolume(), bar.getClose());
            aliasToLastBar.put(alias, bar);
        }

        if (size != 0) {
            bar.update(size, price);
        }
    }

    @Override
    public void onInstrumentRemoved(String alias) {
        aliasToLastBar.remove(alias);
    }

    @Override
    public void setTime(long l) {
        this.time = l;

        long barStartTime = getBarStartTime(time);
        for (Map.Entry<String, BarEvent> entry : aliasToLastBar.entrySet()) {
            String alias = entry.getKey();
            BarEvent bar = entry.getValue();

            if (barStartTime != bar.getTime()) {
                bar.setTime(time);
                consumer.accept(new CustomGeneratedEventAliased(bar, alias));
                bar = new BarEvent(barStartTime, bar.getVolume(), bar.getClose());
                entry.setValue(bar);
            }
        }
    }

    @Override
    public void onUserMessage(Object o) { }

    private long getBarStartTime(long time) {
        return time - time % candleIntervalNs;
    }
}
