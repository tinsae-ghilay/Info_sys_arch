package org.tinsae.controller;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class Controller extends CallBack {

    private final String BROADCAST_CHANNEL;
    private int threshold = 30;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public Controller(String tag) {
        super(tag);
        BROADCAST_CHANNEL = "sectorA/"+tag+"/command/"+ID;
    }

    @Override
    protected void subscribe(String topic) {
        // controller has to listen to feuchtigkeitssensor
        // so we subscribe to it
        String new_topic = "sectorA/sensors/#";
        super.subscribe(new_topic);
    }

    /**
     * checks if message received is an end of task message
     *
     * @param msg   MqttMessage
     * @param topic String topic of the message
     * @return boolean true if message contains EXIT_FLAG
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        // I plan to make this a cascading exit. so, if feuchtigkeit sensor receives an exit message
        // all exit as well.
        return msg.toString().equalsIgnoreCase(EXIT_FLAG);
    }

    /**
     * what task will a subscriber do? workers work, coordinators coordinate.
     *
     * @param msg   MqttMessage for the client to work on
     * @param topic topic of the message
     */
    @Override
    protected void task(MqttMessage msg, String topic) {
        try{
            int humidity = Integer.parseInt(msg.toString());
            // we set command conditionally based on humidity in relation to threshold
            String command = humidity < threshold? "ON" : "OFF";
            // and publish command
            publish(command, BROADCAST_CHANNEL);
            System.out.println(TAG+" : sent a command -> "+command);
        } catch (NumberFormatException e) {
            System.err.println(TAG+" : Invalid humidity value received");
        }

    }

    /**
     * A worker may need to report events at a certain interval
     * this job can be implemented here.
     * example a temperature sensor can measure temperature every 5 minutes
     * and report it to broker
     */
    @Override
    protected void report() {
        // nothing to report, only act when message is received
    }

}
