package ds.assignment.multicast;

import ds.assignment.multicast.lamport.AckEvent;
import ds.assignment.multicast.lamport.GetEvent;
import ds.assignment.multicast.lamport.LamportEvent;
import ds.assignment.multicast.lamport.PutEvent;
import ds.assignment.poissonjob.PoissonJobScheduler;
import generated.LamportMsgHandler.Ack;
import generated.LamportMsgHandler.Empty;
import generated.LamportMsgHandler.Get;
import generated.LamportMsgHandler.Put;
import generated.lamportMsgHandlerGrpc;
import generated.lamportMsgHandlerGrpc.lamportMsgHandlerBlockingStub;
import generated.lamportMsgHandlerGrpc.lamportMsgHandlerImplBase;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * <p>
 * Multicast service using Lamport Clocks to implement Multicast Total Ordering.
 * Stores and retrieves keys in a database.
 * </p>
 */
public class MulticastService extends lamportMsgHandlerImplBase {
  private String hostAddr; // Server's IPv4.
  private List<String> hosts; // IPv4 of all other hosts.
  private final int pid; // Process Identifier.
  private volatile long logical_clock; // Server's Lamport Clock.
  static int eventCounter = 0; // Uniquely identifies events from a certain PID. Increased for each event
                               // created locally.
  /**
   * Queue used to delay events before they are delivered to the app.
   * No need for volatile keyword since PriorityBlockingQueue is already a
   * thread-safe class.
   */
  private final PriorityBlockingQueue<LamportEvent> delayQueue = new PriorityBlockingQueue<>();
  public static final int PORT = 6668; // Port Listening for gRPC connections.
  private final HashMap<String, ManagedChannel> channels = new HashMap<>();
  private final ConcurrentLinkedQueue<LamportEvent> eventsToSend = new ConcurrentLinkedQueue<>();
  private final Random rand = new Random();
  private final ConcurrentLinkedQueue<LamportEvent> deliveredQueue = new ConcurrentLinkedQueue<>();
  private Thread poissonReqGenerator;

  /**
   * Lambda of the poissonReqGenerator Thread.
   * Currently on average generates 1 event per 30 seconds.
   */
  private static final double LAMBDA = 1 / (double) 30;

  public MulticastService(String hostAddr, int pid, boolean startPoissonThread, List<String> hosts) {
    this.hostAddr = hostAddr;
    this.hosts = hosts;
    this.pid = pid;
    logical_clock = 0;
    establishChannels();
    System.out.println("Established channels with all the hosts");
    Thread senderThread = senderThread();
    senderThread.start();
    System.out.println("Started Sender Thread");
    Thread stdinThread = stdinThread();
    stdinThread.start();
    System.out.println("Started Input Thread");
    Thread delayQueueManagerThread = delayQueueManagerThread();
    delayQueueManagerThread.start();
    System.out.println("Started Delay Queue Manager Thread");
    poissonReqGenerator = poissonRequestsGenerator(LAMBDA);
    if (startPoissonThread) {
      poissonReqGenerator.start();
      System.out.println("Started Poisson Thread");
    }

  }

  /**
   * Established channels with all the hosts.
   */
  private void establishChannels() {
    for (String host : hosts) {
      // Connect to the host.
      var channel = ManagedChannelBuilder
          .forAddress(host, PORT)
          .usePlaintext()
          .keepAliveWithoutCalls(true)
          .build();
      channels.put(host, channel);
    }
    var channel = ManagedChannelBuilder
        .forAddress(hostAddr, PORT)
        .usePlaintext()
        .build();
    channels.put(hostAddr, channel);
  }

  private long getLogical_clock() {
    return logical_clock;
  }

  /**
   * Ack request. gRPC
   */
  @Override
  public void ackEvent(Ack request, StreamObserver<Empty> responseObserver) {
    var rcvPid = request.getPid();
    var rcvClock = request.getClock();
    var eventID = request.getEventId();
    var eventPID = request.getEventPid();
    var event = new AckEvent(rcvPid, rcvClock, eventID, eventPID);
    onRcvClockUpdate(rcvClock);
    delayQueue.add(event);
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted();
  }

