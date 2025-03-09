
package org.exercise_three;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class CallableTask implements Callable<Integer> {
    private final int darts;

    // constructor
    public CallableTask(int darts) {
        this.darts = darts;
    }

    /**
     * callable function
     * @return int, how many darts hit the circle
     */
    public Integer call() {
        int hits = 0;
        // random
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for(int i = 0; i < this.darts; ++i) {
            // random simulation of dart throw
            // and the dart lands in x,y coordinated in the square with a side 2 (between -1,1) 0 as a center point
            double x = rng.nextDouble(-1, 1);
            double y = rng.nextDouble(-1, 1);
            // if the hypotenuse of x and y = or less than 1, we have a hit
            // x² + y² <= 1²
            if (Math.hypot(x, y) <= 1) { // hit a target
                ++hits;
            }
        }
        // return hits that landed within the circle
        return hits;
    }
}