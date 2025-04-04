package org.tinsae.controller;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class Controller extends CallBack {

    private int THRESH_HOLD_TEMP = 20;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public Controller(String tag) {
        super(tag);
    }

    /**
     * @param msg message
     * @param topic topic
     * @return boolean
     */
    @Override

    // controller is only interested in temperature sensor
    // as it only responds to message received from it.
    // and if it receives an exit message form it. it exits
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        return msg.toString().equalsIgnoreCase(EXIT_FLAG);
    }

    @Override
    // subscribe to temperature sensor
    protected void subscribe(String topic) {
        String new_topic = "sensors/temperature/#";
        super.subscribe(new_topic);
    }

    /**
     * @param msg message
     * @param topic topic
     */
    @Override
    // controller has a task
    // if it receives a temperature values, it has to pass command to ventil based on threshold.
    protected void task(MqttMessage msg, String topic) {
        String channel = topic.split("/")[1];
        try{
            // we can change threshold by sending a message to threshold
            if(channel.equalsIgnoreCase("threshold")){
                int temp = Integer.parseInt(msg.toString());
                // we only need to change it if it ain't the same
                if(temp!= THRESH_HOLD_TEMP){
                    THRESH_HOLD_TEMP = temp;
                    System.out.println(TAG+" Threshold temperature changed to "+temp);
                }
            }else if (channel.equalsIgnoreCase("temperature")){ // we received temperature
                int temp = Integer.parseInt(msg.toString());
                String state = temp < THRESH_HOLD_TEMP ? "ON" : "OFF";
                System.out.println(TAG + " : Temperature is " + temp + " hence ventil should turn " + state);
                publish(state, "coordinator/ventil");
            }
        } catch (NumberFormatException e) {
            System.err.println(TAG+" : Invalid message received "+e.getMessage());
        }
    }

    /**
     *
     */
    @Override
    // controller has nothing to report to console. on an infinite loop
    protected void report() {

    }
}
