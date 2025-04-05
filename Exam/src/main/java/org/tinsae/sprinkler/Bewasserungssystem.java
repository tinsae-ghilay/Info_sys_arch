package org.tinsae.sprinkler;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class Bewasserungssystem extends CallBack {
    private final String BROADCAST_CHANNEL;
    private String command;

    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public Bewasserungssystem(String tag) {
        super(tag);
        BROADCAST_CHANNEL = "sectorA/"+TAG+"/ID";
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
        // again, I want to cascade this
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
        String new_command = msg.toString();
        if(!new_command.equalsIgnoreCase(command)){
            System.out.println(TAG+" : set to "+new_command);
            setCommand(new_command);
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
        // nothing to report here as well
    }

    @Override
    protected void subscribe(String topic) {
        String new_topic = "sectorA/controller/command/+";
        super.subscribe(new_topic);
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    protected void finalise() {
        publish(EXIT_FLAG, BROADCAST_CHANNEL);
        super.finalise();
    }
}
