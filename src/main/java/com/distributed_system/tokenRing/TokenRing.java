package com.distributed_system.tokenRing;

import com.distributed_system.msgHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class TokenRing {
  private InetAddress hostAddr; // IP where this server is hosted on.
  private InetAddress nextHost; // IP to send token to.
  private volatile Boolean isLocked = true; // Locked or Unlocked.
  msgHandler handler; // Handles sending of messages via the Transport Layer.
  public static final int PORT = 6666; // Port server is being hosted on.
  private static final int RETRY_WAIT_TIME = 500; // If a msg transmission fails we wait this amount of time in milliseconds until the next transmission.
  private static final int HEAVY_WORK_MAX_TIME = 2000; // Used to simulate a heavy task

  public TokenRing(InetAddress hostAddr, InetAddress nextHost) {
    this.nextHost = nextHost;
    this.hostAddr = hostAddr;
    handler = new numMsgHandlerTCP();
    changeLockStateThread();
    this.start();
  }

  /**
   * Infinite Loop. Receive the token, execute some work, send the token, repeat.
   */
  private void start() {
    System.out.println("Starting " + hostAddr);
    while (true) {
      System.out.println("Awaiting for token");
      long token = getToken();
      System.out.println("Received Token:" + token + ", Executing Heavy Work");
      while (isLocked); // Cannot exit this loop till the server is unlocked.
      token += 1;
      System.out.println("Finished Heavy Work, Sending Token " + token);
      sendToken(token);
    }
  }

  /**
   * Blocking call to retrieve token via TCP.
   * Operates the following way: try to receive the token, if it fails retry after WAIT_TIME, only terminates on success.
   * @return The token
   */
  private long getToken() {
    Object token;
    while ((token = handler.receiveMsg(hostAddr, PORT)) == null) {
      try {
        Thread.sleep(RETRY_WAIT_TIME);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return (long) token;
  }

  /**
   * Blocking call to send the token via TCP.
   * Operates the following way: try to send the token, if it fails retry after WAIT_TIME, only terminates on success.
   * @param token to send via TCP.
   */
  private void sendToken(long token) {
    while (!(handler.sendMsg(token, nextHost, PORT))) {
      try {
        Thread.sleep(RETRY_WAIT_TIME);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Freezes thread up to {@code maxMilliseconds}.
   * @param maxMilliseconds
   */
  private static void simulateHeavyWork(int maxSeconds) {
    int sleepTime = ThreadLocalRandom.current().nextInt(0, maxSeconds + 1);
    System.out.println("Heavy work will take " + maxSeconds + " seconds");
    // Heavy work simulated with a sleepTime sleep
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Thread responsible for managing the lock/unlock TokenRing state.
   * Gathers input from stdin, if the input is equal to "lock"/"unlock" it locks/unlocks the token,
   */
  private void changeLockStateThread() {
    Thread t = new Thread(
      new Runnable() {

        public void run() {
          var in = new Scanner(System.in);
          while (in.hasNext()) {
            String inputStr = in.next();
            if (inputStr.equals("lock")) {
              System.out.println("State was locked");
              isLocked = true;
            } else if (inputStr.equals("unlock")) {
              System.out.println("State was unlocked");
              isLocked = false;
            }
          }
          in.close();
        }
      }
    );
    t.start();
  }

  /**
   * Starts a TokenRing server on "{@value args[0]}:{@value #PORT}"
   * @param args Host Address, Next TokenRing Server Address
   * @throws UnknownHostException
   */
  public static void main(String[] args) throws UnknownHostException {
    var host = InetAddress.getByName(args[0]);
    var nextHost = InetAddress.getByName(args[1]);
    new TokenRing(host, nextHost);
  }
}
