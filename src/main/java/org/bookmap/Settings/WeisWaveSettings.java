package org.bookmap.Settings;

import velox.api.layer1.settings.StrategySettingsVersion;

@StrategySettingsVersion(currentVersion = 2, compatibleVersions = {})
public class WeisWaveSettings {
    private int trendDetectionLength;
    private long seconds;

    public WeisWaveSettings() {}

    public WeisWaveSettings(int trendDetectionLength, long seconds) {
        this.trendDetectionLength = trendDetectionLength;
        this.seconds = seconds;
    }

    public int getTrendDetectionLength() {
        return trendDetectionLength;
    }

    public long getSeconds() {
        return seconds;
    }

    public boolean isEmpty() {
        return trendDetectionLength <= 0;
    }
}
