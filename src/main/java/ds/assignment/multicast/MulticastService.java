package ds.assignment.multicast;

import ds.assignment.multicast.lamport.AckEvent;
import ds.assignment.multicast.lamport.GetEvent;
import ds.assignment.multicast.lamport.LamportEvent;
import ds.assignment.multicast.lamport.PutEvent;
import generated.LamportMsgHandler.Ack;
import generated.LamportMsgHandler.Empty;
import generated.LamportMsgHandler.Get;
import generated.LamportMsgHandler.Put;
import generated.lamportMsgHandlerGrpc;
import generated.lamportMsgHandlerGrpc.lamportMsgHandlerBlockingStub;
import generated.lamportMsgHandlerGrpc.lamportMsgHandlerImplBase;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * <p>
 * Multicast service using Lamport Clocks to implement Multicast Total Ordering. Stores and retrieves keys in a database.
 * </p>
 */
public class MulticastService extends lamportMsgHandlerImplBase {
  private String hostAddr; // Server's IPv4.
  private List<String> hosts; // IPv4 of all other hosts.
  private final int pid; // Process Identifier.
  private volatile long logical_clock; // Server's Lamport Clock.
  static int eventCounter = 0; // Uniquely identifies events from a certain PID. Increased for each event created locally.
  /**
   * Queue used to delay events before they are delivered to the app.
   * No need for volatile keyword since PriorityBlockingQueue is already a thread-safe class.
   */
  private final PriorityBlockingQueue<LamportEvent> delayQueue = new PriorityBlockingQueue<>();
  public static final int PORT = 6668; // Port Listening for gRPC connections.

  public MulticastService(String hostAddr, int pid, List<String> hosts) {
    this.hostAddr = hostAddr;
    this.hosts = hosts;
    this.pid = pid;
    logical_clock = 0;
    Thread stdinThread = stdinThread();
    stdinThread.start();
    Thread delayQueueManagerThread = delayQueueManagerThread();
    delayQueueManagerThread.start();
  }

  public long getLogical_clock() {
    return logical_clock;
  }

