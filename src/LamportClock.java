
/** LamportClock
 * @author ronny <br>
 * Timestamp of a process according to lamport's clock.
 * 
 */
public class LamportClock {

    private Integer timestamp;
    private Integer processId;

    /** LamportClock
     * 
     * Constructs a new LamportClock object. <br>
     */
    public LamportClock(Integer processId) {
        this(0, processId);
    }

    public LamportClock(Integer timestamp, Integer processId) {
        this.processId = processId;
        this.timestamp = timestamp;
    }

    public LamportClock setTimestamp(LamportClock stamp) {
        if (stamp != null) {
            this.processId = stamp.processId;
            this.timestamp = stamp.timestamp;
            return this;
        } else {
            return null;
        }
    }

    /** increment()
     * 
     * increments the clock reading by 1 time unit. <br>
     */
    public LamportClock increment() {
        timestamp += 1;
        return this;
    }

    /** getId()
     * 
     * returns the served Id. <br>
     */
    public Integer getId() {
        return processId;
    }

    public boolean equals(LamportClock stamp) {
        if (stamp == null) {
            return false;
        } else {
            return (timestamp == stamp.timestamp) && (processId == stamp.processId);
        }
    }

    /** compareTo()
     * 
     * compare two logical timestamps. <br>
     * @return > 1 if this clock timestamp is greater than other clock timestamp.
     * @return < 1 if this clock timestamp is less than other clock timestamp.
     * @return 0 if they are equal.
     */
    public int compareTo(LamportClock stamp) {
        if (stamp == null) {
            return 1;
        } else if (timestamp > stamp.timestamp) {
            return 1;
        } else if (timestamp < stamp.timestamp) {
            return -1;
        } else if (processId > stamp.processId) {
            return 1;
        } else if (processId < stamp.processId) {
            return -1;
        } else {
            return 0;
        }
    }
}
