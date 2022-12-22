package ds.assignment.gossiping;

import ds.assignment.HostChecker;
import ds.assignment.msgHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Gossiping {
  private InetAddress hostAddr; // IP where this server is hosted on.
  private volatile Set<String> hostsSet = new HashSet<>();
  msgHandler handler; // Handles sending of messages via the Transport Layer.
  public static final int PORT = 6666; // Port server is being hosted on.
  private static final int RETRY_WAIT_TIME = 500; // If a msg transmission fails we wait this amount of time in milliseconds until the next transmission.
  private static final int HEAVY_WORK_MAX_TIME = 2000; // Used to simulate a heavy task

  public Gossiping(InetAddress hostAddr) {
    this.hostAddr = hostAddr;
    // handler = new numMsgHandlerTCP();
    var inputThread = stdinThread();
    inputThread.start();
    this.start();
  }

  /**
   * Infinite Loop. Receive the token, execute some work, send the token, repeat.
   */
  private void start() {
    System.out.println("Starting " + hostAddr);
  }

  /**
   * Thread responsible for managing the known hosts.
   * Gathers input from stdin, if the input is equal to "register host"/"unregister host" it adds/removes the host,
   */
  private Thread stdinThread() {
    return new Thread(
      new Runnable() {

        public void run() {
          var in = new Scanner(System.in);
          while (in.hasNext()) {
            String cmd = in.next();
            String host = in.next();

            // list registered hosts.
            if (cmd.equals("list") && host.equals("all")) {
              System.out.println(hostsSet);
              continue;
            }

            // Checking if the host is in a valid ipv4 format.
            if (!HostChecker.validate(host)) {
              System.out.println("Host: " + host + " is not a valid host name");
              continue;
            }

            if (cmd.equals("register")) {
              if (!hostsSet.add(host)) System.out.println(
                "Host " + host + " was already registered"
              ); else System.out.println("Added Host " + host);
            } else if (cmd.equals("unregister")) {
              if (!hostsSet.remove(host)) System.out.println(
                "Host " + host + " is not registered"
              ); else System.out.println("Removed Host " + host);
            }
          }
          in.close();
        }
      }
    );
  }

  /**
   * Starts a Gossiping server on "{@value args[0]}:{@value #PORT}"
   * @param args Host Address
   * @throws UnknownHostException
   */
  public static void main(String[] args) throws UnknownHostException {
    var host = InetAddress.getByName(args[0]);
    new Gossiping(host);
  }
}
