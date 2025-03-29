package org.tinsae.recorder;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class DataRecorder extends CallBack {

    private int room_temperature;
    int recorded_sensors = 2;//hardcoded in this case
    private String ventil_state;
    DataRecorder(String tag){
        super(tag);
    }

    @Override
    protected void subscribe(String topic) {
        String new_topic = "sensors/#";
        super.subscribe(new_topic);
    }

    /**
     * @param msg msg
     * @param topic topic
     * @return boolean
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        if(msg.toString().equalsIgnoreCase(EXIT_FLAG)){
            recorded_sensors--;
        }
        return recorded_sensors == 0;
    }

    /**
     * @param msg  message
     * @param topic topic
     */
    @Override
    protected void task(MqttMessage msg, String topic) {
        String sensor = topic.split("/")[1];
        if(sensor.equalsIgnoreCase("temperature")){
            try{
                setRoom_temperature(Integer.parseInt(msg.toString()));

            }catch (NumberFormatException e){
                System.err.println(TAG+ " : error parsing temperature "+e.getMessage());
            }
        }
        if(sensor.equalsIgnoreCase("ventil")){
            setVentil_state(msg.toString());
        }
    }

    /**
     *
     */
    @Override
    protected void report() {
        try{
            Thread.sleep(1000);
            System.out.println(TAG+" : recorded room temperature = "+this.room_temperature+" and Ventil state = "+this.ventil_state);
        }catch (InterruptedException e){
            System.err.println(TAG+" : Thread interrupted "+e.getMessage());
        }
    }

    public void setRoom_temperature(int room_temperature) {
        this.room_temperature = room_temperature;
    }

    public void setVentil_state(String ventil_state) {
        this.ventil_state = ventil_state;
    }
}
