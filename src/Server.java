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
    private Integer tcpPort, udpPort;
    private Map<String, Integer> inventory;
    private Map<Integer, Order> orders; // list of pending orders.
    private Map<String, List<Order>> users; // user to string records

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
    }

    public Server(String filename, Integer tcpPort, Integer udpPort) {
        // server networking
        this.filename = filename;
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
        this.tcpListener = new ServerTCPListener(this, tcpPort);
        this.udpListener = new ServerUDPListener(this, udpPort);

        // server records
        this.inventory = new HashMap<String, Integer>();
        this.users = new HashMap<String, List<Order>>();
        this.orders = new HashMap<Integer, Order>();
    }

    /**
     * purchase()
     * 
     * Adds a valid purchase to the user's checkout cart. <br>
     * if not enough items, returns : "Not Available - Not enough items"
     * if product not in storage, returns : "Not Available - We do not sell this product"
     */
    public synchronized String purchase(String username, String product, Integer quantity) {
        String response = "";

        List<Order> cart; // user's checkout cart
        if (users.containsKey(username)) {
            cart = users.get(username);
        } else {
            cart = new ArrayList<Order>();
            users.put(username, cart);
        }

        if (!inventory.containsKey(product)) {
            response = "Not Available - We do not sell this product";
        } else if (inventory.get(product) < quantity) {
            response = "Not Available - Not enough items";
        } else {
            // update inventory.
            int count = inventory.get(product);
            inventory.put(product, count - quantity);

            // add user order to cart.
            Order order = new Order(product, quantity);
            order.setUser(username);
            orders.put(order.getId(), order);
            cart.add(order);
            response = order.toString();
        }

        return response.trim();
    }

    /**
     * cancel()
     * 
     * Cancels the order id. <br>
     * if no existing order: prints "<order-id> not  found,  no  such  order"
     * otherwise, replies: "Order <order-id> is canceled"
     */
    public synchronized String cancel(Integer orderId) {
        String response = "";
        if(!orders.containsKey(orderId)){
            response = String.format("%d not  found,  no  such  order", orderId);
        } else{
            // remove order from cart and records.
            Order order = orders.get(orderId);
            String user = order.getUser();
            List<Order> cart = users.get(user);
            orders.remove(orderId);
            cart.remove(order);
            
            // update inventory.
            String product = order.getProduct();
            int count = inventory.get(product) + order.getQuantity();
            inventory.put(product, count);
            
            response = String.format("Order %d is canceled", orderId);
        }
        
        return response;
    }
    
    /**
     * search()
     * 
     * Lists all the users available for the user <br>
     * if no existing orders for the user: prints "No order found for <user-name>"
     * otherwise, replies: "Order <order-id> is canceled"
     */
    public synchronized String search(String username){
        String response = "";
        response = "search is not implemented yet.. lol";
        return response;
    }

    /**
     * lists all available products with quantities of the store. <br>
     * <b>format</b>: product-name quantity   <br>
     * prints sold out items with quantity 0. <br>
     * each product string is on a separate line.
     */
    public synchronized String list() {
        String response = "";
        for (Entry<String, Integer> record : inventory.entrySet()) {
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
        System.out.println("Starting inventory server...");
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
    public synchronized void load(String filename) {
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
                    inventory.put(product, quantity);
                }

            }

        } catch (IOException e) {
            System.err.println("Could not read input server file.");
            e.printStackTrace();
        }
    }

}
