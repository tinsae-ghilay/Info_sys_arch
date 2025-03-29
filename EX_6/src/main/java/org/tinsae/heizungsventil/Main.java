package org.tinsae.heizungsventil;

public class Main {
    public static void main(String[] args) {
        Heizungsventil ventil = new Heizungsventil("Ventil");
        ventil.connect();
        ventil.init();
        ventil.disconnect();
    }
}
