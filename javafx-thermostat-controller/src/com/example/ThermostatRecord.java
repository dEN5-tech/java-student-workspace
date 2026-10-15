package com.example;

/**
 * Класс данных одной записи телеметрии термостата (Модель данных для TableView).
 */
public class ThermostatRecord {
    private String time;
    private double temperature;
    private double threshold;
    private String mode;
    private String status;

    public ThermostatRecord(String time, double temperature, double threshold, String mode, String status) {
        this.time = time;
        this.temperature = temperature;
        this.threshold = threshold;
        this.mode = mode;
        this.status = status;
    }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
