/** ServerMessenger
 * @author ronny <br>
 * Contains communication methods for the server.
 * Manages the server LUT.
 * 
 */
public class ServerMessenger extends Messenger {

    // specific server handle.
    private Server server;
    private Integer serverId;
    private ServerTag serverTag;

    /** ServerMessenger
     * 
     * Constructs a new ServerMessenger object. <br>
     */
    public ServerMessenger(Server server) {
        this.server = server;
    }

    /**
     * start()
     * 
     * start the server listener <br>
     */
    public void start() {
        // server port listener
        this.serverTag = tags.get(serverId); // set my server tag.
        new ServerTCPListener(server, serverTag.getPort()).start();
    }

    // parse server metadata.
    @Override // <serverId> <numServers> <filename>
    protected boolean parseMetadata(String metadata) {
        try {
            String[] tokens = metadata.split("\\s+");
            this.serverId = Integer.parseInt(tokens[0]);
            this.numServers = Integer.parseInt(tokens[1]);
            server.filename = tokens[2]; // inventory path
            if ((numServers <= 0) || (serverId < 1) || (serverId > numServers)) {
                System.err.println("Bad metadata values. make sure numServers > 0.");
                return false;
            } else {
                return true;
            }
        } catch (Exception err) {
            System.err.println("Could not parse server metadata.");
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
        return "<serverId> <numServers> <inventory_path>";
    }
}
