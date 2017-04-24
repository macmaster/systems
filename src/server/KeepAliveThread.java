package server;

import java.io.PrintWriter;

/** KeepAliveThread
 * mechanism to constantly ping a server and poll if it's awake.
 * synchronous by its very nature.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class KeepAliveThread extends Thread {
	
	private PrintWriter pw;
	private boolean alive = true;
	
	public KeepAliveThread(PrintWriter pw) {
		this.pw = pw;
	}
	
	public void kill() {
		this.alive = false;
	}
	
	@Override
	public void run() {
		try {
			this.alive = true;
			keepConnectionAlive();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void keepConnectionAlive() throws InterruptedException {
		while (alive) {
			Thread.sleep(50);
			pw.println("ping");
		}
	}
	
}
