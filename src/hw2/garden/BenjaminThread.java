package hw2.garden;

/** NewtonThread
 * @author ronny <br>
 * TODO: Description
 * 
 */
public class BenjaminThread extends Thread {

    private Garden garden;

    public BenjaminThread(Garden garden) {
        this.garden = garden;
    }

    public void run() {
        while (true) {
            garden.startSeeding();
            // dig
            try {
                Thread.sleep(50);
                System.out.println("Seeding hole: " + (garden.totalHolesSeededByBenjamin() + 1));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            garden.doneSeeding();
        }

    }
}
