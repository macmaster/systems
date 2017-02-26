
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
    int tcpPort;
    int udpPort;

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
            

			System.out.print("REQ> ");
			while (sc.hasNextLine()) {
				String cmd = sc.nextLine();
				String[] tokens = cmd.split(" ");

                switch (tokens[0]) {
                    case "setmode":
                        // Set the mode of communication for sending commands to
                        // the server (TCP vs UDP)
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
                    case "purchase":
                        // TODO: send appropriate command to the server and
                        // display the
                        // appropriate responses form the server
                        break;
                    case "cancel":
                        // TODO: send appropriate command to the server and
                        // display the
                        // appropriate responses form the server
                        break;
                    case "search":
                        // TODO: send appropriate command to the server and
                        // display the
                        // appropriate responses form the server
                        break;
                    case "list":
                        // TODO: send appropriate command to the server and
                        // display the
                        if (client.isTCP) {
                            System.out.println("Not yet implemented");
                        } else {
                            String contents = tokens[0];
                            client.sendUDPDatagram(contents);
                        }
                        break;
                    case "exit":
                        if (client.isTCP) {
                            System.out.println("Not yet implemented");
                        } else {
                            String contents = tokens[0];
                            client.sendUDPDatagram(contents);
						}
					default:
						System.out.println("ERROR: No such command");
						break;
				}
				System.out.print("REQ> ");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    private void sendUDPDatagram(String contents) throws IOException {
        DatagramPacket sPacket, rPacket;
        byte[] buffer = contents.getBytes();
        sPacket = new DatagramPacket(buffer, buffer.length, ia, udpPort);
        dataSocket.send(sPacket);
        rPacket = new DatagramPacket(rbuffer, rbuffer.length);
        dataSocket.receive(rPacket);
        String retStr = new String(rPacket.getData(), 0, rPacket.getLength());
        System.out.println("Received from Server:" + retStr);
    }

}
