package org.exercise_four.coordinator;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.exercise_four.mqqt.MyMqttCallBack;

import java.util.ArrayList;

public class Coordinator  extends MyMqttCallBack {

    private int total_darts = 1000000;
    private int darts_thrown = 0;
    private int total_hits = 0;
    // how big this is, determines how fine/coarse grained a parallelism can be
    // depending on whether we have parallelism or not. we have to do it accordingly to minimise load imbalance
    private static final int darts_per_worker = 1000;
    // list of workers that get registered here,
    // program ends when we get an MqttMessage with 0 in it and this list is empty
    // when a worker sends 0, that worker will be unregistered(removed from this list) and then this list will be checked.
    ArrayList<String> workers = new ArrayList<>();

    public Coordinator(String tag) {
        super(tag);
    }

    @Override
    protected void subscribe(String topic) {
        super.subscribe(topic+"/+");
    }

    // This method will be called by parent class (hollywood treatment)
    /**
     * @param msg MqttMessage
     * @return boolean
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg, String topic) {
        // process message
        String[] task_msg = split(msg.toString());
        // if worker is trying to register, lets register it first
        if(task_msg[0].equalsIgnoreCase("worker")){
            workers.add(topic);
            String worker_id = split(topic)[1];
            System.out.println(TAG+" : added worker with id \""+worker_id+"\" to registry");
            // it's not an exit message
            return false;
        }
        //System.out.println("Coordinator received this message : "+task_msg[0]);
        /* if the message received from worker is 0,
         * it means it's the final message from that worker
         * so we remove it from the list
         */
        boolean receivedZero = task_msg[0].equals("0");

        if(receivedZero && workers.remove(topic)){
            String worker_id = split(topic)[1];
            System.out.println(TAG+" : Worker with id: \""+worker_id+"\" logged out");
        }
        if(!receivedZero){
            /* we are checking if response from worker is an exit message or not,
             * It means we are sure that the worker has thrown the darts and has sent hits back
             * and if worker is not logging out, it sends the hits along with thrown darts.
             * so we add what we passed to worker as thrown.
             */
            setDarts_thrown(Integer.parseInt(task_msg[1]));
        }
        // if all workers have been removed, it means work is done
        // so coordinator can also close
        return workers.isEmpty();
    }

    // This method will be called by parent class (hollywood treatment)
    /**
     * @param msg : message that will be processed
     */
    @Override
    public void task(MqttMessage msg, String topic) {
        String[] task_msg = split(msg.toString());

        try{
            // if we got hits from worker, add them to total hits
            if(!task_msg[0].equalsIgnoreCase("worker")){
                int hits = Integer.parseInt(task_msg[0]);
                total_hits+= hits;
                //System.out.println(hits+"  Added to total hits!");
            }
            if(task_msg[0].equals("0")){
                return;
            }
            int darts_to_throw = getDarts();
            // we send back a new task (darts) to that worker
            String worker_id = split(topic)[1];
            publish(String.valueOf(darts_to_throw), "worker/"+worker_id);

            //System.out.println("message received from "+task_msg[1]+" by "+TAG+" and can be redirected: Answer : "+new_msg);

        // we also have catch possible exceptions
        }catch(NumberFormatException | IndexOutOfBoundsException e){
            System.err.println(TAG+" : Error doing task "+e.getMessage());
            // we probably should disconnect and close here
            disconnect();
        }
    }

    /**
     * final task to be done before program shuts down
     */
    @Override
    public void finalise() {
        // add final darts to total thrown darts
        double pi = 4.0 * ((double) total_hits / (darts_thrown));
        System.out.println("\tDarts thrown \t: "+darts_thrown);
        System.out.println("\tCalculated pi \t: "+pi);
    }

    /**
     * gets number of darts a worker has to throw
     * reduces darts assigned to worker from total darts
     * @return  int darts to be thrown
     */
    private int getDarts(){
        int darts = 0;
        if(total_darts >= darts_per_worker){
            total_darts-= darts_per_worker;
            darts = darts_per_worker;
        }else if(total_darts != 0){
            darts = total_darts;
            total_darts = 0;
        }
        return darts;
    }

    /**
     * sets total darts thrown, synchronised in case things get done in threads
     * @param thrown int number of darts thrown by a worker
     */
    private void setDarts_thrown(int thrown){
        darts_thrown+=thrown;
        //System.out.println("Thrown darts incremented to : "+darts_thrown);
    }
}

