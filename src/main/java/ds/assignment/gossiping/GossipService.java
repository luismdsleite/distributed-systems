package ds.assignment.gossiping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import ds.assignment.HostChecker;
import ds.assignment.poissonjob.PoissonJobScheduler;
import generated.msgHandlerGrpc;
import generated.MsgHandler.Empty;
import generated.MsgHandler.Msg;
import generated.msgHandlerGrpc.msgHandlerBlockingStub;
import generated.msgHandlerGrpc.msgHandlerImplBase;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

public class GossipService extends msgHandlerImplBase {

    private String hostAddr; // IP where this server is hosted on.

    /** Connection Receiver Thread parameters. */
    private volatile List<String> hostsSet = new Vector<>();
    private volatile Set<String> wordsSet = new HashSet<>();
    private volatile Set<String> bannedWordsSet = new HashSet<>();
    public static final int PORT = 6667; // Port server is being hosted on.
    public static final int MAX_MSG_LENGTH = 512; // Maximum msg size (in bytes)
    private static final Random random = new Random();
    private final ConcurrentHashMap<String, ManagedChannel> channels = new ConcurrentHashMap<>();

    /** Input Thread parameters. */
    private double msgDropChance = 0.20; // Chance of a msg already present in the wordsSet to be dropped.
    private int gossipRate = 3; // How many hosts to gossip at a time.

    /** Poisson Words Generator Thread parameters. */

    // Lambda of the poissonWordsGenerator.
    // Currently on average generates 1 event per 30 seconds.
    private static final double LAMBDA = 1 / (double) 30;

    // File containing the words used by the thread.
    private static final File WORDS_FILE = new File("resources/4000-most-common-english-words-csv.csv");

    public GossipService(String hostAddr, boolean startPoisson) {
        this.hostAddr = hostAddr;
        // handler = new numMsgHandlerTCP();

        start(startPoisson);
    }

    public GossipService(String hostAddr, List<String> registeredUsers, boolean startPoisson) {
        this(hostAddr, startPoisson);
        this.hostsSet.addAll(registeredUsers);
        startChannels();
    }

    public GossipService(String hostAddr, List<String> registeredUsers) {
        this(hostAddr, registeredUsers, true);
    }

    private void start(boolean startPoisson) {
        channels.put(hostAddr, openChannelWithoutFail(hostAddr));// Used by the "send" command and the SendWords job
        startThreads(startPoisson);
    }

    /**
     * Starts all the threads.
     * 
     * @throws IOException
     */
    private void startThreads(boolean startPoisson) {
        System.out.println("Starting " + hostAddr + ":" + PORT);
        var inputThread = stdinThread();
        inputThread.start();
        if (startPoisson)
            poissonWordsGenerator();

    }

    /**
     * Create a channel for every host in {@code hostsSet}. Only executed if the
     * constructor is given a predefined hostsSet.
     */
    private void startChannels() {
        var it = hostsSet.iterator();
        while (it.hasNext()) {
            String host = it.next();
            channels.put(host, openChannelWithoutFail(host));
        }
    }

    /**
     * Tries to open a channel to {@code target}. It keeps trying forever until it
     * succeeds.
     * 
     * @param target IPv4 to open the channel to.
     * @return
     */
    private ManagedChannel openChannelWithoutFail(String target) {
        ManagedChannel channel;
        while (true) {
            try {
                channel = openChannel(target);
                break;
            } catch (Exception e) {
                System.out.println("Failed to open channel to host " + target + " trying again!");
            }
        }
        return channel;
    }

    /**
     * Tries to open a channel to the {@code target}
     * 
     * @param target IPv4 to open the channel to.
     * @return
     */
    private ManagedChannel openChannel(String target) {
        return ManagedChannelBuilder.forAddress(target, PORT)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build();
    }

