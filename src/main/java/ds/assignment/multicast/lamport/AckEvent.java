package ds.assignment.multicast.lamport;

public class AckEvent extends LamportEvent {

  public AckEvent(int pid, long logical_clock, long eventID) {
    super(pid, logical_clock, eventID);
  }

  @Override
  public String toString() {
    return (
      "Ack Event " +
      super.getEventID() +
      " " +
      this.getPid() +
      " " +
      this.getLogical_clock()
    );
  }
}
