
/** ServerThread
 * @author ronny <br>
 *
 * Services a server request.
 */
public class ServerThread extends Thread{
	
	private Server server;
	private String command;
	
	/** ServerThread <br>
	 * 
	 * Constructs a new ServerThread Object. <br>
	 */
	public ServerThread(Server server, String command) {
		this.server = server;
		this.command = command;
	}
	
	public void run(){
		System.out.println(command + "serviced by: " + this.getId());
	}
	
}