    /**
     * <p>
     * Receives a word and gossips it to {@code gossipRate} hosts.
     * </p>
     * <p>
     * If the word received was already in the {@code wordsSet} then we have a
     * {@code msgDropChance} of not propagating it and adding it to the
     * {@code bannedWordsSet}. Words in the {@code bannedWordsSet} are never
     * propagated.
     * </p>
     */
    @Override
    public void sendMsg(Msg request, StreamObserver<Empty> responseObserver) {
        String word = request.getMsg(); // Receive token.
        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted(); // Terminate connection with that server.

        System.out.println("Received word: " + word);
        var b = !wordsSet.add(word);
        if (bannedWordsSet.contains(word)) {
            return;
        }
        if (b && random.nextDouble() < msgDropChance) {
            System.out.println("Dropping already received word: " + word);
            bannedWordsSet.add(word);
            return;
        }

        List<String> hostsToGossip = new ArrayList<>(gossipRate);

        // this is to prevent an exception if the nº of registered hosts < gossipRate
        int gossipsNum = Math.min(gossipRate, hostsSet.size());
        if (gossipsNum <= 0)
            return;
        // Get a random host and add it to the hostsToGossip list. If we already chose
        // that host get a new one.
        for (int i = 0; i < gossipsNum; i++) {
            var possibleHost = hostsSet.get(random.nextInt(hostsSet.size()));
            if (hostsToGossip.contains(possibleHost))
                i--;
            else
                hostsToGossip.add(possibleHost);
        }

        for (String target : hostsToGossip) {
            // Connect to the target and send him the word.
            var channel = channels.get(target);
            msgHandlerBlockingStub handlerStub = msgHandlerGrpc.newBlockingStub(channel);
            handlerStub.sendMsg(request);
        }

    }

    /**
     * <p>
     * Thread responsible for receiving user input. Gathers input from stdin.
     * </p>
     *
     * Commands:
     * <ul>
     * <li><b>register {ipv4}/unregister {ipv4}</b>: Adds/removes the host.
     * <li><b>send {ipv4} {msg}</b>: Sends a msg to a certain host (Used for testing
     * purposes).
     * <li><b>get all</b>: Equivalent to executing all get commands minus <b>get channels</b>.
     * <li><b>get words</b>: List of the received words.
     * <li><b>get hosts</b>: List of all registered hosts.
     * <li><b>get banned</b>: List of words that are not propagated.
     * <li><b>get gossipRate</b>: nº of randomly chosen hosts a message is
     * propagated to.
     * <li><b>get dropChance</b>: chance of a word retransmission being ignored.
     * <li><b>get channels</b>: print all {@link io.grpc.ManagedChannel} stored.
     * <li><b>set gossipRate</b>: changes gossipRate param.
     * <li><b>set dropChance</b>: changes dropChance param.
     * </ul>
     */
    private Thread stdinThread() {
        return new Thread(
                new Runnable() {

                    public void run() {
                        var in = new Scanner(System.in);
                        while (in.hasNext()) {
                            String cmd = in.next();
                            String arg = in.next();
                            // Possible commands
                            switch (cmd) {
                                case "register":
                                    // Checking if the host is in a valid ipv4 format.
                                    if (!HostChecker.validate(arg)) {
                                        System.out.println("Host: " + arg + " is not a valid host name");
                                        continue;
                                    }
                                    if (hostsSet.contains(arg)) {
                                        System.out.println(
                                                "Host " + arg + " was already registered");
                                    } else {
                                        try {
                                            var channel = openChannel(arg);
                                            channels.put(arg, channel);
                                            hostsSet.add(arg);
                                            System.out.println("Added Host " + arg);
                                        } catch (Exception e) {
                                            System.err.println(
                                                    "Failed To register host " + arg + " could not open channel");
                                        }

                                    }
                                    continue;
                                case "unregister":
                                    // Checking if the host is in a valid ipv4 format.
                                    if (!HostChecker.validate(arg)) {
                                        System.out.println("Host: " + arg + " is not a valid host name");
                                        continue;
                                    }
                                    if (!hostsSet.remove(arg)) {
                                        System.out.println("Host " + arg + " is not registered");
                                    } else {
                                        try {
                                            System.out.println("Removed Host " + arg);
                                            var channelToTerminate = channels.get(arg);
                                            channelToTerminate.shutdownNow();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            System.err.println("Failed to unregister " + arg);
                                        }
                                        channels.remove(arg);
                                    }
                                    continue;
                                case "send":
                                    // Checking if the host is in a valid ipv4 format.
                                    if (!HostChecker.validate(arg)) {
                                        System.out.println("Host: " + arg + " is not a valid host name");
                                        continue;
                                    }
                                    String msgStr = in.next();
                                    // Connect to the target and send him the word.
                                    var channel = channels.get(arg);
                                    try {
                                        msgHandlerBlockingStub handlerStub = msgHandlerGrpc.newBlockingStub(channel);
                                        Msg newMsg = Msg.newBuilder().setMsg(msgStr).build();
                                        handlerStub.sendMsg(newMsg);

                                    } catch (Exception e) {
                                        System.err.println("Failed to send msg");
                                        e.printStackTrace();
                                    }
                                    continue;
                                case "set":
                                    switch (arg) {
                                        case "gossipRate":
                                            gossipRate = in.nextInt();
                                            System.out.println("Gossip Rate = " + gossipRate);
                                            continue;
                                        case "dropChance":
                                            msgDropChance = in.nextDouble();
                                            System.out.println("msgDropChance = " + msgDropChance);
                                            continue;
                                        default:
                                            System.err.println("Invalid command: " + cmd + " " + arg + "\n"
                                                    + "use \"get help\" to see all available commands");
                                            continue;
                                    }
                                case "get":
                                    switch (arg) {
                                        case "help":
                                            System.out.println("Available Commands" + "\n" +
                                                    "\t" + "register {ipv4}/unregister {ipv4}" + "\n" +
                                                    "\t" + "send {ipv4} {msg}" + "\n" +
                                                    "\t" + "get all" + "\n" +
                                                    "\t" + "get words" + "\n" +
                                                    "\t" + "get hosts" + "\n" +
                                                    "\t" + "get banned" + "\n" +
                                                    "\t" + "get gossipRate" + "\n" +
                                                    "\t" + "get dropChance" + "\n" +
                                                    "\t" + "set gossipRate" + "\n" +
                                                    "\t" + "set dropChance");
                                            continue;
                                        case "hosts":
                                            System.out.println(hostsSet);
                                            continue;
                                        case "words":
                                            System.out.println(wordsSet);
                                            continue;
                                        case "banned":
                                            System.out.println(bannedWordsSet);
                                            continue;
                                        case "gossipRate":
                                            System.out.println(gossipRate);
                                            continue;
                                        case "dropChance":
                                            System.out.println(msgDropChance);
                                        case "channels":
                                            System.out.println(channels);
                                            continue;
                                        case "all":
                                            System.out.println("No. of Hosts = " + hostsSet.size());
                                            System.out.println("Hosts = " + hostsSet);
                                            System.out.println("No. of Words = " + wordsSet.size());
                                            System.out.println("Words = " + wordsSet);
                                            System.out.println("No. of Banned Words = " + bannedWordsSet.size());
                                            System.out.println("Ignored Words (not propagated) = " + bannedWordsSet);
                                            System.out.println("Gossip Rate = " + gossipRate);
                                            System.out.println("Msg Drop Chance = " + msgDropChance);
                                            continue;
                                        default:
                                            System.err.println("Invalid command: " + cmd + " " + arg + "\n"
                                                    + "use \"get help\" to see all available commands");
                                            continue;
                                    }
                                default:
                                    System.err.println(
                                            "Invalid Command:" + cmd + " " + arg + "\n"
                                                    + "use \"get help\" to see all available commands");
                                    break;
                            }
                        }
                        in.close();
                    }
                });
    }

