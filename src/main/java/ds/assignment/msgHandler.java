package ds.assignment;

import java.net.InetAddress;

/**
 * Interface used to abstract the Transport Layer. 
 */
public interface msgHandler {

  /**
   * Sends a Message.
   * @param obj Object To Send.
   * @param sndAddr Target Address.
   * @param sndPort Target Port.
   * @return True if no errors occurred.
   */
  public Boolean sendMsg(Object obj, InetAddress sndAddr, int sndPort);

  /**
   * Receives a Message
   * @param rcvAddr Address where the server will be exposed.
   * @param rcvPort Port where the server will be exposed.
   */
  public Object receiveMsg(InetAddress rcvAddr, int rcvPort);
}