  /**
   * Get request. gRPC
   */
  @Override
  public void getValue(Get request, StreamObserver<Empty> responseObserver) {
    var rcvPid = request.getPid();
    var rcvClock = request.getClock();
    var eventID = request.getEventId();
    var newClock = onRcvClockUpdate(rcvClock);
    var event = new GetEvent(rcvPid, rcvClock, eventID, request.getKey());
    newClock = onSndClockUpdate();
    eventsToSend.add(new AckEvent(pid, newClock, eventID, rcvPid));
    delayQueue.add(event);
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
        request.getValue());
    newClock = onSndClockUpdate();
    eventsToSend.add(new AckEvent(pid, newClock, eventID, rcvPid));
    delayQueue.add(event);
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

  private void deliverEvent(LamportEvent event) {
    deliveredQueue.add(event);
    System.out.println("Delivered:" + event);
  }

  /**
   * <p>
   * Manages the {@link #delayQueue} based on the type of event that is at the end
   * of the queue.
   * </p>
   * <p>
   * Event Type:
   * <ul>
   * <li>{@link ds.assignment.multicast.lamport.AckEvent}: Remove the event from
   * the delay queue.
   * <li>{@link ds.assignment.multicast.lamport.GetEvent} or
   * {@link ds.assignment.multicast.lamport.PutEvent}: Delivers an event and
   * removes it from the queue if we have a matching Event for each host in the
   * network.
   * <li>Any Other Event: Prints to stderr.
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

              // Lock the queue
              synchronized (delayQueue) {
                var event = delayQueue.peek();
                if (event == null)
                  continue; // Delay queue is empty
                // Create a set with the PIDs of all servers.
                HashSet<Integer> hostsPID = new HashSet<>();
                for (int i = 0; i < hosts.size(); i++) {
                  hostsPID.add(i);
                }

                // Remove event pid from the set.
                // hostsPID.remove(event.getPid());
                // hostsPID.remove(pid);

                var it = delayQueue.iterator();
                it.next(); // Ignore the head of the queue
                while (it.hasNext()) {
                  var eventInQueue = it.next();
                  hostsPID.remove(eventInQueue.getPid());
                }
                // if there exists a msg for each server then we can remove this event
                if (hostsPID.isEmpty()) {
                  // Only deliver Put and Get Events
                  if (event instanceof GetEvent || event instanceof PutEvent) {
                    onDeliveryClockUpdate();
                    deliverEvent(event);
                  }
                  delayQueue.remove(event);
                }
              }
            }
          }
        });
  }

  /**
   * Thread responsible for any outgoing messages. The reason why this thread must
   * be responsible for sending msg is to assure a FIFO channel.
   */
  private Thread senderThread() {
    return new Thread(
        new Runnable() {
          public void run() {
            while (true) {
              var evToSend = eventsToSend.poll();
              if (evToSend != null) {
                sendEvent(evToSend);
                // System.out.println("Sent " + evToSend);
              }
            }
          }
        });
  }

  /**
   * <p>
   * Sends a event that extends the
   * {@link ds.assignment.multicast.lamport.LamportEvent} via multicast.
   * </p>
   * <p>
   * Currently supported events are:
   * <ul>
   * <li>{@link ds.assignment.multicast.lamport.AckEvent}
   * <li>{@link ds.assignment.multicast.lamport.PutEvent}
   * <li>{@link ds.assignment.multicast.lamport.GetEvent}
   * </ul>
   * </p>
   * 
   * @param event
   */
  private void sendEvent(LamportEvent event) {
    if (event instanceof AckEvent) {
      for (String target : hosts)
        sendAck(target, (AckEvent) event);
      sendAck(hostAddr, (AckEvent) event);
    } else if (event instanceof GetEvent) {
      sendGet(hostAddr, (GetEvent) event);
      for (String target : hosts)
        sendGet(target, (GetEvent) event);
    } else if (event instanceof PutEvent) {
      sendPut(hostAddr, (PutEvent) event);
      for (String target : hosts)
        sendPut(target, (PutEvent) event);
    } else {
      System.err.println(
          "Unexpected instance of " + LamportEvent.class.getName() + " Received!");
      return;
    }
  }

