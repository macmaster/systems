package model;

import java.util.concurrent.atomic.AtomicInteger;

/** Order
 * Holds the product name and quantity of an order.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class Order {
	
	// id generator.
	private static AtomicInteger Registrar = new AtomicInteger(1);
	
	private final int id;
	private String username;
	private String product;
	private Integer quantity;
	
	/** Order
	 * 
	 * Constructs a new Order object. <br>
	 * Automatically registers with a new order id.
	 */
	public Order(String product, Integer quantity) {
		this.id = Registrar.getAndIncrement();
		this.product = product;
		this.quantity = quantity;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getProduct() {
		return this.product;
	}
	
	public Integer getQuantity() {
		return this.quantity;
	}
	
	/**
	 * setUser()
	 * 
	 * Binds an order to a user. <br>
	 * @param username
	 */
	public void setUser(String username) {
		this.username = username;
	}
	
	public String getUser() {
		return this.username;
	}
	
	@Override
	public String toString() {
		return String.format("%d %s %s %d", id, username, product, quantity);
	}
	
}
