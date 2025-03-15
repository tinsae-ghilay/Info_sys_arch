package org.exercise_four.coordinator;
public class Main {
    public static void main(String[] args) {

        Coordinator coordinator = new Coordinator("coordinator");
        coordinator.connect();
        coordinator.init();
        coordinator.disconnect();

    }
}