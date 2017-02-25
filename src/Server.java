import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/** Sever.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * TCP / UDP Server for our online store.
 * 
 */

public class Server {

    private String filename;
    private Map<String, Integer> products;
    private Integer tcpPort, udpPort;

    // server port listeners
    private ServerTCPListener tcpListener;
    private ServerUDPListener udpListener;

    public static void main(String[] args) {
        int tcpPort, udpPort;
        if (args.length != 3) {
            System.out.println("ERROR: Provide 3 arguments");
            System.out.println("\t(1) <tcpPort>: the port number for TCP connection");
            System.out.println("\t(2) <udpPort>: the port number for UDP connection");
            System.out.println("\t(3) <file>: the file of inventory");
            System.exit(-1);
        }
        tcpPort = Integer.parseInt(args[0]);
        udpPort = Integer.parseInt(args[1]);
        String filename = args[2];

        // parse the inventory file
        Server server = new Server(filename, tcpPort, udpPort);
        server.load();
        server.start();

        // TODO: handle request from clients
    }

    public Server(String filename, Integer tcpPort, Integer udpPort) {
        this.filename = filename;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.products = new HashMap<String, Integer>();
        this.tcpListener = new ServerTCPListener(this, tcpPort);
        this.udpListener = new ServerUDPListener(this, udpPort);
    }

    /**
     * lists all available products with quantities of the store. <br>
     * <b>format</b>: product-name quantity   <br>
     * prints sold out items with quantity 0. <br>
     * each product string is on a separate line.
     */
    public synchronized String list() {
        String response = "";
        for (Entry<String, Integer> record : products.entrySet()) {
            String product = record.getKey();
            Integer quantity = record.getValue();

            // print product record
            String output = String.format("%s %d\n", product, quantity);
            response += output;
        }
        return response.trim();
    }

    /**
     * start()
     * 
     * start the server listeners <br>
     */
    public void start() {
        tcpListener.start();
        udpListener.start();
    }

    /**
     * 
     * Load inventory file. <br>
     */
    public void load() {
        load(filename);
    }

    /** 
     * Load inventory file from custom source. <br>
     */
    public void load(String filename) {
        try (FileInputStream fstream = new FileInputStream(filename);
                InputStreamReader istream = new InputStreamReader(fstream);
                BufferedReader reader = new BufferedReader(istream)) {

            // load product map from inventory file
            String line = "";
            String product = "";
            Integer quantity = 0;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                if (tokens.length >= 2) {
                    product = tokens[0];
                    quantity = Integer.parseInt(tokens[1]);

                    // update product record
                    products.put(product, quantity);
                }

            }

        } catch (IOException e) {
            System.err.println("Could not read input server file.");
            e.printStackTrace();
        }
    }

}
