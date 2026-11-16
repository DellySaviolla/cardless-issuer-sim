package com.example.issuersim.service;

import com.example.issuersim.model.SimulatorSettings;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private SimulatorSettings settings = new SimulatorSettings();

    public SimulatorSettings getSettings() { return settings; }
    public void updateSettings(SimulatorSettings newSettings) { this.settings = newSettings; }
}