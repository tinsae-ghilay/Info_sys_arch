package org.tinsae.recorder;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.tinsae.callback.CallBack;

public class DataRecorder extends CallBack {

    private int room_temperature;
    private int recorded_sensors = 2;//hardcoded in this case
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
    // recorder is logging from multiple sensors (2 in this case)
    // so if it gets an exit message, it means one sensor left. so reduce one sensor.
    // because exit message here means a sensor exited. not that this sensor should exit.
    // if no sensors are active i.e. sensors == 0. then recorder can exit
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
    // when recorder receive a message, it has to filter it based on topic
    // and update the appropriate value
    // in this case either temperature or Ventil status
    protected void task(MqttMessage msg, String topic) {
        String sensor = topic.split("/")[1];
        if(sensor.equalsIgnoreCase("temperature")){ // message is from temperature sensor
            try{
                setRoom_temperature(Integer.parseInt(msg.toString()));

            }catch (NumberFormatException e){ // we got some value but it is not a number
                System.err.println(TAG+ " : error parsing temperature "+e.getMessage());
            }
        }
        if(sensor.equalsIgnoreCase("ventil")){ // message is about ventil state
            setVentil_state(msg.toString());
        }
    }

    /**
     *
     */
    @Override
    // recorder has to log temperature and ventil state.
    // this is done at a separate interval regardless of messages received
    protected void report() {
        try{
            Thread.sleep(1000);
            if(ventil_state != null && room_temperature != 0){ // values have been set
                System.out.println(TAG+" : recorded room temperature = "+this.room_temperature+" and Ventil state = "+this.ventil_state);
            }
        }catch (InterruptedException e){
            System.err.println(TAG+" : Thread interrupted "+e.getMessage());
        }
    }

    // setter for room temperature
    public void setRoom_temperature(int room_temperature) {
        this.room_temperature = room_temperature;
    }

    // setter for ventil state
    public void setVentil_state(String ventil_state) {
        this.ventil_state = ventil_state;
    }
}
