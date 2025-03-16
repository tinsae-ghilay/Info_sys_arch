package org.exercise_four.worker;
public class Main {
    public static void main(String[] args) {

        // create worker with tag
        Worker worker = new Worker("worker");
        worker.connect();
        worker.init();
        worker.disconnect();
    }
}