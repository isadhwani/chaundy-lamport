package local;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class OpenTalker extends Thread {
    String hostname;
    int port;

    boolean hasToken = false;
    String myHostname;

    boolean sendMarker = false;

    public StateValue state;

    public OpenTalker(String hostname, int port, String myHostname, StateValue s) {
        this.hostname = hostname;
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
    }



    @Override
    public void run() {
//        System.out.println("Sending a message to " + hostname + " on port " + port);
//
//        boolean connEstablished = false;
//
//        while (!connEstablished) {
//            try {
//                if (establishConnection()) {
//                    connEstablished = true;
//                    continue;
//                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            try {
//                sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("Trying to establish tcp talker...");
//        }
        while (true) {

            try {
                Socket socket = new Socket(hostname, port);

                OutputStream outputStream = socket.getOutputStream();

                if (this.hasToken) {
                    String message = "{id: " + myHostname + ", sender: " + myHostname +
                    ", receiver: " + hostname + ", message:''token''}";
                    System.out.println(message);


                    //String message = "token";
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    //System.out.println("Sent message to server: " + message);
                    this.hasToken = false;
                    state.hasToken = false;
                }

                if (this.sendMarker) {
                    String hasTokenYesNo = state.hasToken ? "YES" : "NO";


                    String message = "{id: " + myHostname + ", sender: " + myHostname +
                            ", receiver: " + hostname + ", message:''marker" + this.state.currentMarkerID + "''," +
                            " state:" + state.getState() + ", has_token: " + hasTokenYesNo +"}";
                    System.out.println(message);


                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    this.sendMarker = false;
                }

                // Close the socket
                socket.close();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


