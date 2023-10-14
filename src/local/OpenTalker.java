package local;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class OpenTalker extends Thread {
    String hostname;
    int port;

    boolean sendToken = false;
    String myHostname;

    boolean sendMarker = false;

    public StateValue state;

    double markerDelay = 0;
    double tokenDelay = 0;


    public OpenTalker(String hostname, int port, String myHostname, StateValue s, double tokenDelay ,double markerDelay) {
        this.hostname = hostname;
        this.port = port;
        this.myHostname = myHostname;
        this.state = s;
        this.markerDelay = markerDelay;
        this.tokenDelay = tokenDelay;
    }



    @Override
    public void run() {
       System.out.println("Starting talker to " + hostname + " on port " + port);
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
                //System.out.println("Send marker? : " + this.sendMarker);
                sleep(1000);
                Socket socket = new Socket(hostname, port);

                OutputStream outputStream = socket.getOutputStream();

                if (this.sendToken) {
                    //sleep((long) (this.tokenDelay * 1000));

                    System.out.println("Sending token to " + hostname + " on port " + port);
                    String message = "{id: " + myHostname + ", sender: " + myHostname +
                    ", receiver: " + hostname + ", message:''token''}";
                    System.out.println(message);


                    //String message = "token";
                    byte[] messageBytes = message.getBytes();
                    outputStream.write(messageBytes);
                    outputStream.flush();
                    //System.out.println("Sent message to server: " + message);
                    this.sendToken = false;
                    state.hasToken = false;
                }

                if (this.sendMarker) {
                    System.out.println("Sending marker!!");

                    sleep((long) (this.markerDelay * 1000));
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
                if(state.incrementMarkerAfterSend) {
                    state.currentMarkerID++;
                    state.incrementMarkerAfterSend = false;
                }

                // Close the socket
                //socket.close();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}


