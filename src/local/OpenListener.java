package local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenListener extends Thread {


    // Is this channel recording messages and states?
    public boolean isRecording = false;


    public StateValue state;

    public String myHostname;

    // If a token is received on this listener, then the process should send a marker to the opposite talker
    // if prevListener receives a marker, then send msg on nextTalker

    // tells other listener when to record messages
    public boolean recordOther = false;

    public boolean finishedSnap = false;

    public boolean isClosed = false;
    int port;

    int currentMarkerIndex = 0;

    ArrayList<String> recordedQueue = new ArrayList<String>();

    String channelName;

    OpenListener(int port, String myHostname, StateValue s, String channelName) {
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
        this.channelName = channelName;
    }

    ServerSocket connection;


    @Override
    public void run() {
        //System.out.println("Running listener thread!");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //System.out.println("Server is listening on port " + port);

            while (true) {
                System.out.println("starting listner...");
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
               //System.out.println("Client connected: " + clientSocket.getLocalAddress());

                // Create a new thread to handle the client communication
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String receivedMessages;
            while ((receivedMessages = reader.readLine()) != null) {
                System.out.println("Reading messages...");
                //System.out.println("Received from " + clientSocket.getInetAddress().getHostName() + ": " + receivedMessages);
                System.out.println("Received: " + receivedMessages);

                String[] messages = receivedMessages.split("\\}\\{");

                // if multiple messages are received at once, iterate through them
                for (String message : messages) {
                    // Add back the missing curly braces for each message
                    String jsonMessage = "{" + message + "}";


                    String msg = extractMessage(jsonMessage, "message:");
                    Map decoded = decodeJSON(jsonMessage);

                    //System.out.println(message);
                    //System.out.println("extracted msg: " + msg);


                    if (msg.equals("token")) {
                        state.hasToken = true;

                        if (this.isRecording && !this.isClosed) {
                            //System.out.println("RECORDING!");
                            this.recordedQueue.add(jsonMessage);
                        }


                        System.out.println("{id: " + myHostname + ", sender: " + decoded.get("sender")
                                + ", receiver: " + decoded.get("receiver") + ", message: ''" + msg + "''}");


                        state.state++;
                        System.out.println("{id: " + myHostname + ", state: " + state.state + "}");
                        state.hasToken = true;


                    } else if (msg.contains("marker")) {
                        int markerIndex = extractMarkerNumber(msg);

                        if (markerIndex >= state.currentMarkerID && !this.isClosed) {

                            // If this listener has not yet received a marker (check ID later)
                            if (!this.state.receivedMarker) {
                                System.out.println("Received first marker from " + decoded.get("sender") + ", current state: " + state.getState());
                                this.state.sendOnOthers = true;
                                this.recordOther = true;
                                this.isRecording = false;
                                this.state.receivedMarker = true;
                                this.isClosed = true;

                                System.out.println("{id: " + this.myHostname +
                                        ", snapshot:‘‘channel closed’’, channel: " + channelName +
                                        ", queue:" + recordedQueue + "}");

                            } else {
                                // if this process has already received a marker AND this channel is open, we know this
                                // is the only other channel a process can listen on and the snapshot must be over
                                //this.isRecording = false;

                                this.isRecording = false;
                                this.isClosed = true;
                                this.state.sendOnOthers = true;

                                //System.out.println("Message Queue with " + recordedQueue.size() + " messages: " + recordedQueue);


                                //System.out.println("SNAPSHOT COMPLETE");
                                System.out.println("{id: " + this.myHostname +
                                        ", snapshot:‘‘channel closed’’, channel: " + channelName +
                                        ", queue:" + recordedQueue + "}");


                                this.recordedQueue = new ArrayList<String>();
                            }
                        }
                    }
                }
            }
            // Client disconnected
            //System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            //clientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static String extractMessage(String message, String key) {
        // Assuming the message format is as specified
        // Find the index of the 'message:' substring
        int messageIndex = message.indexOf(key);

        if (messageIndex != -1) {
            // Find the index of the opening single quote
            int startQuoteIndex = message.indexOf("''", messageIndex);

            // Find the index of the closing single quote
            int endQuoteIndex = message.indexOf("''", startQuoteIndex + 2);

            if (startQuoteIndex != -1 && endQuoteIndex != -1) {
                // Extract the message between the single quotes
                return message.substring(startQuoteIndex + 2, endQuoteIndex);
            } else {
                // Handle the case where the opening or closing single quote is not found
                System.out.println("Opening or closing single quote not found.");
                return null; // or throw an exception, depending on your requirements
            }
        } else {
            // Handle the case where 'message:' is not found in the input string
            System.out.println("'message:' not found in the input string.");
            return null; // or throw an exception, depending on your requirements
        }
    }

    private static int extractMarkerNumber(String marker) {
        // Define a pattern to match "peer" followed by one or more digits
        Pattern pattern = Pattern.compile("marker(\\d+)");

        // Create a matcher with the input hostName
        Matcher matcher = pattern.matcher(marker);

        // Check if the pattern is found
        if (matcher.find()) {
            // Extract and parse the matched digits
            String numberString = matcher.group(1);
            return Integer.parseInt(numberString);
        } else {
            // Return a default value or throw an exception, depending on your requirements
            throw new IllegalArgumentException("Invalid host name format: " + marker);
        }
    }

    public static Map<String, Object> decodeJSON(String jsonString) {
        Map<String, Object> resultMap = new HashMap<>();

        // Remove curly braces from the JSON string
        jsonString = jsonString.substring(1, jsonString.length() - 1);

        // Split the string into key-value pairs
        String[] keyValuePairs = jsonString.split(",");

        for (String pair : keyValuePairs) {
            // Split each pair into key and value
            String[] entry = pair.split(":");

            // Trim whitespace from key and value
            String key = entry[0].trim();
            String value = entry[1].trim();

            // Remove quotes if the value is a string
//            if (value.startsWith("'") && value.endsWith("'")) {
//                value = value.substring(1, value.length() - 1);
//            }

            // Add the key-value pair to the result map
            resultMap.put(key, parseValue(value));
        }

        return resultMap;
    }

    private static Object parseValue(String value) {
        // Try to parse the value as an integer
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // If parsing as an integer fails, return the value as a string
            return value;
        }
    }


}


