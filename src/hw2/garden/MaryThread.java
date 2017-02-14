package hw2.garden;

/** NewtonThread
 * @author ronny <br>
 * TODO: Description
 * 
 */
public class MaryThread extends Thread {

    private Garden garden;

    public MaryThread(Garden garden) {
        this.garden = garden;
    }

    public void run() {
        while (true) {
            garden.startFilling();
            // dig
            try {
                Thread.sleep(100);
                System.out.println("Filling hole: " + (garden.totalHolesFilledByMary() + 1));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            garden.doneFilling();
        }

    }
}
