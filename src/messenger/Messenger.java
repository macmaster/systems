package messenger;
import java.io.*;
import java.util.*;

import model.ServerTag;

/** Messenger
 * @author ronny <br>
 * TODO: Description
 * 
 */
public abstract class Messenger {

    // communication handles
    protected Integer numServers;
    protected Map<Integer, ServerTag> tags;

    /** Messenger
     * 
     * Initialize the server-tag file. <br>
     */
    public Messenger() {
        this.tags = new HashMap<Integer, ServerTag>();
    }

    /**
     * parseMetadata()
     * 
     * Parses the first string of input. <br>
     * returns true if the data was successful parsed.
     */
    protected abstract boolean parseMetadata(String metadata);

    /**
     * getMetadataFormat()
     * returns the structural format expected for a metadata string.
     */
    protected abstract String getMetadataFormat();

    /** init()
     * 
     * Prompts server input. <br>
     * postcondition: server tag-file is created.
     */
    public void init() {
        try (InputStreamReader stream = new InputStreamReader(System.in);
             BufferedReader reader = new BufferedReader(stream);) {

            do { // parse server / client metadata string.
                System.out.format("Enter the metadata string (%s): ", getMetadataFormat());
            } while (!parseMetadata(reader.readLine()));

            // build server file.
            int index = 1;
            System.out.println("Server tag format: " + ServerTag.getFormat());
            while (index <= numServers) {
                System.out.format("Enter details for server (%d): ", index);
                ServerTag tag = ServerTag.parse(reader.readLine());
                if (tag == null) {
                    System.err.println("Could not parse server tag.");
                    System.err.println("format: " + ServerTag.getFormat());
                } else { // parsed server tag.
                    tags.put(index, tag);
                    index += 1;
                }
            }

        } catch (IOException err) {
            err.printStackTrace();
            System.err.println("Could not read server input. exiting...");
            System.exit(1);
        }
    }

    
    /** getServerTag()
     * 
     * get the server tag handle of a specific server by id.<br>
     * @return { <inetaddress>:<port> } of server.
     */
    public ServerTag getServerTag(Integer serverId) {
        return tags.get(serverId);
    }
}
