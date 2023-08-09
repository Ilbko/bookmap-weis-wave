package org.bookmap.Strategies;

import org.bookmap.Events.BarEvent;
import velox.api.layer1.data.TradeInfo;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEventAliased;
import velox.api.layer1.messages.indicators.StrategyUpdateGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class UserMessageStrategyUpdateGenerator implements StrategyUpdateGenerator {
    private final long candleIntervalNs;
    private final int trendDetectionLength;

    private Consumer<CustomGeneratedEventAliased> consumer;

    private long time = 0;

    private final Map<String, TrendDetectionForAlias> aliasToTrendDetection = new ConcurrentHashMap<>();


    public UserMessageStrategyUpdateGenerator(long candleIntervalNs, int trendDetectionLength) {
        this.candleIntervalNs = candleIntervalNs;
        this.trendDetectionLength = trendDetectionLength;
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
        TrendDetectionForAlias trendDetection = aliasToTrendDetection.get(alias);
        if (trendDetection == null) {
            trendDetection = new TrendDetectionForAlias();
            aliasToTrendDetection.put(alias, trendDetection);
        }

        long barStartTime = getBarStartTime(time);

        BarEvent bar = trendDetection.getLastBar();

        if (bar == null) {
            bar = new BarEvent(barStartTime);
            trendDetection.setLastBar(bar);
        }

        if (size != 0) {
            bar.updatePrice(price);
        }

        bar.updateVolume(size);
    }

    @Override
    public void onInstrumentRemoved(String alias) {
        aliasToTrendDetection.remove(alias);
    }

    @Override
    public void setTime(long l) {
        this.time = l;

        long barStartTime = getBarStartTime(time);
        for (Map.Entry<String, TrendDetectionForAlias> entry : aliasToTrendDetection.entrySet()) {
            String alias = entry.getKey();
            TrendDetectionForAlias trendDetection = entry.getValue();
            BarEvent bar = trendDetection.getLastBar();

            if (barStartTime != bar.getTime()) {
                bar.setTime(time);

                AtomicInteger trendDetectionCounter = trendDetection.getTrendDetectionCounter();
                if (trendDetection.doIncrementCounter(bar.getMovement())) {
                    if (trendDetectionCounter.incrementAndGet() == trendDetectionLength) {
                        trendDetectionCounter.set(0);

                        trendDetection.changeTrendDetectionStrategy();
                        bar.setVolume(bar.getVolume() - trendDetection.getLastVolumeAtInterval());
                    }
                } else {
                    trendDetectionCounter.set(0);
                }

                bar.changeBarColor(trendDetection.getColorFromStrategy());
                trendDetection.setLastVolumeAtInterval(bar.getVolume());

                consumer.accept(new CustomGeneratedEventAliased(bar, alias));
                bar = new BarEvent(barStartTime, bar.getVolume(), bar.getClose(), bar.getOpen(), bar.getBarColor());
                entry.getValue().setLastBar(bar);
            }
        }
    }

    @Override
    public void onUserMessage(Object o) { }

    private long getBarStartTime(long time) {
        return time - time % candleIntervalNs;
    }
}
