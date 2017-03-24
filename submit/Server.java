

/** Server.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/25/17
 * 
 * TCP / UDP Server for our online store.
 * 
 */

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Server {

    // server networking
    public String filename;
    private ServerMessenger messenger;

    // server records
    private Map<String, Integer> inventory;
    private Map<Integer, Order> orders; // list of pending orders.
    private Map<String, List<Order>> users; // user to string records

    public static void main(String[] args) {
        // parse the inventory file and start the server.
        System.out.println("Starting the inventory server...");
        Server server = new Server();
        server.init();
        server.start();
    }

    public Server() {
        // server networking
        this.messenger = new ServerMessenger(this);

        // server records
        this.inventory = new HashMap<String, Integer>();
        this.users = new HashMap<String, List<Order>>();
        this.orders = new HashMap<Integer, Order>();
    }

    /** getMessenger()
     * 
     * provides access to the messenger for this server. <br>
     */
    public ServerMessenger getMessenger() {
        return this.messenger;
    }

    /**
     * purchase()
     * 
     * Adds a valid purchase to the user's checkout cart. <br>
     * if not enough items, returns : "Not Available - Not enough items"
     * if product not in storage, returns : "Not Available - We do not sell this product"
     * Upon success, reply with "Your order has been placed, <order-id> <user-name> <product-name> <quantity>" 
     *      then, update the inventory.
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

        Integer count = inventory.get(product);
        if (!inventory.containsKey(product)) {
            response = "Not Available - We do not sell this product";
        } else if (count < quantity) {
            response = "Not Available - Not enough items";
        } else if (quantity < 0) {
            response = "Not Available - Negative purchases are not allowed";
        } else {
            // update inventory.
            inventory.put(product, count - quantity);

            // add user order to cart.
            Order order = new Order(product, quantity);
            order.setUser(username);
            orders.put(order.getId(), order);
            cart.add(order);
            response = String.format("Your order has been placed, %s", order.toString());
        }

        return response.trim();
    }

    /**
     * cancel()
     * 
     * Cancels the order id. <br>
     * if no existing order: prints "<order-id> not  found,  no  such  order"
     * otherwise, replies: "Order <order-id> is canceled" and updates the inventory.
     */
    public synchronized String cancel(Integer orderId) {
        String response = "";
        if (!orders.containsKey(orderId)) {
            response = String.format("%d not  found,  no  such  order", orderId);
        } else {
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

        return response.trim();
    }

    /**
     * search()
     * 
     * Lists all the users available for the user <br>
     * if no existing orders for the user: prints "No order found for <user-name>"
     * Otherwise, all orders of the user as "<order-id>, <product-name>, <quantity>"
     */
    public synchronized String search(String username) {
        String response = "";
        List<Order> cart = users.get(username);
        if (!users.containsKey(username) || cart.isEmpty()) {
            response = String.format("No order found for %s", username);
        } else {
            for (Order order : cart) {
                Integer id = order.getId();
                String product = order.getProduct();
                Integer quantity = order.getQuantity();
                response += String.format("%d %s %s\n", id, product, quantity);
            }
        }
        return response.trim();
    }

    /**
     * lists all available products with quantities of the store. <br>
     * <b>format</b>: <product-name> <quantity>   <br>
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
     * initializes the server.
     */
    public void init() {
        messenger.init();
        load(filename);
    }

    /**
     * 
     * Starts the Messenger Service. <br>
     */
    public void start() {
        messenger.start();
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
