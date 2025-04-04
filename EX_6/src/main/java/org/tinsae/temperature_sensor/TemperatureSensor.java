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
    // task is executed when callback receives message
    // in this case temperature sensor doesn't do any task if it receives a message
    // and the only message it may receive is an EXIT message
    protected void task(MqttMessage msg, String topic) {
    }

    @Override
    // to listen to exit command from coordinator. sensor needs to subscribe to coordinator
    protected void subscribe(String topic) {
        String new_topic = "coordinator/all";
        super.subscribe(new_topic);
    }

    @Override
    // sensor measures temperature and publishes this to broker for any process that may be listening
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
        int temp = rand.nextInt(10,50);
        // and broadcast temperature
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
