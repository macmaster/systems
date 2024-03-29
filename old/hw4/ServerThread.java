

/** ServerThread.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * Services a server request.
 */

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class ServerThread extends Thread {

    private Server server;
    private Socket socket;
    private ServerMessenger messenger;
    private static ReentrantLock requestLock = new ReentrantLock(true);

    // KeepAliveThread pinger
    private KeepAliveThread pinger;

    /** ServerThread <br>
     * 
     * Constructs a new ServerThread Object. <br>
     * Services a TCP Socket.
     */
    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.messenger = server.getMessenger();
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
            LamportClock timestamp = null;
            String command = "", response = "";
            while ((command = reader.readLine()) != null) {
                pinger = new KeepAliveThread(ostream); // keep alive thread
                ostream.println("ping"); // 100ms acknowledgement.
                System.out.println("TCP Service: " + command);

                // finished session.
                if (command.equals("exit")) {
                    socket.close();
                    break; // finished socket execution.
                }

                // DEBUG: rec ping.. ignore message.
                else if (command.equals("ping")) {
                    System.out.println("Receieved a ping...");
                }

                // service intraserver request.
                else if (command.startsWith("request")) {
                    timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
                    messenger.receiveRequest(timestamp);
                }

                // service intraserver release.
                else if (command.startsWith("release")) {
                    // execute the command before removing from queue.
                    timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
                    command = reader.readLine();
                    execute(command);
                    messenger.receiveRelease(timestamp);
                }

                // service intraserver acknowledgement.
                else if (command.startsWith("acknowledge")) {
                    timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
                    messenger.receiveAcknowledgement(timestamp);
                }

                // commands that require acknowledgement.
                else if (command.startsWith("purchase") || command.startsWith("cancel")) {
                    pinger.start();
                    requestLock.lock(); // request critical section
                    messenger.request();
                    response = execute(command);
                    messenger.incrementClock();
                    messenger.release(command);
                    requestLock.unlock(); // release critical section
                    pinger.kill();
                    ostream.println(response);
                    ostream.println("EOT");
                }

                // thread-safe commands.
                else { // execute server command. (list or search)
                    response = execute(command);
                    ostream.println(response);
                    ostream.println("EOT");
                    messenger.incrementClock();
                }
            }
        } catch (IOException err) {
            System.out.println("Error servicing TCP Client request. exiting...");
            err.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            // err.printStackTrace();
        }

        // client response
        return response;
    }
}
