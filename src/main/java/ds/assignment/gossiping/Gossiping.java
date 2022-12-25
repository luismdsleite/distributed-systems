package ds.assignment.gossiping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

import ds.assignment.HostChecker;
import ds.assignment.PoissonJobScheduler;
import ds.assignment.msgHandler;

/**
 * <p>
 * Server running a Gossip Protocol.
 * <p>
 * Composed by 3 threads:
 * <ul>
 * <li>stdinThread: Receives input from user to execute a certain command (check
 * #stdinThread method for details)</li>
 * <li>poissonWordsGenerator: Injects random words onto the network to simulate
 * meta information of servers</li>
 * <li>connReceiver: Propagates received words via gossip protocol</li>
 * </ul>
 */
public class Gossiping {
  private InetAddress hostAddr; // IP where this server is hosted on.

  // Connection Receiver Thread parameters.
  private volatile List<String> hostsSet = new Vector<>();
  private volatile Set<String> wordsSet = new HashSet<>();
  private volatile Set<String> bannedWordsSet = new HashSet<>();
  msgHandler handler; // Handles sending of messages via the Transport Layer.
  public static final int PORT = 6666; // Port server is being hosted on.
  public static final int MAX_MSG_LENGTH = 512; // Maximum msg size (in bytes)
  private static final Random random = new Random();

  // Input Thread parameters.
  private double msgDropChance = 0.20; // Chance of a msg already present in the wordsSet to be dropped.
  private int gossipRate = 3; // How many hosts to gossip at a time.

  // Poisson Words Generator Thread parameters.
  private static final double LAMBDA = 1 / (double) 30; // Lambda of the poissonWordsGenerator. Currently on average
  // generates 1 event per 30 seconds.
  // every minute
  private static final File WORDS_FILE = new File("resources/4000-most-common-english-words-csv.csv"); // File
  // containing
  // the words used
  // by the thread.

  public Gossiping(InetAddress hostAddr, boolean startPoisson) {
    this.hostAddr = hostAddr;
    // handler = new numMsgHandlerTCP();
    try {
      start(startPoisson);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public Gossiping(InetAddress hostAddr, List<String> registeredUsers, boolean startPoisson) {
    this(hostAddr, startPoisson);
    this.hostsSet.addAll(registeredUsers);
  }

  public Gossiping(InetAddress hostAddr, List<String> registeredUsers) {
    this(hostAddr, registeredUsers, true);
  }

  /**
   * Starts all the threads.
   * 
   * @throws IOException
   */
  private void start(boolean startPoisson) throws IOException {
    System.out.println("Starting " + hostAddr);
    var inputThread = stdinThread();
    connReceiver().start();
    inputThread.start();
    if (startPoisson)
      poissonWordsGenerator();
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
                    client.close();
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
                      System.err.println("Invalid command: " + cmd + " " + arg + "\n"
                          + "use \"get help\" to see all available commands");
                      continue;
                  }
                case "get":
                  switch (arg) {
                    case "help":
                      System.out.println("Available Commands" + "\n" +
                          "\t" + "register {ipv4}/unregister {ipv4}" + "\n" +
                          "\t" + "send {ipv4} {msg}" + "\n" +
                          "\t" + "get all" + "\n" +
                          "\t" + "get words" + "\n" +
                          "\t" + "get hosts" + "\n" +
                          "\t" + "get banned" + "\n" +
                          "\t" + "get gossipRate" + "\n" +
                          "\t" + "get dropChance" + "\n" +
                          "\t" + "set gossipRate" + "\n" +
                          "\t" + "set dropChance");
                      continue;
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
                      System.out.println("No. of Hosts = " + hostsSet.size());
                      System.out.println("Hosts = " + hostsSet);
                      System.out.println("No. of Words = " + wordsSet.size());
                      System.out.println("Words = " + wordsSet);
                      System.out.println("No. of Banned Words = " + bannedWordsSet.size());
                      System.out.println("Ignored Words (not propagated) = " + bannedWordsSet);
                      System.out.println("Gossip Rate = " + gossipRate);
                      System.out.println("Msg Drop Chance = " + msgDropChance);
                      continue;
                    default:
                      System.err.println("Invalid command: " + cmd + " " + arg + "\n"
                          + "use \"get help\" to see all available commands");
                      continue;
                  }
                default:
                  System.err.println(
                      "Invalid Command:" + cmd + " " + arg + "\n" + "use \"get help\" to see all available commands");
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
   * Injects words to the network at a poisson distribution rate.
   * 
   * @param lambda
   * @throws IOException if it failed to get a UDP socket.
   */
  private void poissonWordsGenerator() throws IOException {
    Random wordsRNG = new Random();
    List<String> words = new ArrayList<>();
    try {
      // Store all words in a list
      Scanner scanner = new Scanner(WORDS_FILE);
      scanner.next(); // First occurrence is not a word!
      while (scanner.hasNext()) {
        words.add(scanner.next());
      }
      scanner.close();
      var scheduler = new PoissonJobScheduler(LAMBDA, new Random(), new SendWords(words, hostAddr, PORT, wordsRNG));
      scheduler.schedulerThread().start();
      System.out.println("Started Poisson Word Generator Thread.");
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

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
    if (args_.size() < 0) {
      System.err.println(Gossiping.class.getName() + " HOST_ADDRESS STATE_BOOLEAN [PEERS_ADDRESSES]");
    } else {
      boolean poissonState;
      if (args_.get(1).equals("true"))
        poissonState = true;
      else if (args_.get(1).equals("false"))
        poissonState = false;
      else {
        System.err.println("STATE_BOOLEAN must be true or false");
        return;
      }
      if (args_.size() == 2) {
        new Gossiping(host, poissonState);
      } else {
        new Gossiping(host, args_.subList(2, args_.size()), poissonState);
      }

    }
  }
}
