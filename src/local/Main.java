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

        Map<Integer, CommunicationChannels> outgoingPortTable = makePortTable();
        // int portNumber = 4950;
        //System.out.print(outgoingPortTable);
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


        int[] listenPorts = {(outgoingPortTable.get(
                ((peerNumber - 1 + 1 + 5) % 5) + 1).previous), // grabs the next hosts previous outgoing port
                (outgoingPortTable.get(
                        ((peerNumber - 1 - 1 + 5) % 5) + 1).next)}; // grabs the previous hosts next outgoing port

        int[] talkPorts = {outgoingPortTable.get(peerNumber).next,
                outgoingPortTable.get(peerNumber).previous
        };

        String[] neighbors = {
                "peer" + (((peerNumber - 1 + 1 + 5) % 5) + 1),
                "peer" + (((peerNumber - 1 - 1 + 5) % 5) + 1)};

        /*
         * neighbors[0] --> next
         * neighbors[1] --> previous
         */

        /*
         * listenPorts[0] --> next
         * listenPorts[1] --> previous
         */

        /*
         * talkPorts[0] --> next
         * talkPorts[1] --> previous
         */

        StateValue state = new StateValue(0);


        String prevListenName = neighbors[1] + "-" +  hostname;
        OpenListener previousListen = new OpenListener(listenPorts[1], hostname, state, prevListenName);
        previousListen.start();

        String nextListenName = neighbors[0] + "-" +  hostname;
        OpenListener nextListen = new OpenListener(listenPorts[0], hostname, state, nextListenName);
        nextListen.start();

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        float t = 0;
        float m = 0;
        float s = -1;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-x")) {
                previousListen.hasToken = true;
                state.hasToken = true;

            } else if (arg.equals("-t")) {
                t = Float.parseFloat(args[i + 1]);
            } else if (arg.equals("-s")) {
                s = Float.parseFloat(args[i + 1]);
            } else if (arg.equals("-m")) {
                m = Float.parseFloat(args[i + 1]);
            }
        }

        OpenTalker previousTalk = new OpenTalker(neighbors[1], talkPorts[1], hostname, state, t, m);
        //OpenTalker previousTalk = new OpenTalker("localhost", talkPorts[1]); // hard code to localhost for rn
        previousTalk.start();

        OpenTalker nextTalk = new OpenTalker(neighbors[0], talkPorts[0], hostname, state, t, m);

        //OpenTalker nextTalk = new OpenTalker("localhost", talkPorts[0]); // hard code to localhost for rn
        nextTalk.start();


        // Has this peer started its snapshot yet?
        boolean startedSnap = false;

        printState(hostname, state.getState(), neighbors[1], neighbors[0]);



        boolean first = true;

        while (true) {

            // If this peer should start a snapshot...
            if (state.getState() == s && !startedSnap) {

                System.out.println("STARTING SNAP");

                System.out.println("{id: " + hostname + ", snapshot:''started''}");
                nextTalk.sendMarker = true;
                previousTalk.sendMarker = true;

                nextListen.isRecording = true;
                previousListen.isRecording = true;

                startedSnap = true;
            }

            // Snapshot cases
            if (nextListen.sendOnOther) {
                previousTalk.sendMarker = true;
                nextListen.sendOnOther = false;
            }
            if (previousListen.sendOnOther) {
                nextTalk.sendMarker = true;
                previousListen.sendOnOther = false;
            }
            if (nextListen.recordOther) {
                previousListen.isRecording = true;
                nextListen.recordOther = false;
            }
            if (previousListen.recordOther) {
                nextListen.isRecording = true;
                previousListen.recordOther = false;
            }

            if(previousListen.isClosed && nextListen.isClosed) {
                System.out.println("{id:" + hostname + ", snapshot:''complete''}");
                previousListen.isClosed = nextListen.isClosed = false;

                sleep(1000);
                // after finishing snap, only accept markers of a higher ID
                nextTalk.sendMarker = false;
                previousTalk.sendMarker = false;
                state.incrementMarkerAfterSend = true;
            }

            if (previousListen.hasToken()) {

                if (!first) {
                    state.state++;
                } else {
                    first = false;
                }

                System.out.println("{id: " + hostname + ", state: " + state.state + "}");

                nextTalk.hasToken = true;
                previousListen.hasToken = false;
            }

        }

    }

    private static void startSnapshot(OpenTalker nextTalk, OpenTalker previousTalk, OpenListener nextListen,
                                      OpenListener previousListen) {


    }


    private static void printState(String hostname, int state, String preID, String nextID) {
        System.out.println("{id: " + hostname + ", state: " + state + ", predecessor: " + preID +
                ", successor: " + nextID + "}");
    }

    private static Map<Integer, CommunicationChannels> makePortTable() {
        int port = 4950; // First port we will use is 4950

        Map<Integer, CommunicationChannels> portTable = new HashMap<>();

        for (int i = 1; i < 6; i++) {

            portTable.put(i, new CommunicationChannels(port, port + 1));
            port += 2;

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
}






