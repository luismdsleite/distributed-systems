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

public class SendWords implements PoissonJob {
    private final List<String> strings;
    private InetAddress host;
    private int port;
    private DatagramChannel channel;
    private Random rng;

    /**
     * PoissonJob to send words to a server via UDP.
     * 
     * @param words words sent via UDP to the server.
     * @param host  IPv4 of the server.
     * @param port  UDP port.
     * @throws IOException
     */
    SendWords(List<String> words, InetAddress host, int port, Random rng) throws IOException {
        this.strings = words;
        this.host = host;
        this.port = port;
        this.channel = DatagramChannel.open();
        this.rng = rng;
    }

    @Override
    public void execute() {
        try {
            if (strings.size() > 0) {
                var word = strings.get(rng.nextInt(strings.size()));
                strings.remove(word);
                ByteBuffer buffer = ByteBuffer.wrap(word.getBytes());
                channel.send(buffer, new InetSocketAddress(host, port));
                System.out.println("Poisson: Injected to the network the word: " + word);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
