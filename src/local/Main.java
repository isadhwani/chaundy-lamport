package local;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


public class Main {


    /**
     * Invariant: always 5 peers
     *
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Command line arguments: " + Arrays.toString(args));


        String hostname;

        try {
            InetAddress localHost = InetAddress.getLocalHost();
            hostname = localHost.getHostName();
            System.out.println("Host Name: " + hostname);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        // For now make hostname peer1
        //hostname = "peer1";
        int peerNumber = extractPeerNumber(hostname);


        StateValue state = new StateValue(0);


        float t = 0;
        float m = 0;
        float s = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-t")) {
                t = Float.parseFloat(args[i + 1]);
            } else if (arg.equals("-s")) {
                s = Float.parseFloat(args[i + 1]);
            } else if (arg.equals("-m")) {
                m = Float.parseFloat(args[i + 1]);
            }
        }

        // Connections opened in order of ring, coming back to itself
        TCPConnection[] connections = new TCPConnection[4];


        Map<Integer, CommunicationChannels> outgoingPortTable = makePortTable(4950);

        int[][] outGoingPortTable = new int[5][5];

        int port = 4950;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                outGoingPortTable[r][c] = port;
                port++;
            }
        }

        int connectingPeerNumber = getNextValue(peerNumber);
        int connIndex = 0;
        while (connectingPeerNumber != peerNumber) {

            OpenListener listen = new OpenListener(outGoingPortTable[connectingPeerNumber - 1][peerNumber - 1], hostname,
                    state, "peer" + connectingPeerNumber + "-" + hostname);

            OpenTalker talk = new OpenTalker("peer" + connectingPeerNumber, outGoingPortTable[peerNumber - 1][connectingPeerNumber - 1],
                    hostname, state, t, m);

            TCPConnection c = new TCPConnection(talk, listen);
            connections[connIndex] = c;
            connIndex++;
            connectingPeerNumber = getNextValue(connectingPeerNumber);
        }

        String[] neighbors = {
                "peer" + (((peerNumber - 1 + 1 + 5) % 5) + 1),
                "peer" + (((peerNumber - 1 - 1 + 5) % 5) + 1)};

        /*
         * neighbors[0] --> next
         * neighbors[1] --> previous
         */

        // Make refrences for this peers neighbors, making token passing simplier
        OpenListener previousListen = connections[3].listener;
        OpenListener nextListen = connections[0].listener;
        OpenTalker previousTalk = connections[3].talker;
        OpenTalker nextTalk = connections[0].talker;

        for (TCPConnection c : connections)
            c.listener.start();


        sleep(1000);

        // start talking channels after listeners are set up
        for (TCPConnection c : connections)
            c.talker.start();


        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-x")) {
                state.hasToken = true;
            }
        }

        boolean startedSnap = false;
        printState(hostname, state.getState(), neighbors[1], neighbors[0]);
        boolean first = true;

        System.out.println("Listening for token on port " + previousListen.port);
        while (true) {

            // If this peer should start a snapshot...
            if (state.getState() == s && !startedSnap) {
                System.out.println("STARTING SNAP");

                System.out.println("{id: " + hostname + ", snapshot:''started''}");

                for (TCPConnection c : connections) {
                    System.out.println("Telling talk to send marker");
                    System.out.println("is talker to " + c.talker.hostname + "alive?" +  c.talker.isAlive());
                    c.talker.sendMarker = true;
                    c.listener.isRecording = true;
                }

                startedSnap = true;
            }

            // if any listener has received a marker, send on all channels that are not closed
            if (state.sendOnOthers) {
                for (TCPConnection c : connections) {
                    if (!c.listener.isClosed) {
                        c.talker.sendMarker = true;
                    }
                }

                state.sendOnOthers = false;
            }

            // Snapshot cases


            if (channelsAllClosed(connections) && state.receivedMarker) {
                System.out.println("{id:" + hostname + ", snapshot:''complete''}");
                previousListen.isClosed = nextListen.isClosed = false;

                sleep(1000);
                // after finishing snap, only accept markers of a higher ID
                for (TCPConnection c : connections) {
                    if (!c.listener.isClosed) {
                        c.talker.sendMarker = false;
                    }
                }
                state.receivedMarker = false;
                //state.incrementMarkerAfterSend = true;
            }

            sleep((long) (t * 1000));

            if (state.hasToken) {
                nextTalk.sendToken = true;
                state.hasToken = false;
            }

        }

    }

    private static boolean channelsAllClosed(TCPConnection[] connections) {
        for (TCPConnection c : connections) {
            if (!c.listener.isClosed) {
                c.talker.sendMarker = false;
            }
        }
        return true;
    }

    private static void startSnapshot(OpenTalker nextTalk, OpenTalker previousTalk, OpenListener nextListen,
                                      OpenListener previousListen) {


    }


    private static void printState(String hostname, int state, String preID, String nextID) {
        System.out.println("{id: " + hostname + ", state: " + state + ", predecessor: " + preID +
                ", successor: " + nextID + "}");
    }

    private static Map<Integer, CommunicationChannels> makePortTable(int startPort) {

        Map<Integer, CommunicationChannels> portTable = new HashMap<>();

        for (int i = 1; i < 6; i++) {

            portTable.put(i, new CommunicationChannels(startPort, startPort + 1));
            startPort += 2;

        }
        return portTable;
    }

    private static int extractPeerNumber(String hostName) {
        // Define a pattern to match "peer" followed by one or more digits
        Pattern pattern = Pattern.compile("peer(\\d+)");

        // Create a matcher with the input hostName
        Matcher matcher = pattern.matcher(hostName);

        // Check if the pattern is found
        if (matcher.find()) {
            // Extract and parse the matched digits
            String numberString = matcher.group(1);
            return Integer.parseInt(numberString);
        } else {
            // Return a default value or throw an exception, depending on your requirements
            throw new IllegalArgumentException("Invalid host name format: " + hostName);
        }
    }

    public static int getNextValue(int current) {
        // Assuming the input is between 1 and 5 (inclusive)
        if (current >= 1 && current <= 5) {
            // If the current value is 5, the next value is 1
            if (current == 5) {
                return 1;
            }
            // For any other value, the next value is the current value + 1
            else {
                return current + 1;
            }
        } else {
            // Handle invalid input (you can throw an exception or return a special value)
            System.out.println("Invalid input. Please provide a number between 1 and 5 (inclusive).");
            return -1; // You can choose a different special value if needed
        }
    }
}






