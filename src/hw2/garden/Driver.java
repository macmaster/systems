package hw2.garden;

public class Driver {

    public static void main(String[] args) {
        Garden garden = new Garden();
        NewtonThread newton = new NewtonThread(garden);
        BenjaminThread benjamin = new BenjaminThread(garden);
        MaryThread mary = new MaryThread(garden);

        newton.start();
        benjamin.start();
        mary.start();
    }

}
