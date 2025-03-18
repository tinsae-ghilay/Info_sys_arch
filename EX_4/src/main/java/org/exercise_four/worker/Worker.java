package org.exercise_four.worker;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.exercise_four.mqqt.MyMqttCallBack;

import java.util.concurrent.ThreadLocalRandom;

public class Worker extends MyMqttCallBack{


    private final String BROADCAST_TOPIC;
    public Worker(String tag) {

        super(tag);
        BROADCAST_TOPIC = "coordinator/"+ID;
    }

    /**
     * @param topic String topic the worker subscribes
     */
    @Override
    protected void subscribe(String topic) {
        // workers subscribe with tag and ID
        super.subscribe(topic+"/"+ID);
        // worker can register itself with coordinator here
        // sending message payload like "worker"/ID helps us maintain track of workers lifecycle
        // coordinator has to maintain a list of workers, to ensure that all workers have logged out before it does
        publish(TAG+SEPARATOR+ID, BROADCAST_TOPIC);

    }

    /**
     * @param msg MqttMessage
     * @return Boolean
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        //
        boolean should_exit = msg.toString().equals("0");
        if(should_exit){ // received exit flag
            publish(EXIT_FLAG,BROADCAST_TOPIC);
        }
        return should_exit;
    }

    // calculate dart hits
    // send them back to broker
    private int calculateHits(long darts) {
        int hits = 0;
        // random generator
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        // simulate dart throw, by getting random coordinates
        for (int i = 0; i < darts; i++) {
            double x = rand.nextDouble(-1.0, 1.0);
            double y = rand.nextDouble(-1.0, 1.0);

            if (Math.hypot(x, y) <= 1) { // coordinates are within circle
                hits++;
            }
        }
        return hits;
    }

    @Override
    public void task( MqttMessage msg, String topic) {
        try{
            int darts = Integer.parseInt(msg.toString());
            int hits = calculateHits(darts);
            // we send hits/darts (hits per darts) back to coordinator
            publish(hits+SEPARATOR+darts, BROADCAST_TOPIC);
        }catch(NumberFormatException e){
            // we might need to close
            System.err.println(TAG+ " : "+e.getMessage()+" -> "+ msg);
        }
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        super.messageArrived(s, mqttMessage);
    }

    @Override
    protected void finalise() {
        System.out.println("finalising");
        super.finalise();
    }
}
