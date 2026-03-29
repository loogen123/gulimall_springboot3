package com.lg.gulimail.product.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gulimail.ai")
public class AiConfig {

    private static final Logger logger = LoggerFactory.getLogger(AiConfig.class);

    private boolean enabled = true;
    private String model = "gpt-3.5-turbo";
    private double temperature = 0.7;
    private int maxTokens = 1000;
    private int timeoutSeconds = 30;
    private int maxRetries = 3;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public String toString() {
        return "AiConfig{" +
                "enabled=" + enabled +
                ", model='" + model + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", timeoutSeconds=" + timeoutSeconds +
                ", maxRetries=" + maxRetries +
                '}';
    }
}