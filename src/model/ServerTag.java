package model;

import java.net.*;

/** Order
 * Bundles the server ip address and port together.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class ServerTag {
	
	private int port;
	private byte[] address;
	
	/**
	 * ServerTag
	 * 
	 * Factory method to constructs a new ServerTag object. <br>
	 * Give it a server tag. <ip_address>:<port>
	 * Returns null upon failure.
	 */
	public static ServerTag parse(String tag) {
		try {
			String[] tokens = tag.split(":");
			int port = Integer.parseInt(tokens[1]);
			
			byte[] address = new byte[4];
			String addressString = tokens[0];
			tokens = addressString.split("\\.");
			for (int idx = 0; idx < 4; idx++) {
				address[idx] = (byte) Integer.parseInt(tokens[idx]);
			}
			return new ServerTag(address, port);
		} catch (Exception err) {
			return null;
		}
	}
	
	/** ServerTag
	 * 
	 * Constructs a new ServerTag object. <br>
	 * Give it pure server tag data. <ip_address>:<port>
	 */
	private ServerTag(byte[] address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * getFormat()
	 * returns the expected server tag format. <br>
	 */
	public static String getFormat() {
		return "<ip_address>:<port>";
	}
	
	/**
	 * getPort()
	 * returns the server port. <br>
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * getAddressBytes()
	 * returns the server address bytes. <br>
	 */
	public byte[] getAddressBytes() {
		return address;
	}
	
	/** getAddress()
	 * returns the server ip address. <br>
	 * @throws UnknownHostException 
	 */
	public InetAddress getAddress() throws UnknownHostException {
		return InetAddress.getByAddress(address);
	}
	
}
