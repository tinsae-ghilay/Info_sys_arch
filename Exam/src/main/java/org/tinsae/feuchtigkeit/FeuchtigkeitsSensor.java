package org.tinsae.feuchtigkeit;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

import java.util.Random;

import static java.lang.Thread.sleep;

public class FeuchtigkeitsSensor extends CallBack {

    private final String BROADCAST_CHANNEL;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public FeuchtigkeitsSensor(String tag) {
        super(tag);
        BROADCAST_CHANNEL = "sensors/"+tag+ID;
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
    }

    /**
     * A worker may need to report events at a certain interval
     * this job can be implemented here.
     * example a temperature sensor can measure temperature every 5 minutes
     * and report it to broker
     */
    @Override
    protected void report() {
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            this.disconnect();
            System.err.println(TAG+" : Timer interrupted");
        }
        // we get temperature value (randomly generated)
        Random rand = new Random();
        // let's say temperature is between 10 and 50.
        int temp = rand.nextInt(0,80);
        // and broadcast temperature
        publish(String.valueOf(temp),BROADCAST_CHANNEL);
        System.out.println(TAG+" : reported a Humidity level of "+temp);
    }


    @Override
    protected void subscribe(String topic) {

        String new_topic = "sectorA/controller/all";
        super.subscribe(new_topic);
    }
}