  /**
   * Ack request.
   */
  @Override
  public void ackEvent(Ack request, StreamObserver<Empty> responseObserver) {
    var rcvPid = request.getPid();
    var rcvClock = request.getClock();
    var eventID = request.getEventId();
    var event = new AckEvent(rcvPid, rcvClock, eventID);
    onRcvClockUpdate(rcvClock);
    delayQueue.add(event);
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  /**
   * Get request.
   */
  @Override
  public void getValue(Get request, StreamObserver<Empty> responseObserver) {
    var rcvPid = request.getPid();
    var rcvClock = request.getClock();
    var eventID = request.getEventId();
    var newClock = onRcvClockUpdate(rcvClock);
    var event = new GetEvent(rcvPid, rcvClock, eventID, request.getKey());
    delayQueue.add(event);
    sendAcks(rcvPid, newClock, eventID);
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  /**
   * Put request
   */
  @Override
  public void putValue(Put request, StreamObserver<Empty> responseObserver) {
    var rcvPid = request.getPid();
    var rcvClock = request.getClock();
    var eventID = request.getEventId();
    var newClock = onRcvClockUpdate(rcvClock);
    var event = new PutEvent(
      rcvPid,
      rcvClock,
      eventID,
      request.getKey(),
      request.getValue()
    );
    delayQueue.add(event);
    sendAcks(rcvPid, newClock, eventID);
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  /**
   * Update the Lamport clock on a msg receive.
   *
   * @param logical_clock
   * @return updated Lamport clock.
   */
  private long onRcvClockUpdate(long logical_clock) {
    return (this.logical_clock = Math.max(this.logical_clock, logical_clock));
  }

  /**
   * Update the Lamport clock on a msg send.
   *
   * @param logical_clock
   * @return updated Lamport clock.
   */
  private long onSndClockUpdate() {
    return (this.logical_clock += 1);
  }

  /**
   * Update the Lamport clock on a msg delivery.
   *
   * @param logical_clock
   * @return updated Lamport clock.
   */
  private long onDeliveryClockUpdate() {
    return (this.logical_clock += 1);
  }

  /**
   * Sends a Ack msg to all hosts in the {@link #hosts} list, it also adds the Ack msg to the {@link #delayQueue}.
   * @param ackPid
   * @param ackClock
   * @param eventId
   */
  private void sendAcks(int ackPid, long ackClock, long eventId) {
    // Add Ack Event to our own delay queue.
    delayQueue.add(new AckEvent(ackPid, ackClock, eventId));

    // Create the Ack Msg and send it to every host.
    Ack ackMsg = Ack
      .newBuilder()
      .setPid(ackPid)
      .setClock(ackClock)
      .setEventId(eventId)
      .build();
    for (String target : hosts) {
      // Connect to the target and send him the Ack msg. On Failure case retry.
      sendAck(target, ackMsg);
    }
  }

  private void deliverEvent(LamportEvent event) {
    System.out.println("Delivered:" + event);
  }

  /**
   * <p>
   * Manages the {@link #delayQueue} based on the type of event that is at the end of the queue.
   * </p>
   * <p>
   * Event Type:
   * <ul>
   * <li> {@link ds.assignment.multicast.lamport.AckEvent}: Remove the event from the delay queue.
   * <li> {@link ds.assignment.multicast.lamport.GetEvent} or {@link ds.assignment.multicast.lamport.PutEvent}: Delivers an event and removes it from the queue if we have a matching Ack Event for each host in the queue.
   * <li> Any Other Event: Prints to stderr.
   * <ul>
   * </p>
   *
   * @return The Thread responsible for managing the delay queue.
   */
  private Thread delayQueueManagerThread() {
    return new Thread(
      new Runnable() {

        public void run() {
          while (true) {
            // TODO: Check if its possible for an event to appear at the head of a queue. If it is then dont use .peek() followed by .take() since the queue may have changed by then.
            var event = delayQueue.peek();
            if (event == null) continue; // Delay queue is empty

            // If we receive an Ack Event simply remove it
            if (event instanceof AckEvent) {
              delayQueue.remove(event);
              continue;
            }
            // Block the queue
            synchronized (delayQueue) {
              // Put or Get Events
              if (event instanceof PutEvent || event instanceof GetEvent) {
                // Create a set with the PIDs of all servers.
                HashSet<Integer> hostsPID = new HashSet<>();
                for (int i = 0; i < hosts.size(); i++) {
                  hostsPID.add(i);
                }
                // Remove my own pid from the set.
                hostsPID.remove(pid);

                var it = delayQueue.iterator();
                it.next(); // Ignore the head of the queue
                while (it.hasNext()) {
                  var eventInQueue = it.next();
                  if (eventInQueue.getEventID() == event.getEventID()) {
                    hostsPID.remove(eventInQueue.getPid());
                  }
                }
                // If HostsPID is empty then that means we have an ACK for each server in the system.
                if (hostsPID.isEmpty()) {
                  onDeliveryClockUpdate();
                  delayQueue.remove(event);
                  deliverEvent(event);
                }
                continue;
              }
            }
            // Unreachable!
            System.err.println(
              "Unexpected instance of " +
              LamportEvent.class.getName() +
              " Received!"
            );
          }
        }
      }
    );
  }

  /**
   * <p> Sends a event that extends the {@link ds.assignment.multicast.lamport.LamportEvent}.</p>
   * <p>
   * Currently supported events are:
   * <ul>
   * <li> {@link ds.assignment.multicast.lamport.AckEvent}
   * <li> {@link ds.assignment.multicast.lamport.PutEvent}
   * <li> {@link ds.assignment.multicast.lamport.GetEvent}
   * </ul>
   * </p>
   * @param event
   */
  public void sendEvent(LamportEvent event) {
    if (event instanceof AckEvent) {
      sendAck(hostAddr, (AckEvent) event);
      for (String target : hosts) sendAck(target, (AckEvent) event);
    } else if (event instanceof GetEvent) {
      sendGet(hostAddr, (GetEvent) event);
      for (String target : hosts) sendGet(target, (GetEvent) event);
    } else if (event instanceof PutEvent) {
      sendPut(hostAddr, (PutEvent) event);
      for (String target : hosts) sendPut(target, (PutEvent) event);
    } else {
      System.err.println(
        "Unexpected instance of " + LamportEvent.class.getName() + " Received!"
      );
      return;
    }
  }

  /**
   * Connect to the {@code host} and send him the Ack msg. On Failure case retry.
   * @param host
   * @param ev
   */
  private void sendAck(String host, AckEvent ev) {
    // Create the Ack Msg and send it to every host.
    Ack ackMsg = Ack
      .newBuilder()
      .setPid(ev.getPid())
      .setClock(ev.getLogical_clock())
      .setEventId(ev.getEventID())
      .build();
    sendAck(host, ackMsg);
  }

  /**
   * Connect to the {@code host} and send him the Ack msg. On Failure case retry.
   * @param host
   * @param ackMsg
   */
  private void sendAck(String host, Ack ackMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = ManagedChannelBuilder
      .forAddress(host, PORT)
      .usePlaintext()
      .build();
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
          channel
        );

        handlerStub.ackEvent(ackMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Connect to the {@code host} and send him the Get msg. On Failure case retry.
   * @param host
   * @param ev
   */
  private void sendGet(String host, GetEvent ev) {
    // Create the Ack Msg and send it to every host.
    Get getMsg = Get
      .newBuilder()
      .setPid(ev.getPid())
      .setClock(ev.getLogical_clock())
      .setEventId(ev.getEventID())
      .setKey(ev.getKey())
      .build();
    sendGet(host, getMsg);
  }

  /**
   * Connect to the {@code host} and send him the Get msg. On Failure case retry.
   * @param host
   * @param getMsg
   */
  private void sendGet(String host, Get getMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = ManagedChannelBuilder
      .forAddress(host, PORT)
      .usePlaintext()
      .build();
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
          channel
        );

        handlerStub.getValue(getMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Connect to the {@code host} and send him the Put msg. On Failure case retry.
   * @param host
   * @param ev
   */
  private void sendPut(String host, PutEvent ev) {
    // Create the Ack Msg and send it to every host.
    Put putMsg = Put
      .newBuilder()
      .setPid(ev.getPid())
      .setClock(ev.getLogical_clock())
      .setEventId(ev.getEventID())
      .setKey(ev.getKey())
      .setValue(ev.getValue())
      .build();
    sendPut(host, putMsg);
  }

  /**
   * Connect to the {@code host} and send him the Put msg. On Failure case retry.
   * @param host
   * @param putMsg
   */
  private void sendPut(String host, Put putMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = ManagedChannelBuilder
      .forAddress(host, PORT)
      .usePlaintext()
      .build();
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
          channel
        );

        handlerStub.putValue(putMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Thread responsible for managing the lock/unlock TokenRing state.
   * Gathers input from stdin, accepts the following commands:
   * <ul>
   * <li>"lock"/"unlock": locks/unlocks the token.
   * <li>"startToken": Used to create the token, it create a token and sends it to
   * itself.
   * </ul>
   */
  private Thread stdinThread() {
    return new Thread(
      new Runnable() {
        Random rand = new Random();

        public void run() {
          var in = new Scanner(System.in);
          while (in.hasNext()) {
            switch (in.next()) {
              case "send":
                var ev = new GetEvent(
                  pid,
                  logical_clock,
                  eventCounter,
                  rand.nextInt(5000)
                );
                sendEvent(ev);
                eventCounter++;
                break;
              case "queue":
                System.out.println("Queue: " + delayQueue);
                break;
              default:
                break;
            }
          }
          in.close();
        }
      }
    );
  }

  public static void main(String[] args)
    throws IOException, InterruptedException {
    var args_ = Arrays.asList(args);

    if (args_.size() != 4) {
      System.err.println(
        MulticastService.class.getName() +
        " HOST_ADDRESS STATE_BOOLEAN PID [PEERS_ADDRESSES]"
      );
    } else {
      boolean poissonState;
      if (args_.get(1).equals("true")) {
        poissonState = true;
      } else if (args_.get(1).equals("false")) {
        poissonState = false;
      } else {
        System.err.println("STATE_BOOLEAN must be true or false");
        return;
      }
      
      var service = new MulticastService(
        args[0],
        Integer.parseInt(args[2]),
        args_.subList(3, args_.size())
      );
      var server = NettyServerBuilder
        .forAddress(new InetSocketAddress(args[0], PORT))
        .addService(service)
        .build()
        .start();
      System.out.println(
        "Started Multicast Service at " + args[0] + ":" + PORT
      );
      server.awaitTermination();
    }
  }
}
