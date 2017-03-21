package network;

import java.io.*;

/**
 * Created by tschmidt on 3/20/17.
 */
public class KeepAliveThread extends Thread {
    private PrintWriter pw;
    private boolean alive = true;

    public KeepAliveThread(PrintWriter pw) {
        this.pw = pw;
    }

    public void kill() { self.alive = false; }

    public void run() { keepConnectionAlive(); }

    private void keepConnectionAlive() {
        while (true) {
            if (!alive) { break; }
            try {
                Thread.sleep(50);
                pw.println("ping");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
