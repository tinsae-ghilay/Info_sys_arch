package org.tinsae.sirene;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

import static java.lang.Thread.sleep;

public class Sirene extends CallBack {
    private int threshold = 10;
    private String state;
    /**
     * Constructor
     *
     * @param tag String used to identify the callback (coordinator , worker or any other name that we want to give it)
     *            we also use this to identify objects in debug print-outs
     */
    public Sirene(String tag) {
        super(tag);
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
        try{
            int humidity = Integer.parseInt(msg.toString());
            // we set command conditionally based on humidity in relation to threshold
            String status = humidity < threshold? "ALARM" : "OK";
            setState(status);

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
        while(state.equalsIgnoreCase("WARNING")){
            System.err.println(TAG+" : WARNING !! Humidity too low. turn on sprinklers immediately");
            try{
                sleep(500);
            } catch (InterruptedException e) {
                System.err.println(TAG+" : timer interrupted"+e.getMessage());
            }
        }
    }

    @Override
    protected void subscribe(String topic) {
        // controller has to listen to feuchtigkeitssensor
        // so we subscribe to it
        String new_topic = "sectorA/sensors/#";
        super.subscribe(new_topic);
    }

    public void setState(String status) {
        this.state = status;
    }
}
