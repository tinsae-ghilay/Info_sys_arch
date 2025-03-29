package org.tinsae.heizungsventil;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class Heizungsventil extends CallBack {

    private final String BROADCAST_CHANNEL;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public Heizungsventil(String tag) {
        super(tag);
        BROADCAST_CHANNEL ="sensors/"+TAG+"/"+ID;
    }

    /**
     * @param msg message
     * @param topic topic
     * @return true if exit message is received
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg, String topic) {

        return msg.toString().equalsIgnoreCase(EXIT_FLAG);
    }

    /**
     * @param msg message
     * @param topic message topic
     */
    @Override
    protected void task(MqttMessage msg, String topic) {
        try{
            switch (msg.toString()){
                case "ON":
                    System.out.println(TAG+ " : State ON");
                    publish("ON",BROADCAST_CHANNEL);
                    break;
                case "OFF":
                    System.out.println(TAG+ " : State OFF");
                    publish("OFF", BROADCAST_CHANNEL);
                    break;
                default:
                    System.out.println(TAG+ " unknown message received");
            }
        } catch (NumberFormatException e) {
            System.err.println(TAG+" : Error converting String temp to int"+e.getMessage());
        }
    }

    /**
     *
     */
    @Override
    protected void report() {
    }

    @Override
    protected void subscribe(String topic) {
        String new_topic = "coordinator/#";
        super.subscribe(new_topic);
    }

    @Override
    protected void finalise() {
        super.finalise();
        publish(EXIT_FLAG,BROADCAST_CHANNEL);
    }
}
