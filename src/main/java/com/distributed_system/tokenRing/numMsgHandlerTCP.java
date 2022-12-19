package com.distributed_system.tokenRing;

import com.distributed_system.msgHandler;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Sends/Receives a {@code long} via TCP.
 */
public class numMsgHandlerTCP implements msgHandler {

  @Override
  public Boolean sendMsg(
    Object longNum,
    InetAddress senderAddr,
    int senderPort
  ) {
    try (Socket socket = new Socket(senderAddr, senderPort)) {
      // Connection Established.
      // Convert Long number to byte[] and send it via TCP.
      var msg = longToBytes((Long) longNum);
      socket.getOutputStream().write(msg);
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Object receiveMsg(InetAddress rcvAddr, int rcvPort) {
    try (ServerSocket serverSocket = new ServerSocket(rcvPort, 0, rcvAddr)) {
      serverSocket.setSoTimeout(0);
      Socket socket = serverSocket.accept();
      var msg = socket.getInputStream().readAllBytes();
      return bytesToLong(msg);
    } catch (IOException e) {
      // Error with ServerSocket.
      e.printStackTrace();
      return null;
    }
  }

  // ByteBuffer Wrapper to convert from Long to byte[] and vice-versa.
  private static ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

  private static byte[] longToBytes(long x) {
    buffer.clear();
    buffer.putLong(0, x);
    return buffer.array();
  }

  private static long bytesToLong(byte[] bytes) {
    buffer.put(bytes, 0, bytes.length);
    buffer.flip(); //need flip
    return buffer.getLong();
  }
}