  /**
   * Connect to the {@code host} and send him the Ack msg. On Failure case retry.
   * 
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
   * 
   * @param host
   * @param ackMsg
   */
  private void sendAck(String host, Ack ackMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = channels.get(host);
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
            channel);

        handlerStub.ackEvent(ackMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Connect to the {@code host} and send him the Get msg. On Failure case retry.
   * 
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
   * 
   * @param host
   * @param getMsg
   */
  private void sendGet(String host, Get getMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = channels.get(host);
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
            channel);

        handlerStub.getValue(getMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Connect to the {@code host} and send him the Put msg. On Failure case retry.
   * 
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
   * 
   * @param host
   * @param putMsg
   */
  private void sendPut(String host, Put putMsg) {
    // Connect to the host and send him the Ack msg. On Failure case retry.
    var channel = channels.get(host);
    while (true) {
      try {
        lamportMsgHandlerBlockingStub handlerStub = lamportMsgHandlerGrpc.newBlockingStub(
            channel);

        handlerStub.putValue(putMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
      }
    }
  }

  /**
   * Thread responsible for executing commands.
   * Gathers input from stdin, accepts the following commands:
   * <ul>
   * <li>"send": Generates a random request.
   * <li>"queue": Prints the delay queue (All request that have not yet been delivered/discarded).
   * <li>"delivered": Prints all delivered requests.
   * <li>"startPoisson": Starts the poisson request generator (Assuming it was not started by default).
   * <li>"pausePoisson": Pauses the poisson request generator.
   * <li>"resumePoisson": Resumes the poisson request generator.
   * </ul>
   */
  private Thread stdinThread() {
    return new Thread(
        new Runnable() {

          public void run() {
            var in = new Scanner(System.in);
            while (in.hasNext()) {
              switch (in.next()) {
                case "send":
                  generateRandomEvent();
                  break;
                case "queue":
                  var sortedArray = delayQueue.toArray();
                  Arrays.sort(sortedArray);
                  System.out.println(Arrays.toString(sortedArray));
                  break;
                case "delivered":
                  System.out.println("Delivered: " + deliveredQueue);
                  break;
                case "startPoisson":
                  if (!poissonReqGenerator.isAlive()) {
                    poissonReqGenerator.start();
                    System.out.println("Started Poisson Thread");
                  } else
                    System.out.println("Poisson Thread is already alive");
                  break;
                case "pausePoisson":
                  poissonReqGenerator.suspend();
                  System.out.println("Paused Thread");
                  break;
                case "resumePoisson":
                  poissonReqGenerator.resume();
                  System.out.println("Resumed Thread");
                  break;
                default:
                  System.out.println("Invalid Command");
                  break;
              }
            }
            in.close();
          }
        });
  }

  /**
   * Generates random requests at a poisson distribution rate
   * 
   * @param LAMBDA Lambda value of the poisson distribution
   * @return
   */
  private Thread poissonRequestsGenerator(double LAMBDA) {
    var scheduler = new PoissonJobScheduler(LAMBDA, rand, new SendRequests(this));
    return scheduler.schedulerThread();
  }

  /**
   * Generate a random Put request.
   */
  public void generateRandomEvent() {
    long updatedClock = onSndClockUpdate(); // Update Lamport Clock
    LamportEvent ev;
    ev = new PutEvent(pid,
        updatedClock,
        eventCounter,
        rand.nextInt(5000),
        "POISSON");
    eventsToSend.add(ev);
    eventCounter++;

  }

  public static void main(String[] args)
      throws IOException, InterruptedException {
    var args_ = Arrays.asList(args);

    if (args_.size() < 4) {
      System.err.println(
          MulticastService.class.getName() +
              " HOST_ADDRESS STATE_BOOLEAN PID [PEERS_ADDRESSES]");
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
          poissonState,
          args_.subList(3, args_.size()));
      var server = NettyServerBuilder
          .forAddress(new InetSocketAddress(args[0], PORT))
          .addService(service)
          .build()
          .start();
      System.out.println(
          "Started Multicast Service at " + args[0] + ":" + PORT);
      server.awaitTermination();
    }
  }

}
