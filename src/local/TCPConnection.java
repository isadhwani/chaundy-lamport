package local;

/**
 * Represents a Listener and talker for one process to connect to another
 */
public class TCPConnection {
    OpenListener listener;
    OpenTalker talker;

    TCPConnection(OpenTalker talk, OpenListener listen) {
        this.talker = talk;
        this.listener = listen;
    }

}
