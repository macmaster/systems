import java.io.*;
import java.net.Socket;

/** ServerThread
 * @author ronny <br>
 *
 * Services a server request.
 */
public class ServerThread extends Thread {

    private Server server;
    private String command;

    /** ServerThread <br>
     * 
     * Constructs a new ServerThread Object. <br>
     */
    public ServerThread(Server server, String command) {
        this.server = server;
    }

    public void run() {

    }

}
