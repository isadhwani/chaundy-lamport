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
       //System.out.println("Starting talker to " + hostname + " on port " + port);

        while (true) {

            //System.out.println("Should this talker on port " + this.port + " send a token? " + this.sendToken);

            try {
                //System.out.println("Send marker? : " + this.sendMarker);
                Socket socket = new Socket(hostname, port);

                OutputStream outputStream = socket.getOutputStream();

                if (this.sendToken) {
                    state.hasToken = false;
                    this.sendToken = false;




                    //System.out.println("Sending token to " + hostname + " on port " + port);
                    String message = "{id: " + myHostname + ", sender: " + myHostname +
                    ", receiver: " + hostname + ", message:''token''}";
                    System.out.println(message);


                    //String message = "token";
                    byte[] messageBytes = message.getBytes();

                    //System.out.println("Sleeping for " + tokenDelay + " seconds");
                    outputStream.write(messageBytes);

                    outputStream.flush();
                    //System.out.println("Sent message to server: " + message);
                } else if (this.sendMarker) {
                    System.out.println("Sending marker!!");

                    String hasTokenYesNo = state.hasToken ? "YES" : "NO";


                    String message = "{id: " + myHostname + ", sender: " + myHostname +
                            ", receiver: " + hostname + ", message:''marker" + this.state.currentMarkerID + "''," +
                            " state:" + state.getState() + ", has_token: " + hasTokenYesNo +"}";
                    System.out.println(message);


                    byte[] messageBytes = message.getBytes();
                    sleep((long) (this.markerDelay * 1000));

                    outputStream.write(messageBytes);
                    outputStream.flush();
                    this.sendMarker = false;
                }
                if(state.incrementMarkerAfterSend) {
                    state.currentMarkerID++;
                    state.incrementMarkerAfterSend = false;
                }

                // Close the socket
                socket.close();
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


