import java.io.IOException;
import java.net.*;

/** ServerUDPListener
 * By: Ronald Macmaster and Taylor Schmidt
 * UT-EID: rpm953   and    trs2277
 * Date: 2/25/17
 * 
 * UDP Listener thread for product server. <br>
 * Opens new UDP server socket and listens <br>
 */
public class ServerUDPListener extends Thread {

    private int port;
    private Server server;

    /** ServerUDPListener <br>
     * 
     * Constructs a new ServerUDPListener Object. <br>
     */
    public ServerUDPListener(Server server, int port) {
        this.port = port;
        this.server = server;
    }

    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port);) {
            final int length = 1024;
            while (true) { // listen for tcp clients
                byte[] data = new byte[length];
                DatagramPacket packet = new DatagramPacket(data, length);
                socket.receive(packet);
                ServerThread worker = new ServerThread(server, packet);
                worker.start();
            }
        } catch (IOException e) {
            System.out.println("Error listening on UDP Socket. exiting...");
            e.printStackTrace();
        }
    }

}
