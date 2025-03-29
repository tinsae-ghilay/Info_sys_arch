package org.tinsae.recorder;

public class Main {
    public static void main(String[] args) {
        DataRecorder recorder = new DataRecorder("recorder");
        recorder.connect();
        recorder.init();
        recorder.disconnect();
    }
}
