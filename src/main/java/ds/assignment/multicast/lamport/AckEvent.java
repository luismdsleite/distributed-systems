package ds.assignment.multicast.lamport;

public class AckEvent extends LamportEvent {

  private int eventPID;

  public int getEventPID() {
    return eventPID;
  }

  public AckEvent(int pid, long logical_clock, long eventID, int eventPID) {
    super(pid, logical_clock, eventID);
    this.eventPID = eventPID;
  }

  @Override
  public String toString() {
    return (
      "Ack Event ID:" +
      super.getEventID() +
      " Event_PID:" +
      super.getEventID() +
      " PID:" +
      this.getPid() +
      " Clock:" +
      this.getLogical_clock()
    );
  }
}
