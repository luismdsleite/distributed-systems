package ds.assignment.multicast.lamport;

public class GetEvent extends LamportEvent {
  private long key;

  public long getKey() {
    return key;
  }

  public GetEvent(int pid, long logical_clock, long eventID, long key) {
    super(pid, logical_clock, eventID);
    this.key = key;
  }

  @Override
  public String toString() {
    return (
      "Get Event ID:" +
      this.getEventID() +
      " PID:" +
      this.getPid() +
      " Clock:" +
      this.getLogical_clock() +
      " Key: " +
      key
    );
  }
}
