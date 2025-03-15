package org.exercise_four.worker;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.exercise_four.mqqt.MyMqttCallBack;

import java.util.concurrent.ThreadLocalRandom;

public class Worker extends MyMqttCallBack{


    public Worker(String tag) {
        super(tag);
    }

    /**
     *
     */
    @Override
    protected void subscribe(String topic) {
        // workers subscribe with tag and ID
        super.subscribe(topic+"/"+getId());
        // worker can register itself with coordinator here
        // this helps us be able to send targeted message to specific worker objects
        publish(new MqttMessage((getTAG()+" "+getId()).getBytes()), "coordinator");
    }

    /**
     * @param msg MqttMessage
     * @return Boolean
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg) {
        return msg.toString().equalsIgnoreCase("0");
    }

    // calculate dart hits
    // send them back to broker
    private int calculateHits(long darts) {
        int hits = 0;
        // random generator
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // simulate dart throw, by getting random coordinates
        for (int i = 0; i < darts; i++) {
            double x = rng.nextDouble(-1.0, 1.0);
            double y = rng.nextDouble(-1.0, 1.0);

            if (Math.hypot(x, y) <= 1) { // coordinates are within circle
                hits++;
            }
        }
        return hits;
    }

    @Override
    public void task( MqttMessage msg) {
        try{
            int darts = Integer.parseInt(msg.toString());
            int hits = calculateHits(darts);
            // we send id here to tell coordinator , where the message is coming from (PS: the encoding plays a role.)
            MqttMessage new_msg = new MqttMessage((hits+" "+getId()).getBytes());
            publish(new_msg,"coordinator");
        }catch(NumberFormatException e){
            // we might need to close
            System.err.println(getTAG()+ " : "+e.getMessage()+" -> "+ msg);
        }
    }

    /**
     *
     */
    @Override
    public void finalise() {
        super.finalise();
        // worker should notify that it is closing
        publish(new MqttMessage(("0 "+getId()).getBytes()),"coordinator");
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) {
        super.messageArrived(s, mqttMessage);
    }
}
