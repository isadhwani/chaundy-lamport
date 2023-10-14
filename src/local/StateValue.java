package local;

import java.util.ArrayList;

/**
 * Object for sharing state among listener, talker and main threads
 */
public class StateValue {
    public int state;

    // If a token is received on this listener, then the process should send a marker to the opposite talker
    // if prevListener receives a marker, then send msg on nextTalker

    // tells main to tell other listeners when to record messages, if they are not closed
    public boolean receivedMarker = false;

    public boolean recordOnOthers = false;

    public boolean hasToken = false;

    public boolean sendOnOthers = false;


    public boolean incrementMarkerAfterSend = false;

    // Represents the marker ID that this process has received up to. If a marker under this ID is received,
    // disregard it because we know we've finished that snapshot
    public int currentMarkerID = 0;

    public ArrayList<String> recordedQueue = new ArrayList<String>();

    public StateValue(int s) {
        this.state = s;
    }

    public void increment() {
        this.state = state + 1;
    }

    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return ("State: " + state + "\nReceived Marker: " + receivedMarker + "\nHas Token: " + hasToken);
    }
}
