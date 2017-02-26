/** Client.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * TCP / UDP Client for our online store.
 * 
 */

import java.util.Scanner;
import java.net.*;
import java.io.*;

public class Client {
    int len = 1024;
    byte[] rbuffer = new byte[len];
    boolean isTCP = true;
    InetAddress ia;
    DatagramSocket dataSocket;
    Socket socket;
    int tcpPort;
    int udpPort;
    PrintWriter out;
    BufferedReader in;

    public Client(InetAddress ia, DatagramSocket dataSocket, int udpPort, int tcpPort) {
        this.ia = ia;
        this.dataSocket = dataSocket;
        this.udpPort = udpPort;
        this.tcpPort = tcpPort;
    }

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <hostAddress>: the address of the server");
            System.out.println("\t(2) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(3) <udpPort>: the port number for UDP connection");
            System.exit(-1);
        }
        String hostAddress = args[0];
        int tcpPort = Integer.parseInt(args[1]);
        int udpPort = Integer.parseInt(args[2]);

        try {
            InetAddress ia = InetAddress.getByName(hostAddress);
            DatagramSocket dataSocket = new DatagramSocket();
            Scanner sc = new Scanner(System.in);

            Client client = new Client(ia, dataSocket, udpPort, tcpPort);
            client.connectToServer();

			System.out.print("> ");
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.trim().split("\\s+");
                switch (tokens[0]) {
                    case "setmode":
                        // Set the mode of communication for sending commands to the server (TCP vs UDP)
                        // Looks like "setmode [U|T]"
                        switch (tokens[1]) {
                            case "T":
                                client.isTCP = true;
                                break;
                            case "U":
                                client.isTCP = false;
                                break;
                            default:
                                System.out.println("ERROR: '" + tokens[1] + "' is not a valid mode");
                                break;
                        }
                        System.out.println("Will use " + (client.isTCP ? "TCP" : "UDP") + " for communication.");
                        break;
                    case "purchase": // Looks like "purchase <user-name> <product-name> <quantity>"
                    case "cancel":   // Looks like "cancel <order-id>"
                    case "search":   // Looks like "search <user-name>"
                    case "list":     // Looks like "list"
                        if (client.isTCP) {
                            client.sendTCPRequest(cmd);
                        } else {
                            client.sendUDPDatagram(cmd);
                        }
                        break;
                    case "exit":
                        if (client.isTCP) {
                            client.out.println("exit");
                            client.disconnectFromServer();
                            System.out.println("Closed connection to server.");
                        } else {
                            String contents = tokens[0];
                            client.sendUDPDatagram(contents);
						}
						break;
                    case "connect":
                        client.connectToServer();
                        break;
					default:
						System.out.println("ERROR: No such command");
						break;
				}
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

    public void sendUDPDatagram(String contents) throws IOException {
        DatagramPacket sPacket, rPacket;
        byte[] buffer = contents.getBytes();
        sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
        dataSocket.send(sPacket);
        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        dataSocket.receive(rPacket);
        String retStr = new String(rPacket.getData(), 0, rPacket.getLength());
        System.out.println("Received from Server:");
        System.out.println(retStr);
    }

    public void connectToServer() throws IOException {
        if (this.socket == null || this.socket.isClosed()) {
            this.socket = new Socket(this.ia, this.tcpPort);
        }
        if (this.in != null) { this.in.close(); }
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        if (this.out != null) { this.out.close(); }
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
    }

    public void disconnectFromServer() throws IOException {
        this.socket.close();
        this.in.close();
        this.out.close();
    }

    public void sendTCPRequest(String contents) throws IOException {
        this.out.println(contents);
        System.out.println("Waiting for server response...");
        String response;
        boolean first = true;
        while (true)  {
            response = this.in.readLine();
            if (response == null) { System.out.println("Server returned null."); break; }
            if (response.equals("EOT")) { System.out.println("End of server response."); break; }
            if (first) { System.out.println("Server response:"); first = false; }
            System.out.println(response);
        }

    }
}
