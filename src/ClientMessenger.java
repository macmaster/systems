import java.net.*;

/** ClientMessenger
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 3/17/2017
 * 
 */
public class ClientMessenger extends Messenger {

    // id of the server the client is connected to.
    private Integer serverId = 1;

    /** ClientMessenger
     * 
     * Constructs a new ClientMessenger object. <br>
     */
    public ClientMessenger() {
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see Messenger#parseMetadata(java.lang.String)
     */
    @Override
    protected boolean parseMetadata(String metadata) {
        try {
            String[] tokens = metadata.split("\\s+");
            this.numServers = Integer.parseInt(tokens[0]);
            if ((numServers <= 0)) {
                System.err.println("Bad metadata values. make sure numServers > 0.");
                return false;
            } else {
                return true;
            }
        } catch (Exception err) {
            System.err.println("Could not parse client metadata.");
            System.err.println("usage: " + getMetadataFormat());
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see Messenger#getMetadataFormat()
     */
    @Override
    protected String getMetadataFormat() {
        return "<numServers>";
    }
    
    protected void hopServers(){
        serverId += 1;
        if(serverId > numServers){
            System.err.println("Out of servers to connect to. exiting...");
            System.exit(1);
        }
    }

    protected InetAddress getServerAddress() {
        InetAddress address = null;
        do {
            try {
                address = getServerTag(serverId).getAddress();
            } catch (UnknownHostException err) {
                // DEBUG: remove this?
                err.printStackTrace();
                serverId += 1;
            }
        } while (address == null && serverId <= numServers);
        return address;
    }

    protected Integer getServerPort() {
        return getServerTag(serverId).getPort();
    }

}
