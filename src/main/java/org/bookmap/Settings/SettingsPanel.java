package org.bookmap.Settings;

import velox.gui.StrategyPanel;

import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends StrategyPanel {
    private final JComboBox<TimeInterval> timeIntervalsComboBox = new JComboBox<>();
    private final JSpinner trendDetectionLengthSpinner = new JSpinner();

    private final TimeInterval[] timeIntervals = {
            new TimeInterval("5 sec", 5),
            new TimeInterval("10 sec", 10),
            new TimeInterval("15 sec", 15),
            new TimeInterval("30 sec", 30),
            new TimeInterval("1 min", 60),
            new TimeInterval("2 min", 120),
            new TimeInterval("3 min", 180),
            new TimeInterval("5 min", 300),
            new TimeInterval("10 min", 600),
            new TimeInterval("15 min", 900),
            new TimeInterval("30 min", 1800)};

    public interface SettingsPanelCallback {
        void onSettingsUpdate(WeisWaveSettings weisWaveSettings);
    }

    public SettingsPanel(SettingsPanelCallback callback) {
        super("Settings");
        GridBagLayout gridBagLayout = new GridBagLayout();

        setLayout(gridBagLayout);

        initTimeIntervalsComboBox(callback);
        initTrendDetectionLengthSpinner(callback);
    }

    private void initTimeIntervalsComboBox(SettingsPanelCallback callback) {
        JLabel timeIntervalsLabel = new JLabel("Interval: ");
        GridBagConstraints timeIntervalsConstraints = new GridBagConstraints();
        timeIntervalsConstraints.anchor = GridBagConstraints.WEST;
        timeIntervalsConstraints.insets = new Insets(0, 5, 5, 0);
        timeIntervalsConstraints.gridx = 0;
        timeIntervalsConstraints.gridy = 0;
        add(timeIntervalsLabel, timeIntervalsConstraints);

        timeIntervalsComboBox.setModel(new DefaultComboBoxModel<>(timeIntervals));
        GridBagConstraints timeIntervalsComboBoxConstraints = new GridBagConstraints();
        timeIntervalsComboBoxConstraints.anchor = GridBagConstraints.EAST;
        timeIntervalsComboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
        timeIntervalsComboBoxConstraints.insets = new Insets(0, 0, 5, 0);
        timeIntervalsComboBoxConstraints.gridx = 1;
        timeIntervalsComboBoxConstraints.gridy = 0;
        timeIntervalsComboBox.setSelectedIndex(4);
        timeIntervalsComboBox.addItemListener(e -> callback.onSettingsUpdate(getCurrentWeisWaveSettings()));

        add(timeIntervalsComboBox, timeIntervalsComboBoxConstraints);
    }

    private void initTrendDetectionLengthSpinner(SettingsPanelCallback callback) {
        JLabel trendDetectionLengthLabel = new JLabel("Trend detection length: ");
        trendDetectionLengthLabel.setToolTipText("Set how many closing prices of a set time interval " +
                "are required to be considered a change of a trend.");
        GridBagConstraints trendDetectionLengthConstraints = new GridBagConstraints();
        trendDetectionLengthConstraints.anchor = GridBagConstraints.WEST;
        trendDetectionLengthConstraints.insets = new Insets(0, 5, 5, 0);
        trendDetectionLengthConstraints.gridx = 0;
        trendDetectionLengthConstraints.gridy = 1;
        add(trendDetectionLengthLabel, trendDetectionLengthConstraints);

        trendDetectionLengthSpinner.setModel(new SpinnerNumberModel(2, 1, 5, 1));
        GridBagConstraints trendDetectionLengthSpinnerConstraints = new GridBagConstraints();
        trendDetectionLengthSpinnerConstraints.anchor = GridBagConstraints.EAST;
        trendDetectionLengthSpinnerConstraints.fill = GridBagConstraints.HORIZONTAL;
        trendDetectionLengthSpinnerConstraints.insets = new Insets(0, 0, 5, 0);
        trendDetectionLengthSpinnerConstraints.gridx = 1;
        trendDetectionLengthSpinnerConstraints.gridy = 1;
        trendDetectionLengthSpinner.addChangeListener(e -> callback.onSettingsUpdate(getCurrentWeisWaveSettings()));

        add(trendDetectionLengthSpinner, trendDetectionLengthSpinnerConstraints);
    }

    private WeisWaveSettings getCurrentWeisWaveSettings() {
        int trendDetectionLength = (int) trendDetectionLengthSpinner.getValue();
        long seconds = timeIntervals[timeIntervalsComboBox.getSelectedIndex()].seconds();

        return new WeisWaveSettings(trendDetectionLength, seconds);
    }
}
