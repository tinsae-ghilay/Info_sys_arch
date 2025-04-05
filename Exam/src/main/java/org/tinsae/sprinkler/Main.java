package org.tinsae.sprinkler;

public class Main {
    public static void main(String[] args) {
        Bewasserungssystem sprinkler = new Bewasserungssystem("sprinkler");
        sprinkler.connect();
        sprinkler.init();
        sprinkler.disconnect();
    }
}
