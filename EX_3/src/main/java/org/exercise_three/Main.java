

package org.exercise_three;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    // total darts. used MAX_VALUE but it took about 10 seconds per iteration on 8 threads
    // so 500 mill has to be enough
    private static final int TOTAL_DARTS = 500000000;
    // threads can be set from 1  - ( I tested 16 )
    private static final int THREAD_COUNT = 1;
    // work packets
    private static final int PACKETS = 32;
    // number of iterations to do
    private static final int ITERATION_COUNT = 5;


    public static void main(String[] args) {
        // basic info first
        System.out.println("Darts \t\t: " + TOTAL_DARTS);
        System.out.println("Iterations\t: " + ITERATION_COUNT);
        System.out.println("Threads \t: " + THREAD_COUNT);
        // we will record total time and sum of calculated pi
        double total_time= 0.0;
        double pi_sum = 0.0;

        // we iterate for the predefined count
        for (int i = 0; i < ITERATION_COUNT; ++i) {

            // try with resources, (auto cleans resources)
            try (ExecutorService service = Executors.newFixedThreadPool(THREAD_COUNT)) {
                // List of futures can be declared here // to avoid unnecessary overhead
                List<Future<Integer>> futures = new ArrayList<>(PACKETS);
                // share tasks equally between work packets
                int[] packets = balancedPackets();
                // total hits,will be used for calculating pi based on hits on the virtual circle
                int totalHits = 0;
                // now that we have prepared every thin, we can record time before starting execution
                long startTime = System.currentTimeMillis();
                // submit tasks
                for (int dartsPerPackage : packets) {
                    futures.add(service.submit(new CallableTask(dartsPerPackage)));
                }
                // get results
                for (Future<Integer> future : futures) {
                    try { // add what we get from MonteCarloTask
                        totalHits += future.get();
                    } catch (Exception e) {
                        System.err.println("error - " + e);
                    }
                }
                // before we go any further, lets record end time and avoid possible overhead
                long endTime = System.currentTimeMillis();
                // we calculate the value of pi in this iteration
                double approximated_pi = 4.0 * totalHits / TOTAL_DARTS;
                // total time = start - end,
                double exec_time = endTime - startTime / 1000F;
                System.out.println("Iteration " + (i + 1) + " took " + exec_time + " seconds to calculate pi   \t:  " + approximated_pi);
                // and we add time and pi value to total for average
                total_time += exec_time;
                pi_sum += approximated_pi;

                // close thread pool
                service.shutdown();
                try {
                    if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
                        service.shutdownNow();
                    }
                } catch (InterruptedException var20) {
                    service.shutdownNow();
                }
            } catch (RuntimeException e) {
                throw new RuntimeException(e);
            }
        }

        // average time
        double time = total_time / ITERATION_COUNT;
        // average value of pi as end result
        double pi = pi_sum / ITERATION_COUNT;
        // printing results
        System.out.println("Average pi\t:" + pi);
        System.out.println("Execution time\t:" + time + " seconds");
    }

    /**
     * this function divides a number of tasks in to work packets fairly
     * each packet will have almost equal number of tasks
     * maximum difference of task between packets = 1,
     * how many will have one task more than others depends on number of tasks and number of packets
     * @return int[] ( integer array the size of packets, containing number of tasks per packet
     */
    private static int[] balancedPackets() {

        // array of packets
        int[] packets = new int[PACKETS];
        // all are empty at first
        int empty_packets = PACKETS;
        // and all darts are not assigned
        int remaining_darts = TOTAL_DARTS;
        while (remaining_darts > 0) { // while we have darts
            // divide available darts to empty packets
            int darts_per_package = remaining_darts / empty_packets;
            // first packet is lucky, it will have 1 less task than the others
            packets[empty_packets - 1] = darts_per_package;
            // darts are then reduces since we did assign some to this packet
            remaining_darts -= darts_per_package;
            // and there are less empty packets now
            empty_packets--;
        }
        // packets are full
        return packets;
    }
}
