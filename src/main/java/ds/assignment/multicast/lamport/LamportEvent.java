package ds.assignment.multicast.lamport;

public class LamportEvent implements Comparable<LamportEvent> {
  private int pid; // Server which the event originated from.
  private long logical_clock; // Lamport Clock
  private long eventID; // the <pid,eventID> pair works as a ID for each event. 

  public long getEventID() {
    return eventID;
  }

  public int getPid() {
    return pid;
  }

  public long getLogical_clock() {
    return logical_clock;
  }

  public LamportEvent(int pid, long logical_clock, long eventID) {
    this.pid = pid;
    this.logical_clock = logical_clock;
    this.eventID = eventID;
  }

  /**
   * Comparison is based on the {@code logical_clock} value, {@code pid} acts as a
   * tie-breaker.
   */
  @Override
  public int compareTo(LamportEvent arg0) {
    // We cant simply subtract the pids/logical_clocks since these variables are of
    // type long and the compareTo method returns an int.
    if (logical_clock == arg0.logical_clock) {
      if (pid > arg0.pid) return 1;
      if (pid < arg0.pid) return -1; else return 0;
    }
    if (logical_clock > arg0.logical_clock) return 1;
    if (logical_clock < arg0.logical_clock) return -1;
    return 0;
  }

  @Override
  public String toString() {
    return "Generic Event " + eventID + " " + pid + " " + logical_clock;
  }
}