    /**
     * Injects words to the network at a poisson distribution rate.
     * 
     * @param lambda
     * @throws IOException if it failed to get a UDP socket.
     */
    private void poissonWordsGenerator() {

        Random wordsRNG = new Random();
        List<String> words = new ArrayList<>();
        try {
            // Store all words in a list
            Scanner scanner = new Scanner(WORDS_FILE);
            scanner.next(); // First occurrence is not a word!
            while (scanner.hasNext()) {
                words.add(scanner.next());
            }
            scanner.close();

            var scheduler = new PoissonJobScheduler(LAMBDA, new Random(),
                    new SendWords(words, channels.get(hostAddr), wordsRNG));
            scheduler.schedulerThread().start();
            System.out.println("Started Poisson Word Generator Thread.");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GossipService service = null;
        var args_ = Arrays.asList(args);
        if (args_.size() == 0) {
            System.err.println("Class " + GossipService.class.getName() + " requires at least 1 argument.");
            return;
        }

        if (args_.size() < 0) {
            System.err.println(GossipService.class.getName() + " HOST_ADDRESS STATE_BOOLEAN [PEERS_ADDRESSES]");
        } else {
            boolean poissonState;
            if (args_.get(1).equals("true"))
                poissonState = true;
            else if (args_.get(1).equals("false"))
                poissonState = false;
            else {
                System.err.println("STATE_BOOLEAN must be true or false");
                return;
            }
            if (args_.size() == 2) {
                service = new GossipService(args[0], poissonState);
            } else {
                service = new GossipService(args[0], args_.subList(2, args_.size()), poissonState);
            }

        }
        var server = NettyServerBuilder
                .forAddress(new InetSocketAddress(args[0], PORT))
                .addService(service)
                .build()
                .start();
        server.awaitTermination();
    }

}
