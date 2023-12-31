package org.bookmap;

import org.bookmap.Colors.WaveColorScheme;
import org.bookmap.Events.BarEvent;
import org.bookmap.Events.CustomEventAggregation;
import org.bookmap.Settings.SettingsPanel;
import org.bookmap.Settings.WeisWaveSettings;
import org.bookmap.Strategies.UserMessageStrategyUpdateGenerator;
import velox.api.layer1.*;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.ListenableHelper;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.layers.strategies.interfaces.*;
import velox.api.layer1.messages.GeneratedEventInfo;
import velox.api.layer1.messages.Layer1ApiHistoricalDataLoadedMessage;
import velox.api.layer1.messages.Layer1ApiUserMessageAddStrategyUpdateGenerator;
import velox.api.layer1.messages.UserMessageLayersChainCreatedTargeted;
import velox.api.layer1.messages.indicators.*;
import velox.api.layer1.settings.Layer1ConfigSettingsInterface;
import velox.gui.StrategyPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Layer1Attachable
@Layer1StrategyName("Ilbko's Weis Wave")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class IlbkoWeisWave implements
        Layer1ApiFinishable,
        Layer1ApiAdminAdapter,
        Layer1ApiInstrumentListener,
        OnlineCalculatable,
        Layer1CustomPanelsGetter,
        Layer1ConfigSettingsInterface,
        SettingsPanel.SettingsPanelCallback
{
    private static final CustomEventAggregatble BAR_EVENTS_AGGREGATOR = new CustomEventAggregation();

    private static final String INDICATOR_NAME_BARS_BOTTOM = "Bars: bottom panel";

    private static final String TREE_NAME = "Bars";

    private static final Class<?>[] CUSTOM_EVENTS = new Class<?>[] { BarEvent.class };

    private static final int MAX_BODY_WIDTH = 30;

    private static final int MIN_BODY_WIDTH = 1;

    private final Layer1ApiProvider provider;

    private final Map<String, Double> sizeMultiplierMap = new HashMap<>();

    private final Map<String, String> indicatorsFullNameToUserName = new HashMap<>();

    private DataStructureInterface dataStructureInterface;

    private SettingsAccess settingsAccess;
    private final Map<String, WeisWaveSettings> settingsMap = new HashMap<>();

    private Object locker = new Object();

    private int trendDetectionLength;
    private long candleIntervalNs;

    private final BufferedImage tradeIcon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

    public IlbkoWeisWave(Layer1ApiProvider provider) {
        this.provider = provider;

        ListenableHelper.addListeners(provider, this);

        candleIntervalNs = TimeUnit.SECONDS.toNanos(60);

        Graphics graphics = tradeIcon.getGraphics();
        graphics.setColor(Color.BLUE);
        graphics.drawLine(0, 0, 15, 15);
        graphics.drawLine(15, 0, 0, 15);
    }

    private Layer1ApiUserMessageAddStrategyUpdateGenerator getGeneratorMessage(boolean isAdd) {
        return new Layer1ApiUserMessageAddStrategyUpdateGenerator(
                IlbkoWeisWave.class,
                TREE_NAME,
                isAdd,
                true,
                true,
                new UserMessageStrategyUpdateGenerator(candleIntervalNs, trendDetectionLength),
                new GeneratedEventInfo[] {
                        new GeneratedEventInfo(
                                BarEvent.class,
                                BarEvent.class,
                                BAR_EVENTS_AGGREGATOR)
                });
    }

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        sizeMultiplierMap.put(alias, instrumentInfo.sizeMultiplier);
        if (settingsMap.isEmpty()) {
            WeisWaveSettings settings = getSettingsFor(alias);
            this.trendDetectionLength = settings.getTrendDetectionLength();
            this.candleIntervalNs = TimeUnit.SECONDS.toNanos(settings.getSeconds());
        }
    }

    @Override
    public void onInstrumentRemoved(String s) {

    }

    @Override
    public void onInstrumentNotFound(String s, String s1, String s2) {

    }

    @Override
    public void onInstrumentAlreadySubscribed(String s, String s1, String s2) {

    }

    @Override
    public void calculateValuesInRange(String indicatorName, String indicatorAlias, long t0, long intervalWidth, int intervalsNumber, CalculatedResultListener calculatedResultListener) {
        List<DataStructureInterface.TreeResponseInterval> result = dataStructureInterface.get(IlbkoWeisWave.class, TREE_NAME, t0,
                intervalWidth, intervalsNumber, indicatorAlias, CUSTOM_EVENTS);

        int bodyWidth = getBodyWidth(intervalWidth);

        for (int i = 1; i <= intervalsNumber; i++) {

            BarEvent value = getBarEvent(result.get(i));
            if (value != null) {
                value = new BarEvent(value);

                value.applySizeMultiplier(sizeMultiplierMap.get(indicatorAlias));
                value.setBodyWidthPx(bodyWidth);

                calculatedResultListener.provideResponse(value);
            } else {
                calculatedResultListener.provideResponse(Double.NaN);
            }
        }

        calculatedResultListener.setCompleted();
    }

    @Override
    public OnlineValueCalculatorAdapter createOnlineValueCalculator(String indicatorName, String indicatorAlias, long l, Consumer<Object> consumer, InvalidateInterface invalidateInterface) {
        return new OnlineValueCalculatorAdapter() {
            int bodyWidth = MAX_BODY_WIDTH;

            @Override
            public void onIntervalWidth(long intervalWidth) {
                this.bodyWidth = getBodyWidth(intervalWidth);
            }

            @Override
            public void onUserMessage(Object data) {
                if (data instanceof CustomGeneratedEventAliased) {
                    CustomGeneratedEventAliased aliasedEvent = (CustomGeneratedEventAliased) data;
                    if (indicatorAlias.equals(aliasedEvent.alias) && aliasedEvent.event instanceof BarEvent) {
                        BarEvent event = (BarEvent)aliasedEvent.event;
                        event = new BarEvent(event);

                        event.applySizeMultiplier(sizeMultiplierMap.get(indicatorAlias));
                        event.setBodyWidthPx(bodyWidth);

                        consumer.accept(event);
                    }
                }
            }
        };
    }

    @Override
    public void onUserMessage(Object data) {
        if (data.getClass() == UserMessageLayersChainCreatedTargeted.class) {
            UserMessageLayersChainCreatedTargeted message = (UserMessageLayersChainCreatedTargeted) data;
            if (message.targetClass == getClass()) {
                addAndActivateIndicator();
            }
        }

        if (data.getClass() == Layer1ApiHistoricalDataLoadedMessage.class) {
            reloadIndicator();
        }
    }

    private Layer1ApiUserMessageModifyIndicator getUserMessageAdd() {
        return Layer1ApiUserMessageModifyIndicator.builder(IlbkoWeisWave.class, INDICATOR_NAME_BARS_BOTTOM)
                .setIsAdd(true)
                .setGraphType(Layer1ApiUserMessageModifyIndicator.GraphType.BOTTOM)
                .setOnlineCalculatable(this)
                .setIndicatorColorScheme(new WaveColorScheme())
                .setIndicatorLineStyle(IndicatorLineStyle.NONE)
                .build();
    }

    private void addIndicator() {
        Layer1ApiUserMessageModifyIndicator message = getUserMessageAdd();

        synchronized (indicatorsFullNameToUserName) {
            indicatorsFullNameToUserName.put(message.fullName, message.userName);
        }

        provider.sendUserMessage(message);
    }

    private void addAndActivateIndicator() {
        provider.sendUserMessage(new Layer1ApiDataInterfaceRequestMessage(dataStructureInterface -> this.dataStructureInterface = dataStructureInterface));
        addIndicator();
        provider.sendUserMessage(getGeneratorMessage(true));
    }

    @Override
    public void finish() {
        synchronized (indicatorsFullNameToUserName) {
            for (String userName : indicatorsFullNameToUserName.values()) {
                provider.sendUserMessage(new Layer1ApiUserMessageModifyIndicator(IlbkoWeisWave.class, userName, false));
            }
        }

        provider.sendUserMessage(getGeneratorMessage(false));
    }

    private void reloadIndicator() {
        finish();
        addAndActivateIndicator();
    }

    public int getBodyWidth(long intervalWidth) {
        long bodyWidth = candleIntervalNs / intervalWidth;
        bodyWidth = Math.max(bodyWidth, MIN_BODY_WIDTH);
        bodyWidth = Math.min(bodyWidth, MAX_BODY_WIDTH);
        return (int) bodyWidth;
    }

    private BarEvent getBarEvent(DataStructureInterface.TreeResponseInterval treeResponseInterval) {
        Object result = treeResponseInterval.events.get(BarEvent.class.toString());
        if (result != null) {
            return (BarEvent) result;
        } else {
            return null;
        }
    }

    @Override
    public StrategyPanel[] getCustomGuiFor(String alias, String indicatorName) {
        return new StrategyPanel[] { new SettingsPanel(this, alias, getSettingsFor(alias)) };
    }

    @Override
    public void acceptSettingsInterface(SettingsAccess settingsAccess) {
        this.settingsAccess = settingsAccess;
    }

    private WeisWaveSettings getSettingsFor(String alias) {
        synchronized (locker) {
            WeisWaveSettings settings = settingsMap.get(alias);
            if (settings == null) {
                settings = (WeisWaveSettings) settingsAccess.getSettings(alias, INDICATOR_NAME_BARS_BOTTOM, WeisWaveSettings.class);
                if (settings.isEmpty()) {
                    settings = new WeisWaveSettings(2, 60L);
                    settingsChanged(alias, settings);
                }
                settingsMap.put(alias, settings);
            }

            return settings;
        }
    }

    private void settingsChanged(String alias, WeisWaveSettings weisWaveSettings) {
        synchronized (locker) {
            settingsAccess.setSettings(alias, INDICATOR_NAME_BARS_BOTTOM, weisWaveSettings, WeisWaveSettings.class);
            BarEvent.clearCache();
        }
    }

    @Override
    public void onSettingsUpdate(String alias, WeisWaveSettings weisWaveSettings) {
        this.trendDetectionLength = weisWaveSettings.getTrendDetectionLength();
        this.candleIntervalNs = TimeUnit.SECONDS.toNanos(weisWaveSettings.getSeconds());

        finish();
        addAndActivateIndicator();

        settingsChanged(alias, weisWaveSettings);
        settingsMap.put(alias, weisWaveSettings);
    }
}
