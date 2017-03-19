package network;

/** ServerThread.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * Services a server request.
 */

import java.io.*;
import java.net.*;

import controller.Server;

public class ServerThread extends Thread {

    private Server server;
    private Socket socket;

    /** ServerThread <br>
     * 
     * Constructs a new ServerThread Object. <br>
     * Services a TCP Socket.
     */
    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    // service TCP Socket
    public void run() {
        this.serviceTCP();
    }

    /**
     * serviceTCP()
     * 
     * Connection-based tcp socket. <br>
     * Writes to socket output.
     */
    public void serviceTCP() {
        try (InputStreamReader istream = new InputStreamReader(socket.getInputStream());
             PrintWriter ostream = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(istream);) {

            // continually service tcp connection.
            String command = "", response = "";
            while ((command = reader.readLine()) != null) {
                System.out.println("TCP Service: " + command);
                if (command.equals("exit")) {
                    System.out.println("Closing tcp socket.");
                    socket.close();
                    break; // finished socket execution.
                } else { // execute server command.
                    response = execute(command);
                    ostream.println(response);
                    ostream.println("EOT");
                }
            }
        } catch (IOException err) {
            System.out.println("Error servicing TCP Client request. exiting...");
            err.printStackTrace();
        }
    }

    /** execute()
     * 
     * Executes valid server command. <br>
     */
    public String execute(String command) {
        String response = "";
        String[] tokens = command.trim().split("\\s+");
        try { // parse and execute
            String opcode = tokens[0].toLowerCase();
            if (opcode.equals("list")) {
                response = server.list();
            } else if (opcode.equals("purchase")) {
                String user = tokens[1];
                String product = tokens[2];
                Integer quantity = Integer.parseInt(tokens[3]);
                response = server.purchase(user, product, quantity);
            } else if (opcode.equals("cancel")) {
                Integer orderId = Integer.parseInt(tokens[1]);
                response = server.cancel(orderId);
            } else if (opcode.equals("search")) {
                String username = tokens[1];
                response = server.search(username);
            } else {
                response = "server command not supported: " + opcode;
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
