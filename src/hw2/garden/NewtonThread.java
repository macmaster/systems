package hw2.garden;

/** NewtonThread
 * @author ronny <br>
 * TODO: Description
 * 
 */
public class NewtonThread extends Thread {

    private Garden garden;

    public NewtonThread(Garden garden) {
        this.garden = garden;
    }

    public void run() {
        while (true) {
            garden.startDigging();
            // dig
            try {
                Thread.sleep(50);
                System.out.println("Digging hole: " + (garden.totalHolesDugByNewton() + 1));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            garden.doneDigging();
        }

    }
}
