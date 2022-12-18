package com.distributed_system.tokenRing;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.distributed_system.msgHandler;

public class SendToken {

  public static void main(String[] args) throws UnknownHostException {
    var host1 = InetAddress.getByName("127.0.0.1");
    var host2 = InetAddress.getByName("127.0.0.2");
    var host3 = InetAddress.getByName("127.0.0.3");

    msgHandler handler = new numMsgHandlerTCP();
    handler.sendMsg( (long) 0, host1, TokenRing.defaultPort);

  }
}
