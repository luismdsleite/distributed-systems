package ds.assignment.gossiping;

import ds.assignment.HostChecker;
import ds.assignment.msgHandler;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Gossiping {
  private InetAddress hostAddr; // IP where this server is hosted on.
  private volatile List<String> hostsSet = new Vector<>();
  private volatile Set<String> wordsSet = new HashSet<>();
  private volatile Set<String> bannedWordsSet = new HashSet<>();
  msgHandler handler; // Handles sending of messages via the Transport Layer.
  public static final int PORT = 6666; // Port server is being hosted on.
  public static final int MAX_MSG_LENGTH = 512; // Maximum msg size (in bytes)
  private static final Random random = new Random();
  private double msgDropChance = 0.20; // Chance of a msg already present in the wordsSet to be dropped.
  private int gossipRate = 3; // How many hosts to gossip at a time.

  public Gossiping(InetAddress hostAddr) {
    this.hostAddr = hostAddr;
    // handler = new numMsgHandlerTCP();
    var inputThread = stdinThread();
    this.start();
    connReceiver().start();
    inputThread.start();
  }

  public Gossiping(InetAddress hostAddr, List<String> registeredUsers) {
    this(hostAddr);
    this.hostsSet.addAll(registeredUsers);
  }

  /**
   * Infinite Loop. Receive the token, execute some work, send the token, repeat.
   */
  private void start() {
    System.out.println("Starting " + hostAddr);
  }

  /**
   * <p>
   * Thread responsible for receiving user input. Gathers input from stdin.
   * </p>
   *
   * Commands:
   * <ul>
   * <li><b>register {ipv4}/unregister {ipv4}</b>: Adds/removes the host.
   * <li><b>send {ipv4} {msg}</b>: Sends a msg to a certain host (Used for testing
   * purposes).
   * <li><b>get all</b>: Equivalent to executing all get commands.
   * <li><b>get words</b>: List of the received words.
   * <li><b>get hosts</b>: List of all registered hosts.
   * <li><b>get banned</b>: List of words that are not propagated.
   * <li><b>get gossipRate</b>: nº of randomly chosen hosts a message is
   * propagated to.
   * <li><b>get dropChance</b>: chance of a word retransmission being ignored.
   * <li><b>set gossipRate</b>: changes gossipRate param.
   * <li><b>set dropChance</b>: changes dropChance param.
   * </ul>
   */
  private Thread stdinThread() {
    return new Thread(
        new Runnable() {

          public void run() {
            var in = new Scanner(System.in);
            while (in.hasNext()) {
              String cmd = in.next();
              String arg = in.next();
              // Possible commands
              switch (cmd) {
                case "register":
                  // Checking if the host is in a valid ipv4 format.
                  if (!HostChecker.validate(arg)) {
                    System.out.println("Host: " + arg + " is not a valid host name");
                    continue;
                  }
                  if (hostsSet.contains(arg)) {
                    System.out.println(
                        "Host " + arg + " was already registered");
                  } else {
                    hostsSet.add(arg);
                    System.out.println("Added Host " + arg);
                  }
                  continue;
                case "unregister":
                  // Checking if the host is in a valid ipv4 format.
                  if (!HostChecker.validate(arg)) {
                    System.out.println("Host: " + arg + " is not a valid host name");
                    continue;
                  }
                  if (!hostsSet.remove(arg)) {
                    System.out.println("Host " + arg + " is not registered");
                  } else {
                    System.out.println("Removed Host " + arg);
                  }
                  continue;
                case "send":
                  // Checking if the host is in a valid ipv4 format.
                  if (!HostChecker.validate(arg)) {
                    System.out.println("Host: " + arg + " is not a valid host name");
                    continue;
                  }
                  try {
                    String msg = in.next();
                    DatagramChannel client = DatagramChannel.open();
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    client.send(buffer, new InetSocketAddress(arg, PORT));
                  } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                  continue;
                case "set":
                  switch (arg) {
                    case "gossipRate":
                      gossipRate = in.nextInt();
                      System.out.println("Gossip Rate = " + gossipRate);
                      continue;
                    case "dropChance":
                      msgDropChance = in.nextDouble();
                      System.out.println("msgDropChance = " + msgDropChance);
                      continue;
                    default:
                      System.err.println("Invalid command: " + cmd + " " + arg);
                      continue;
                  }
                case "get":
                  switch (arg) {
                    case "hosts":
                      System.out.println(hostsSet);
                      continue;
                    case "words":
                      System.out.println(wordsSet);
                      continue;
                    case "banned":
                      System.out.println(bannedWordsSet);
                      continue;
                    case "gossipRate":
                      System.out.println(gossipRate);
                      continue;
                    case "dropChance":
                      System.out.println(msgDropChance);
                      continue;
                    case "all":
                      System.out.println("Hosts = " + hostsSet);
                      System.out.println("Words = " + wordsSet);
                      System.out.println("Ignored Words (not propagated) = " + bannedWordsSet);
                      System.out.println("Gossip Rate = " + gossipRate);
                      System.out.println("Msg Drop Chance = " + msgDropChance);
                      continue;
                    default:
                      System.err.println("Invalid command: " + cmd + " " + arg);
                      continue;
                  }
                default:
                  System.err.println("Invalid Command:" + cmd + " " + arg);
                  break;
              }
            }
            in.close();
          }
        });
  }

  /**
   * Thread responsible for receiving messages and propagating them to other hosts
   * via the Gossip Protocol.
   * 
   * @return
   */
  private Thread connReceiver() {
    return new Thread(
        new Runnable() {
          private DatagramChannel server;

          public void run() {
            try {
              // Start the Server
              InetSocketAddress address = new InetSocketAddress(hostAddr, PORT);
              server = DatagramChannel.open().bind(address);
              ByteBuffer buffer = ByteBuffer.allocate(MAX_MSG_LENGTH);

              // Await messages.
              while (true) {
                var origin = (InetSocketAddress) server.receive(buffer);
                String msg = extractMessage(buffer);
                System.out.println("Received msg " + msg + " from " + origin.getAddress());
                parseMessage(msg);
                buffer.clear();
              }
            } catch (IOException e) {
              e.printStackTrace();
              System.err.println("Unable to start Server");
            }
          }

          // Get a String from the buffer
          private static String extractMessage(ByteBuffer buffer) {
            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String msg = new String(bytes);
            buffer.flip();
            return msg;
          }

          // Receive the msg and gossip to registered hosts.
          private void parseMessage(String msg) {
            // If the word was already added, then {msgDropChance}% of the times we don't
            // propagate it.
            var b = !wordsSet.add(msg);
            if (bannedWordsSet.contains(msg)) {
              return;
            }
            if (b && random.nextDouble() < msgDropChance) {
              System.out.println("Dropping already received word: " + msg);
              bannedWordsSet.add(msg);
              return;
            }

            List<String> hostsToGossip = new ArrayList<>(gossipRate);

            // this is to prevent an exception if the nº of registered hosts < gossipRate
            int gossipsNum = Math.min(gossipRate, hostsSet.size());
            if (gossipsNum <= 0)
              return;
            // Get a random host and add it to the hostsToGossip list. If we already chose
            // that host get a new one.
            for (int i = 0; i < gossipsNum; i++) {
              var possibleHost = hostsSet.get(random.nextInt(hostsSet.size()));
              if (hostsToGossip.contains(possibleHost))
                i--;
              else
                hostsToGossip.add(possibleHost);
            }

            for (String target : hostsToGossip) {
              try {
                System.out.println("Sending " + msg + " to " + target);
                server.send(
                    ByteBuffer.wrap(msg.getBytes()),
                    new InetSocketAddress(target, PORT));
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        });
  }

  /**
   * Starts a Gossiping server on "{@value args[0]}:{@value #PORT}"
   * 
   * @param args Host Address
   * @throws UnknownHostException
   */
  public static void main(String[] args) throws UnknownHostException {
    var args_ = Arrays.asList(args);
    if (args_.size() == 0) {
      System.err.println("Class " + Gossiping.class.getName() + " requires at least 1 argument.");
      return;
    }

    var host = InetAddress.getByName(args[0]);
    if (args_.size() == 1) {
      new Gossiping(host);
    } else {
      new Gossiping(host, args_.subList(1, args_.size()));
    }
  }
}
