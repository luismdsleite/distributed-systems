package com.distributed_system.tokenRing;

import com.distributed_system.msgHandler;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class TokenRing {
  public static int defaultPort = 6666;
  private InetAddress hostAddr;
  private InetAddress nextHost;
  msgHandler handler;

  public TokenRing(InetAddress hostAddr, InetAddress nextHost) {
    this.nextHost = nextHost;
    this.hostAddr = hostAddr;
    handler = new numMsgHandlerTCP();
    this.start();
  }

  // Infinite Loop. Receive the token, execute some work, send the token, repeat.
  public void start() {
    System.out.println("Starting " + hostAddr);
    while (true) {
      System.out.println("Awaiting for token");
      // TODO: If sending fails retry! (same with server setup)
      long token = (Long) handler.receiveMsg(hostAddr, defaultPort);
      System.out.println("Received Token:" + token + ", Executing Heavy Work");
      // try {
      //   Thread.sleep(2 * 1000);
      // } catch (InterruptedException e) {
      //   e.printStackTrace();
      // }
      token += 1;
      System.out.println("Finished Heavy Work, Sending Token " + token);
      handler.sendMsg(token, nextHost, defaultPort);
    }
  }

  public static void main(String[] args) throws UnknownHostException {
    var host1 = InetAddress.getByName(args[0]);
    var host2 = InetAddress.getByName(args[1]);
    // var host3 = InetAddress.getByName("127.0.0.3");
    var token1 = new TokenRing(host1, host2);
  }
}
