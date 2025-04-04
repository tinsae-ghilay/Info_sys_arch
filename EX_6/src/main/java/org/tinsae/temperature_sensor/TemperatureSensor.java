package org.tinsae.temperature_sensor;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

import java.util.Random;

import static java.lang.Thread.sleep;

public class TemperatureSensor extends CallBack {

    private final String BROADCAST_CHANNEL;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public TemperatureSensor(String tag) {
        super(tag);
        BROADCAST_CHANNEL ="sensors/"+TAG+"/"+ID;
    }

    @Override
    // temperature sensor is a worker. hence it only needs to check if it received an exit message.
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        return msg.toString().equalsIgnoreCase(EXIT_FLAG);
    }

    @Override
    protected void task(MqttMessage msg, String topic) {
    }

    @Override
    protected void subscribe(String topic) {
        String new_topic = "coordinator/all";
        super.subscribe(new_topic);
    }

    @Override
    protected void report() {
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            this.disconnect();
            System.err.println(TAG+" : Timer interrupted");
        }

        Random rand = new Random();
        int temp = rand.nextInt(10,50);
        publish(String.valueOf(temp),BROADCAST_CHANNEL);
        System.out.println(TAG+" : reported a temperature of "+temp);
   }

    @Override
    protected void finalise() {
        super.finalise();
        // let's tell any process that is listening
        publish(EXIT_FLAG,BROADCAST_CHANNEL);
    }
}
