package hw2.garden;

import java.util.concurrent.locks.*;

/** Garden
 * @author ronny <br>
 * TODO: Description
 * 
 * (a)  Benjamin cannot plant a seed unless at least one empty hole exists and Mary cannot fill
        a hole unless at least one hole exists in which Benjamin has planted a seed.
   (b)  Newton has to wait for Benjamin if there are 4 holes dug which have not been seeded
        yet.  He also has to wait for Mary if there are 8 unfilled holes.  Mary does not care how
        far Benjamin gets ahead of her.
   (c)  There is only one shovel that can be used to dig and fill holes, and thus Newton and
        Mary need to coordinate between themselves for using the shovel; ie.  only one of them
        can use the shovel at any point of time.
 * 
 */
public class Garden {

    private ReentrantLock shovel;
    private Condition unseededHoleExists;
    private Condition unfilledHoleExists;
    private Condition unseededHoleLimit;
    private Condition unfilledHoleLimit;

    private final Integer unseededHolesMax = 1000;
    private final Integer unfilledHolesMax = 1000;
    private Integer unseededHoles, unfilledHoles;
    private Integer newtonHoles, benjaminHoles, maryHoles;

    public Garden() {
        shovel = new ReentrantLock();
        unseededHoleExists = shovel.newCondition();
        unfilledHoleExists = shovel.newCondition();
        unseededHoleLimit = shovel.newCondition();
        unfilledHoleLimit = shovel.newCondition();

        // hole state counters
        unseededHoles = 0;
        unfilledHoles = 0;
        newtonHoles = 0;
        benjaminHoles = 0;
        maryHoles = 0;
    }

    public void startDigging() {
        shovel.lock();
        try {
            while (unseededHoles >= unseededHolesMax) {
                unseededHoleLimit.await();
            }
            while (unfilledHoles >= unfilledHolesMax) {
                unfilledHoleLimit.await();
            }
        } catch (InterruptedException err) {
            System.err.println("Interrupted while waiting!!");
            err.printStackTrace();
            shovel.unlock();
        }
    }

    public void doneDigging() {
        newtonHoles = newtonHoles + 1;
        unseededHoles = unseededHoles + 1;
        unseededHoleExists.signal();
        shovel.unlock();
    }

    public void startSeeding() {
        shovel.lock();
        try {
            // benjamin can't plant a seed unless an empty hole exists.
            while (unseededHoles == 0) {
                unseededHoleExists.await();
            }
            unseededHoles = unseededHoles - 1;
        } catch (InterruptedException err) {
            System.err.println("Interrupted while waiting!!");
            err.printStackTrace();
            shovel.unlock();
        }
    }

    public void doneSeeding() {
        unfilledHoles = unfilledHoles + 1;
        benjaminHoles = benjaminHoles + 1;
        unfilledHoleExists.signal();
        if (unseededHoles < unseededHolesMax) {
            unseededHoleLimit.signal();
        }
        shovel.unlock();
    }

    public void startFilling() {
        shovel.lock();
        try {
            // Mary cannot fill a hole unless a seeded hole exists
            while (unfilledHoles == 0) {
                unfilledHoleExists.await();
            }
            unfilledHoles = unfilledHoles - 1;
        } catch (InterruptedException err) {
            System.err.println("Interrupted while waiting!!");
            err.printStackTrace();
            shovel.unlock();
        }
    }

    public void doneFilling() {
        maryHoles = maryHoles + 1;
        if (unfilledHoles < unfilledHolesMax) {
            unfilledHoleLimit.signal();
        }
        shovel.unlock();
    }

    /*
     * The following methods return the total number of holes dug, seeded or
     * filled by Newton, Benjamin or Mary at the time the methods' are invoked
     * on the garden class.
     */
    public int totalHolesDugByNewton() {
        return newtonHoles;
    }

    public int totalHolesSeededByBenjamin() {
        return benjaminHoles;
    }

    public int totalHolesFilledByMary() {
        return maryHoles;
    }
}