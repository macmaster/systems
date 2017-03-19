package controller;

/** Client.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * TCP / UDP Client for our online store.
 * 
 */

import java.util.Scanner;

import messenger.ClientMessenger;

import java.net.*;
import java.io.*;

public class Client {
    private int port;
    private InetAddress ia;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // TODO: implement server queue and client messenger.
    private ClientMessenger messenger;

    public Client() {
        this.messenger = new ClientMessenger();
        this.messenger.init();
        this.ia = messenger.getServerAddress();
        this.port = messenger.getServerPort();
    }

    public static void main(String[] args) {
        // kick-start client input loop.
        System.out.println("\n\n/*** Online Shopping Client ***/");
        try (Scanner sc = new Scanner(System.in);) {
            Client client = new Client();
            client.connectToServer();
            System.out.print("> ");
            while (sc.hasNextLine()) {
                String cmd = sc.nextLine();
                client.execute(cmd);
                System.out.print("> ");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * initializes the client messenger.
     */
    public void init() {
        messenger.init();
    }

    public void connectToServer() throws IOException {
        try {
            this.ia = messenger.getServerAddress();
            this.port = messenger.getServerPort();
            if (this.socket == null || this.socket.isClosed()) {
                this.socket = new Socket();
                this.socket.connect(new InetSocketAddress(ia, port), 100);
                this.socket.setSoTimeout(100); // set server timeouts.
            }
            if (this.in != null) {
                this.in.close();
            }
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            if (this.out != null) {
                this.out.close();
            }
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
        } catch (IOException err) {
            err.printStackTrace();
            System.err.format("ERROR: can't connect to host %s on port %d %n", ia.getHostAddress(), port);
            System.err.println("Hopping servers.");
            refreshConnection(); // retry on new server.
        }
    }

    public void disconnectFromServer() throws IOException {
        if (this.socket != null) {
            this.socket.close();
        }
        if (this.in != null) {
            this.in.close();
        }
        if (this.out != null) {
            this.out.close();
        }
    }

    public void refreshConnection() throws IOException {
        disconnectFromServer();
        messenger.hopServers();
        connectToServer(); // reconnect on new server.
    }

    public void sendTCPRequest(String contents) throws IOException {
        try {
            this.out.println(contents);
            System.out.println("Waiting for server response...");
            String response = "";
            boolean first = true;
            while (true) {
                response = this.in.readLine();
                if (response == null) {
                    System.out.println("Server returned null.");
                    break;
                }
                if (response.equals("EOT")) {
                    System.out.println("End of server response.");
                    break;
                }
                if (first) {
                    System.out.println("Server response:");
                    first = false;
                }
                System.out.println(response);
            }
        } catch (SocketTimeoutException signal) {
            // timeout, server is dead. switch servers.
            refreshConnection();
            // resend command.
            // TODO: Swap "resending command" to "printing failure and prompting again?"
            sendTCPRequest(contents);
        }
    }

    public void execute(String cmd) throws IOException {
        String[] tokens = cmd.trim().split("\\s+");
        switch (tokens[0]) {
            case "purchase":
                // "purchase <user-name> <product-name> <quantity>"
            case "cancel":
                // "cancel <order-id>"
            case "search":
                // "search <user-name>"
            case "list":
                // "list"
                this.sendTCPRequest(cmd);
                break;
            case "exit":
                this.out.println("exit");
                this.disconnectFromServer();
                System.out.println("Closed connection to server.");
                break;
            case "connect":
                this.connectToServer();
                break;
            default:
                System.out.println("ERROR: No such command");
                break;
        }
    }
}
