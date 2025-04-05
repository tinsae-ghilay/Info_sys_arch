package org.tinsae.feuchtigkeit;

public class Main {

    public static void main(String[] args) {
        FeuchtigkeitsSensor sensor = new FeuchtigkeitsSensor("feuchtigkeit");
        sensor.connect();
        sensor.init();
        sensor.disconnect();
    }
}
