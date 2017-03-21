package controller;

import java.io.*;
import java.net.*;

import messenger.ClientMessenger;

public class Client {
    private int port;
    private InetAddress ia;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private ClientMessenger messenger;

    public Client() {
        this.messenger = new ClientMessenger();
        this.messenger.init();
        this.ia = messenger.getServerAddress();
        this.port = messenger.getServerPort();
    }

    public static void main(String[] args) {
        System.out.println("/*** Online Shopping Client ***/");
        try (InputStreamReader stream = new InputStreamReader(System.in); BufferedReader reader = new BufferedReader(stream);) {
            // kick-start client
            Client client = new Client();
            client.connectToServer();

            // command loop.
            String command = "";
            System.out.print("> ");
            while ((command = reader.readLine()) != null) {
                client.execute(command);
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
            while (true) {
                String response = this.in.readLine();
                if (response == null) {
                    System.out.println("Server crashed.");
                    break;
                } else if (response.equals("ping")) {
                    System.out.println("Server sent keep alive.");
                } else if (response.equals("EOT")) {
                    System.out.println("End of server response.");
                    break;
                } else {
                    System.out.println(response);
                }
            }
        } catch (SocketTimeoutException signal) {
            // timeout, server is dead. switch servers.
            refreshConnection();
            sendTCPRequest(contents);
            // resend command.
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
                System.exit(0); // finished.
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
