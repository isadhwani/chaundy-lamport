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
    public static void main(String[] args) {
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


        OpenListener previousListen = new OpenListener(listenPorts[1], hostname, state);
        previousListen.start();

        OpenListener nextListen = new OpenListener(listenPorts[0], hostname, state);
        nextListen.start();

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        OpenTalker previousTalk = new OpenTalker(neighbors[1], talkPorts[1], hostname, state);
        //OpenTalker previousTalk = new OpenTalker("localhost", talkPorts[1]); // hard code to localhost for rn
        previousTalk.start();

        OpenTalker nextTalk = new OpenTalker(neighbors[0], talkPorts[0], hostname, state);

        //OpenTalker nextTalk = new OpenTalker("localhost", talkPorts[0]); // hard code to localhost for rn
        nextTalk.start();


        float t = 1;
        float m = -1;
        float s = -1;


        // Has this peer started its snapshot yet?
        boolean startedSnap = false;

        boolean receivedMarker = false;


        printState(hostname, state.getState(), neighbors[1], neighbors[0]);


        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-x")) {
                previousListen.hasToken = true;
                state.hasToken = true;

            } else if  (arg.equals("-t")) {
                t = Float.parseFloat(args[i + 1]);
            } else if  (arg.equals("-s")) {
                s = Float.parseFloat(args[i + 1]);
            } else if  (arg.equals("-m")) {
                m = Float.parseFloat(args[i + 1]);
            }
        }

        boolean first = true;

        while(true) {
            if (previousListen.hasToken()) {

                if(!first) {
                    state.state++;
                } else {
                    first = false;
                }

                System.out.println("{id: " + hostname + ", state: " + state.state + "}");

                nextTalk.hasToken = true;
                previousListen.hasToken = false;
                try {
                    sleep((long)(t * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // If this peer should start a snapshot...
            if(state.getState() == s && !startedSnap) {

                System.out.println("STARTING SNAP");

                System.out.println("{id: " + hostname + ", snapshot:''started''}");
                nextTalk.sendMarker = true;
                previousTalk.sendMarker = true;
                startedSnap = true;
            }

            if(state.receivedMarker) {
                // logic to determine what chanel to send marker on:

                if(nextListen.sendToOther) {
                    previousTalk.sendMarker = true;
                }
                if(previousListen.sendToOther) {
                    nextTalk.sendMarker = true;
                }
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






