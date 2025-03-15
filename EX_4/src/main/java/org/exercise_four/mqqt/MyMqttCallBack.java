package org.exercise_four.mqqt;

// Needed for client id generation
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

// Needed for MQTT
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static java.lang.Thread.sleep;


public abstract class MyMqttCallBack implements MqttCallback {
    private final String TAG;
    private final static  String url = "tcp://localhost:1883";
    private final String id;
    // Client object needed for connecting to the MQTT broker
    private MqttClient client;

    // Flag that allows us to exit the application
    private boolean shutdownFlag = false;

    public MyMqttCallBack(String tag){
        this.TAG = tag;
        this.id = UUID.randomUUID().toString();
        try {
            client = new MqttClient(url,id);
            System.out.println(TAG+" started with Id : "+id);
        } catch (MqttException e) {
            System.err.println(TAG+" : Error defining client -> "+e.getMessage());
        }

    }

    public void connect() {
        try{
            client.connect();
            client.setCallback(this);
            subscribe(TAG);
            System.out.println(TAG+" : Connected!");
        } catch (MqttException e) {
            System.out.println(TAG+" : "+e.getMessage());
        }
    }

    // abstract methods , the hollywood treatments
    // subscriber subscribes to a topic
    protected void subscribe(String topic){
        try{
            client.subscribe(topic);
            System.out.println(TAG+" : Subscribed for topic \""+topic+"\"");
        } catch (MqttException e) {
            System.err.println(TAG+" : Error subscribing to topic \""+topic+"\" : Reason -> "+e.getMessage());
        }
    };


    // boolean, if received message should trigger shutdown
    protected abstract boolean isExitMessage(MqttMessage msg);
    // what task will a subscriber do? workers work, coordinators coordinate.
    protected abstract void task(MqttMessage msg);

    // task to be done before closing?
    protected void finalise(){
        System.out.println(TAG+" exiting:");
    }
    // disconnecting mqqt client and freeing resource
    public void disconnect(){
        try{
            client.disconnect();
            client.close();
            System.out.println(TAG+" : Disconnected!");
        }catch (MqttException e){
            System.err.println(TAG+" : Error disconnecting client -> "+e.getMessage());
        }
    }

    // publish a message for a topic
    public void publish(MqttMessage msg, String topic){
        try {
            client.publish(topic,msg);
            //System.out.println(TAG+" : Published topic \""+msg+"\" to "+topic);
        } catch (MqttException e) {
            System.err.println(TAG+" : Error sending message -> "+e.getMessage());
            // we should probably end program here
            shutdownFlag = true;
        }
    }

    // continuous loop.
    public void init(){
        while(!shutdownFlag){
            try{
                sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        finalise();
    }

    /**
     * @param throwable reason
     */
    @Override
    public void connectionLost(Throwable throwable) {
        // if connection is lost, we have to close client
        // setting shutDownFlag to true can do the task
        System.err.println(TAG+" : Disconnected, Reason : "+throwable.getMessage());
        shutdownFlag = true;
    }

    /**
     * @param s Topic
     * @param mqttMessage Message
     */
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage){
        //System.out.println("Message received ");
        if(isExitMessage(mqttMessage)){
            System.out.println(TAG+" : received an exit flag: exiting");
            this.shutdownFlag = true;
        }else{
            task(mqttMessage);
        }
    }

    /**
     * @param iMqttDeliveryToken : ahm!
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // what do I need her? nothing for the moment
    }


    public String getTAG() {
        return TAG;
    }

    public String getId() {
        return id;
    }
}
