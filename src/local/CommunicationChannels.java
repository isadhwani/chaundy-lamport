package local;

public class CommunicationChannels {
    int previous;
    int next;


    CommunicationChannels(int prev, int next) {
        this.previous = prev;
        this.next = next;
    }
    @Override
    public String toString() {
        return "Previous: " + this.previous + "\nNext: " + this.next;
    }

}
