package model;

import java.util.regex.*;

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

    public LamportClock setClock(LamportClock clock) {
        if (clock != null) {
            this.processId = clock.processId;
            this.timestamp = clock.timestamp;
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

    /**
     * @return the timestamp
     */
    public Integer getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the processId
     */
    public Integer getProcessId() {
        return processId;
    }

    /**
     * @param processId the processId to set
     */
    public void setProcessId(Integer processId) {
        this.processId = processId;
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

    public String toString() {
        return String.format("(%d, %d)", timestamp, processId);
    }

    public static LamportClock parseClock(String clockString) {
        try {
            String regex = "\\((\\d+), (\\d+)\\)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(clockString);
            matcher.find();
            Integer timestamp = Integer.parseInt(matcher.group(1));
            Integer processId = Integer.parseInt(matcher.group(2));
            return new LamportClock(timestamp, processId);
        } catch (Exception err) {
            // bad clock string
            return null;
        }
    }
}
