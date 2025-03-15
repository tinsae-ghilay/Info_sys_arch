package org.exercise_four.coordinator;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.exercise_four.mqqt.MyMqttCallBack;

import java.util.ArrayList;

public class Coordinator  extends MyMqttCallBack {

    private int total_darts = 1000235;
    private int darts_thrown = 0;
    private int total_hits = 0;
    private static final int darts_per_worker = 1000;
    // darts passed to worker
    private int immediate_darts = 0;
    ArrayList<String> workers = new ArrayList<>();


    public Coordinator(String tag) {
        super(tag);
    }


    // This method will be called by parent class (hollywood treatment)
    /**
     * @param msg MqttMessage
     * @return boolean
     */
    @Override
    protected boolean isExitMessage(MqttMessage msg) {
        // we are only sure that the darts were thrown, only after we received response from worker
        // but I am not sure if this accounts for when we have multiple workers
        // for that, we might also have to include that number in MqttMessage from worker
        // but for the time being we increament thrown darts by the previously sent amount of darts
        setDarts_thrown(immediate_darts);
        String[] task_msg = splitMessage(msg);
        //System.out.println("Coordinator received this message : "+task_msg[0]);
        // if the message received from worker is 0,
        // it means it's the final message from that worker
        // so we remove it from the list
        boolean receivedZero = task_msg[0].equals("0");
        if(receivedZero && workers.remove(task_msg[1])){
            System.out.println(getTAG()+" : Worker with id: \""+task_msg[1]+"\" logged out");
        }
        // if all workers have been removed, it means work is done
        // so coordinator can also close
        return receivedZero && workers.isEmpty();
    }


    // This method will be called by parent class (hollywood treatment)
    /**
     * @param msg : message that will be processed
     */
    @Override
    public void task(MqttMessage msg) {
        String[] task_msg = splitMessage(msg);

        try{
            // if worker is trying to register, lets just add that worker
            if(task_msg[0].equalsIgnoreCase("worker")){
                workers.add(task_msg[1]);
                int darts_to_throw = getDarts();
                System.out.println(getTAG()+" : added worker with id \""+task_msg[1]+"\" to registry");
                // and give it a task
                MqttMessage new_msg = new MqttMessage(String.valueOf(darts_to_throw).getBytes());
                publish(new_msg, "worker/"+task_msg[1]);
                return;
            }

            // let`s add hits obtained from worker to total hits
            int hits = Integer.parseInt(task_msg[0]);
            total_hits+= hits;
            int darts_to_throw = getDarts();
            // we send back a new task (darts) to that worker
            MqttMessage new_msg = new MqttMessage(String.valueOf(darts_to_throw).getBytes());
            publish(new_msg, "worker/"+task_msg[1]);

            //System.out.println("message received from "+task_msg[1]+" by "+getTAG()+" and can be redirected: Answer : "+new_msg);

        // we also have catch possible exceptions
        }catch(NumberFormatException | IndexOutOfBoundsException e){
            System.err.println(getTAG()+" : Error doing task "+e.getMessage());
            // we probably should disconnect and close here
            disconnect();
        }
    }

    /**
     * final task to be done before program shuts down
     */
    @Override
    public void finalise() {
        super.finalise();
        // add final darts to total thrown darts
        double pi = 4.0 * ((double) total_hits / (darts_thrown));
        System.out.println("\tDarts thrown \t: "+darts_thrown);
        System.out.println("\tCalculated pi \t: "+pi);
    }

    private String[] splitMessage(MqttMessage msg){
        return msg.toString().split(" ");
    }

    /**
     * gets number of darts a worker has to throw
     * reduces darts assigned to worker from total darts
     * @return  int darts to be thrown
     */
    // ideally, this may be better done synchronously
    // because we may receive multiple requests at the same time if workers are many
    synchronized private int getDarts(){
        int darts = 0;
        if(total_darts >= darts_per_worker){
            total_darts-= darts_per_worker;
            darts = darts_per_worker;
        }else if(total_darts != 0){
            darts = total_darts;
            total_darts = 0;
        }
        immediate_darts = darts;
        return darts;
    }

    /**
     * sets total darts thrown, synchronised in case things get done in threads
     * @param thrown int number of darts thrown by a worker
     */
    synchronized private void setDarts_thrown(int thrown){
        darts_thrown+=thrown;
    }

}

