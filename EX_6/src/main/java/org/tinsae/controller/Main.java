package org.tinsae.controller;

public class Main {
    public static void main(String[] args) {
        Controller ctrl = new Controller("coordinator");
        ctrl.connect();
        ctrl.init();
        ctrl.disconnect();
    }
}
