import java.io.*;
import java.net.*;

/** ServerThread
 * @author ronny <br>
 *
 * Services a server request.
 */
public class ServerThread extends Thread {

    private Server server;
    private Socket socket;
    private DatagramPacket packet;

    private ConnectionMode mode;

    private enum ConnectionMode {
        TCP, UDP
    };

    /** ServerThread <br>
     * 
     * Constructs a new ServerThread Object. <br>
     * Services a TCP Socket.
     */
    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.mode = ConnectionMode.TCP;
    }

    /** ServerThread <br>
     * 
     * Constructs a new ServerThread Object. <br>
     * Services a UDP packet.
     */
    public ServerThread(Server server, DatagramPacket packet) {
        this.server = server;
        this.packet = packet;
        this.mode = ConnectionMode.UDP;
    }

    public void run() {
        if (mode.equals(ConnectionMode.TCP)) {
            // service TCP Socket
            this.serviceTCP();
        } else if (mode.equals(ConnectionMode.UDP)) {
            // service UDP Socket
            this.serviceUDP();
        }
    }

    /**
     * serviceTCP()
     * 
     * Connection-based tcp socket. <br>
     * Writes to socket output.
     */
    public void serviceTCP() {
        try (InputStreamReader istream = new InputStreamReader(socket.getInputStream());
                PrintWriter ostream = new PrintWriter(socket.getOutputStream());
                BufferedReader reader = new BufferedReader(istream);) {

            // continually service tcp connection.
            String command = "", response = "";
            while ((command = reader.readLine()) != null) {
                System.out.println("TCP Service: " + command);
                if (command.equals("exit")) {
                    break; // finished socket execution.
                } else { // execute server command.
                    response = execute(command);
                    ostream.print(response);
                }
            }
        } catch (IOException err) {
            System.out.println("Error servicing TCP Client request. exiting...");
            err.printStackTrace();
        }
    }

    /**
     * serviceUDP()
     * 
     * Connectionless UDP packets. <br>
     * Sends a response packet.
     */
    public void serviceUDP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            String command = new String(packet.getData());
            System.out.println("UDP Service: " + command);

            // execute server command
            String response = execute(command);
            byte[] data = response.getBytes();
            int length = data.length;

            // send return packet with command response.
            DatagramPacket returnPacket = new DatagramPacket(data, length);
            returnPacket.setAddress(packet.getAddress());
            returnPacket.setPort(packet.getPort());
            socket.send(returnPacket);
        } catch(IOException err){
            System.err.println("Error servicing UDP request.");
            err.printStackTrace();
        }
    }

    public String execute(String command) {
        String response = "";
        String[] tokens = command.trim().split("\\s+");
        try { // parse and execute
            String opcode = tokens[0].toLowerCase();
            if (opcode.equals("list")) {
                response = server.list();
            } else {
                response = command;
            }
        } catch (Exception err) {
            System.err.println("invalid server command: " + command);
            response = "invalid server command: " + command;
            err.printStackTrace();
        }

        // client response
        return response;
    }
}
