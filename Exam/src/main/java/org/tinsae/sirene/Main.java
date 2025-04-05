package org.tinsae.sirene;

public class Main {
    public static void main(String[] args) {
        Sirene sirene = new Sirene("siren");
        sirene.connect();
        sirene.init();
        sirene.disconnect();
    }
}
