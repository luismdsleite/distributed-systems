package com.distributed_system.tokenRing;

import com.distributed_system.msgHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadLocalRandom;

public class TokenRing {
  private static final int RETRY_WAIT_TIME = 500;
  private static final int HEAVY_WORK_MAX_TIME = 2000;
  public static final int PORT = 6666;
  private InetAddress hostAddr;
  private InetAddress nextHost;
  msgHandler handler;

  public TokenRing(InetAddress hostAddr, InetAddress nextHost) {
    this.nextHost = nextHost;
    this.hostAddr = hostAddr;
    handler = new numMsgHandlerTCP();
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
      TokenRing.simulateHeavyWork(HEAVY_WORK_MAX_TIME);
      token += 1;
      System.out.println("Finished Heavy Work, Sending Token " + token);
      sendToken(token);
    }
  }

  /**
   * Blocking call to retrieve token via TCP.
   * Operates the following way: try to receive the token, if it fails retry after WAIT_TIME, can only end on success.
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
   * Operates the following way: try to send the token, if it fails retry after WAIT_TIME, can only end on success.
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
    // Heavy work simulated with a 2s sleep
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
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
