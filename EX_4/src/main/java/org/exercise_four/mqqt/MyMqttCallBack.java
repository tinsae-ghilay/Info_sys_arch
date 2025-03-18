package org.exercise_four.mqqt;

// Needed for client id generation
import java.util.UUID;
// Needed for MQTT
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import static java.lang.Thread.sleep;


public abstract class MyMqttCallBack implements MqttCallback {
    // TAG, String used as part of topic and identification of callback on logs
    protected final String TAG;
    // url of mosquitto
    private final static  String url = "tcp://localhost:1883";
    // pseudo unique ID
    protected final String ID;
    // Client object needed for connecting to the MQTT broker
    private MqttClient client;
    // Flag that allows us to exit the application
    private boolean shutdownFlag = false;
    // separator to split messages in to parts
    public final String SEPARATOR = "/";
    public static final String EXIT_FLAG = "Goodbye";

    public MyMqttCallBack(String tag){
        this.TAG = tag;
        this.ID = UUID.randomUUID().toString();
        try {
            client = new MqttClient(url,ID);
            System.out.println(TAG+" started with Id : "+ID);
        } catch (MqttException e) {
            System.err.println(TAG+" : Error defining client -> "+e.getMessage());
        }

    }

    /**
     * calls connect() on client object
     * sets this class(object) as clients callback
     * subscribes to mqtt
     */
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
            // specific topic
            client.subscribe(topic);
            System.out.println(TAG+" : Subscribed for topic \""+topic+"\"");
        } catch (MqttException e) {
            System.err.println(TAG+" : Error subscribing to topic \""+topic+"\" : Reason -> "+e.getMessage());
        }
    };


    // Template methods
    // boolean, if received message should trigger shutdown
    protected abstract boolean isExitMessage(MqttMessage msg,String topic);
    // what task will a subscriber do? workers work, coordinators coordinate.
    protected abstract void task(MqttMessage msg, String topic);

    // task to be done before closing?
    protected void finalise(){
        System.out.println(TAG+" : bye!!");
    }

    // disconnecting mqqt client and freeing resource
    public void disconnect(){
        try{
            client.disconnect();
            client.close();
        }catch (MqttException e){
            System.err.println(TAG+" : Error disconnecting client -> "+e.getMessage());
        }
    }

    // publish a message for a topic
    public void publish(String message, String topic){
        try {
            MqttMessage msg = new MqttMessage(message.getBytes());
            client.publish(topic,msg);
            //System.out.println(TAG+" : Published topic \""+msg+"\" to "+topic);
        } catch (MqttException e) {
            System.err.println(TAG+" : Error sending message -> "+e.getMessage());
            // we should probably end program here
            shutdownFlag = true;
        }
    }


    // continuous loop.
    public final void init(){
        while(!shutdownFlag){
            try{
                sleep(500);
            } catch (InterruptedException e) {
                System.err.println("Interrupted !!");
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
     * on message received, we call an inherited task() method
     * which does a task depending on the inheriting object
     * @see org.exercise_four.worker.Worker
     * @see org.exercise_four.coordinator.Coordinator
     * @param s Topic
     * @param mqttMessage Message
     */
    @Override
    public void messageArrived(String s, MqttMessage mqttMessage){
        if(isExitMessage(mqttMessage,s) && !shutdownFlag){
            System.out.print(TAG+" : received an exit flag: ");
            this.shutdownFlag = true;
        }else{
            task(mqttMessage, s);
        }
    }

    /**
     * @param iMqttDeliveryToken : ahm!
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // what do I need her? nothing for the moment
    }



}
