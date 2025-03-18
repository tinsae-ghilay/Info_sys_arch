package org.exercise_four.coordinator;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.exercise_four.mqqt.MyMqttCallBack;

import java.util.ArrayList;

import static java.lang.Thread.dumpStack;
import static java.lang.Thread.sleep;

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
    boolean isCleaning =  false;

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
        String[] task_msg = msg.toString().split(SEPARATOR);
        // if worker is trying to register, lets register it first
        if(task_msg[0].equalsIgnoreCase("worker") && ! workers.contains(topic)){
            workers.add(topic);
            String worker_id = topic.split("/")[1];;
            System.out.println(TAG+" : added worker with id \""+worker_id+"\" to registry");
            // it's not an exit message
            return false;
        }
        //System.out.println("Coordinator received this message : "+task_msg[0]);
        /* if the message received from worker is Goodbye,
         * it means it's the final message from that worker
         * so we remove it from the list
         */
        boolean isExitFlag = task_msg[0].equals(EXIT_FLAG);

        if(isExitFlag && workers.remove(topic)){
            String worker_id = topic.split("/")[1];
            System.out.println(TAG+" : Worker with id: \""+worker_id+"\" logged out");
        }
        if(!isExitFlag){
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
        String[] task_msg = msg.toString().split(SEPARATOR);
        String worker_id = topic.split("/")[1];
        try{
            // if we got hits from worker, add them to total hits
            if(!task_msg[0].equalsIgnoreCase("worker")){
                int hits = Integer.parseInt(task_msg[0]);
                total_hits+= hits;
                //System.out.println(hits+"  Added to total hits!");
            }
            int darts_to_throw = getDarts();
            if(darts_to_throw == 0){
                System.out.println("Tasks complete!!");
            }
            // we send back a new task (darts) to that worker
            publish(String.valueOf(darts_to_throw), "worker/"+worker_id);
            //System.out.println("message received from "+task_msg[1]+" by "+TAG+" and can be redirected: Answer : "+new_msg);

        // we also have catch possible exceptions
        }catch(NumberFormatException e){

            // imagine a scenario, where darts are finished, all workers that responded go unregistered
            // but some workers did not respond and so, shutdown flag couldn't be set.
            // in this scenario. a worker sends and exit message, but shutdown flag cannot be set because there are still some workers remaining.
            // in this scenario, task tries to parse a String (Goodbye) to int, and a NumberFormatException occurs
            // we use this to clear workers and set shutdownFlag, so program closes gracefully.
            // pi will be correctly calculated, because any reduced dart will be accounted for only when there is a response.
            if(task_msg[0].equals(EXIT_FLAG) && !workers.isEmpty()){
                if(!isCleaning){
                    System.err.println("some darts may be lost, because some workers failed to respond, unregistering orphaned workers ");
                    isCleaning = true;
                }
                for(String worker: workers){
                    publish(EXIT_FLAG,worker);
                    try{
                        sleep(100);
                    } catch (InterruptedException ex) {
                        System.err.println(TAG+" : interrupted sleep -> "+e.getMessage());
                    }
                }
            }
        }catch(IndexOutOfBoundsException e){
            System.err.println(TAG+ " : Index out of bound -> "+e.getMessage());
        }
    }

    /**
     * final task to be done before program shuts down
     */
    @Override
    public void finalise() {
        System.out.println("finalising");
        // add final darts to total thrown darts
        double pi = 4.0 * ((double) total_hits / (darts_thrown));
        System.out.println("\tDarts thrown \t: "+darts_thrown);
        System.out.println("\tCalculated pi \t: "+pi);
        super.finalise();
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

