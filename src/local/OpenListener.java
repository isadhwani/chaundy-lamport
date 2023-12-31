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
   public  boolean hasToken = false;


   public ArrayList<String> recordedQueue = new ArrayList<String>();
    public ArrayList<Integer> stateQueue = new ArrayList<Integer>();

    // Is this channel recording messages and states?
    public boolean isRecording = false;



    public StateValue state;

    public String myHostname;

    // If a token is received on this listener, then the process should send a marker to the opposite talker
    // if prevListener receives a marker, then send msg on nextTalker
    public boolean sendToOther = false;

    int port;

    int currentMarkerIndex = 0;

    OpenListener(int port, String myHostname, StateValue s) {
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
    }

    ServerSocket connection;


    @Override
    public void run() {
        //System.out.println("Running listener thread!");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            //System.out.println("Server is listening on port " + port);

            while (true) {
                // Wait for a client to connect
                Socket clientSocket = serverSocket.accept();
                //System.out.println("Client connected: " + clientSocket.getInetAddress());

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

            String message;
            while ((message = reader.readLine()) != null) {
                //System.out.println("Received from " + clientSocket.getInetAddress().getHostAddress() + ": " + message);
                //System.out.println(message);
                String msg = extractMessage(message, "message:");

                Map decoded = decodeJSON(message);

                //System.out.println(message);
                //System.out.println("extracted msg: " + msg);




                if(msg.equals("token")) {
                    this.hasToken = true;
                    state.hasToken = true;

                    if(this.isRecording) {
                        recordedQueue.add(message);
                    }

                    System.out.println("{id: " + myHostname + ", sender: " + decoded.get("sender")
                            + ", receiver: " + decoded.get("receiver") + ", message: " + decoded.get("message") + "}");


                } else if (msg.contains("marker")  ) {
                    int markerIndex = extractMarkerNumber(msg);

                    // If the markerID has not already been recorded, then process this marker
                    if(markerIndex >= this.state.currentMarkerID) {

                        // if this peer has already not received a marker...
                        if (!state.receivedMarker && this.currentMarkerIndex <= markerIndex) {
                        /*
                        1. record the state S_pi
                        2. mark as “closed” the channel that the marker was received on (C_ik)
                        3. send out markers on all its outgoing channels (except Cik)
                        4. start recording messages received on all incoming channels (except C_ik)
                         */


                            System.out.println("Received first marker, current State: " + state.getState());
                            this.currentMarkerIndex++;

                            // set boolean flag of receiving marker to true, so we can tell in the other listener
                            state.receivedMarker = true;

                            // Tell other talker to send a marker
                            this.sendToOther = true;

                        } else if (state.receivedMarker && this.currentMarkerIndex <= markerIndex) {
                            // if this process has already received a marker AND this channel is open, we know this
                            // is the only other channel a process can listen on and the snapshot must be over
                            this.isRecording = false;


                            this.currentMarkerIndex++;


                            System.out.println("received second marker,snapshot over");

                            this.sendToOther = true;

                            // Let main have 1 second to send a message, before stopping the snapshot
                            sleep(1000);
                            state.receivedMarker = false;



                            System.out.print("Message Queue:");
                            for (String s : recordedQueue) {
                                System.out.print(s + " ");
                            }

                            System.out.println("SNAPSHOT COMPLETE");

                        } else {
                            System.out.println("received duplicate marker, ignore");
                        }

                    }
                }
            }


            // Client disconnected
            //System.out.println("Client disconnected: " + clientSocket.getInetAddress());
            clientSocket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean hasToken()  {
        if(this.hasToken) {
            //System.out.println("I have the token!");
        } else {
            //System.out.println("I don't have the token!");
        }
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this.hasToken;
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









