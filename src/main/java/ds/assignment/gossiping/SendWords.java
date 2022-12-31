package ds.assignment.gossiping;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import ds.assignment.poissonjob.PoissonJob;
import generated.msgHandlerGrpc;
import generated.MsgHandler.Msg;
import generated.msgHandlerGrpc.msgHandlerBlockingStub;
import io.grpc.ManagedChannelBuilder;

public class SendWords implements PoissonJob {
    private final List<String> strings;
    private String host;
    private int port;
    private Random rng;

    /**
     * PoissonJob to send words to a server via gRPC.
     * 
     * @param words words sent via gRPC to the server.
     * @param host  IPv4 of the server.
     * @param port  UDP port.
     * @throws IOException
     */
    SendWords(List<String> words, String host, int port, Random rng){
        this.strings = words;
        this.host = host;
        this.port = port;
        this.rng = rng;
    }

    @Override
    public void execute() {
        if (strings.size() > 0) {

            var word = strings.get(rng.nextInt(strings.size()));
            // Connect to the target and send him the word.
            var channel = ManagedChannelBuilder.forAddress(host, port)
                    .usePlaintext()
                    .build();
            msgHandlerBlockingStub handlerStub = msgHandlerGrpc.newBlockingStub(channel);
            Msg newMsg = Msg.newBuilder().setMsg(word).build();
            handlerStub.sendMsg(newMsg);
            strings.remove(word);
            System.out.println("Poisson: Injected to the network the word: " + word);
        }

    }

}
