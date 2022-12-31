package ds.assignment.tokenring;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Scanner;

import generated.msgHandlerGrpc;
import generated.MsgHandler.Empty;
import generated.MsgHandler.Msg;
import generated.msgHandlerGrpc.msgHandlerBlockingStub;
import generated.msgHandlerGrpc.msgHandlerImplBase;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

/**
 * <p>
 * Server implementation of a distributed TokenRing setup. Communication is done
 * via gRPC.
 * </p>
 */
public class TokenService extends msgHandlerImplBase {
  private String hostAddr; // IP to host the service on.
  private String nextHost; // IP to send token to.
  private volatile Boolean isLocked; // Locked or Unlocked.
  private boolean wasLocked = true; // Used only to know when to print "token unlocked" message.
  private static final int PORT = 6666; // Port server is being hosted on.
  private Thread lockStateThread;

  public TokenService(String hostAddr, String nextHost, boolean startLocked) {
    this.hostAddr = hostAddr;
    this.nextHost = nextHost;
    this.isLocked = startLocked;
    lockStateThread = changeLockStateThread();
    lockStateThread.start();
  }

  public TokenService(String hostAddr, String nextHost) {
    this(hostAddr, nextHost, true);
  }

  /**
   * <p>
   * Executed when a token is received. After receiving a token check if
   * the token is locked and if it is then send it to the {@code nextHost}.
   * </p>
   * <p>
   * If the transmission of the token to the {@code nextHost} fails then we keep
   * trying till we succeed.
   * </p>
   */
  @Override
  public void sendMsg(Msg request, StreamObserver<Empty> responseObserver) {
    String token = request.getMsg(); // Receive token.
    responseObserver.onNext(Empty.newBuilder().build());
    responseObserver.onCompleted(); // Terminate connection with that server.
    System.out.println("Received Token " + token + "!");
    // Check if we want to lock the token.
    if (isLocked) {
      System.out.println("Locked Token");
      wasLocked = true;
    }
    // End when the token is unlocked
    while (isLocked) {
      continue;
    }
    // Trigger this msg only when the token was locked and is now unlocked.
    if (wasLocked) {
      System.out.println("Token was unlocked");
      wasLocked = false;
    }

    // Connect to the nextHost and send him the token. On Failure case retry
    var channel = ManagedChannelBuilder.forAddress(nextHost, PORT)
        .usePlaintext()
        .build();
    while (true) {
      try {
        msgHandlerBlockingStub handlerStub = msgHandlerGrpc.newBlockingStub(channel);
        long newToken = Long.valueOf(token) + 1;
        Msg newTokenMsg = Msg.newBuilder().setMsg("" + newToken).build();
        handlerStub.sendMsg(newTokenMsg);
        System.out.println("Sent Token " + newTokenMsg);
        break;
      } catch (Exception e) {
        System.err.println(e);
        System.out.println("Failed to Send Token " + token + "Trying Again");
      }

    }

  }

  /**
   * Thread responsible for managing the lock/unlock TokenRing state.
   * Gathers input from stdin, accepts the following commands:
   * <ul>
   * <li>"lock"/"unlock": locks/unlocks the token.
   * <li>"startToken": Used to create the token, it create a token and sends it to
   * itself.
   * </ul>
   */
  private Thread changeLockStateThread() {
    return new Thread(
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
              } else if (inputStr.equals("startToken")) {
                var channel = ManagedChannelBuilder.forAddress(hostAddr, PORT)
                    .usePlaintext()
                    .build();
                msgHandlerBlockingStub handlerStub = msgHandlerGrpc.newBlockingStub(channel);
                long newToken = 0;
                Msg newTokenMsg = Msg.newBuilder().setMsg("" + newToken).build();
                handlerStub.sendMsg(newTokenMsg);
                System.out.println("Sent Token " + newTokenMsg);
              }
            }
            in.close();
          }
        });
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    var server = NettyServerBuilder
        .forAddress(new InetSocketAddress(args[0], PORT))
        .addService(new TokenService(args[0], args[1], true))
        .maxConcurrentCallsPerConnection(1)
        .build()
        .start();
    System.out.println("Started TokenService");

    server.awaitTermination();

  }

}
