package org.tinsae.temperature_sensor;

public class Main {

    public static void main(String[] args) {
        TemperatureSensor sensor = new TemperatureSensor("temperature");
        sensor.connect();
        sensor.init();
        sensor.disconnect();
    }
}
