package ds.assignment.tokenring;

import java.net.InetAddress;
import java.net.UnknownHostException;

import ds.assignment.msgHandler;

public class SendToken {

  public static void main(String[] args) throws UnknownHostException {
    var targetAddr = InetAddress.getByName(args[0]);

    msgHandler handler = new numMsgHandlerTCP();
    handler.sendMsg( (long) 0, targetAddr, TokenRing.PORT);

  }
}
