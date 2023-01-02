package ds.assignment.multicast.lamport;

public class PutEvent extends LamportEvent {
  private String value;
  private long key;

  public String getValue() {
    return value;
  }

  public long getKey() {
    return key;
  }

  public PutEvent(
    int pid,
    long logical_clock,
    long eventID,
    long key,
    String value
  ) {
    super(pid, logical_clock, eventID);
    this.key = key;
    this.value = value;
  }

  @Override
  public String toString() {
    return (
      "Put Event ID:" +
      this.getEventID() +
      " PID:" +
      this.getPid() +
      " Clock:" +
      this.getLogical_clock() +
      " Key: " +
      this.getKey() +
      " Value:" +
      this.getValue()
    );
  }
}
