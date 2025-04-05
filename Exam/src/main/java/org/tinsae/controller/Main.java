package org.tinsae.controller;

public class Main {
    public static void main(String[] args) {
        Controller controller = new Controller("controller");
        controller.connect();
        controller.init();
        controller.disconnect();
    }
}
