package local;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusJSON {
    private String id;
    private String state;
    private String predecessor;
    private String successor;


    public StatusJSON(String id, String state, String predecessor, String successor) {
        this.id = id;
        this.state = state;
        this.predecessor = predecessor;
        this.successor = successor;
    }

    // Jackson annotation to map 'id' field in JSON to 'id' field in Java
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Jackson annotation to map 'state' field in JSON to 'state' field in Java
    @JsonProperty("state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    // Jackson annotation to map 'predecessor' field in JSON to 'predecessor' field in Java
    @JsonProperty("predecessor")
    public String getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(String predecessor) {
        this.predecessor = predecessor;
    }

    // Jackson annotation to map 'successor' field in JSON to 'successor' field in Java
    @JsonProperty("successor")
    public String getSuccessor() {
        return successor;
    }

    public void setSuccessor(String successor) {
        this.successor = successor;
    }

    @Override
    public String toString() {
        return "MyData{" +
                "id='" + id + '\'' +
                ", state='" + state + '\'' +
                ", predecessor='" + predecessor + '\'' +
                ", successor='" + successor + '\'' +
                '}';
    }
}
