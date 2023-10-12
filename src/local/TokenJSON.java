package local;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenJSON {
    private String id;
    private String sender;
    private String receiver;
    private String message;

    // Default constructor (required for Jackson)
    // Parameterized constructor
    public TokenJSON(String id, String sender, String receiver, String message) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
    }

    // Jackson annotation to map 'id' field in JSON to 'id' field in Java
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Jackson annotation to map 'sender' field in JSON to 'sender' field in Java
    @JsonProperty("sender")
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    // Jackson annotation to map 'receiver' field in JSON to 'receiver' field in Java
    @JsonProperty("receiver")
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    // Jackson annotation to map 'message' field in JSON to 'message' field in Java
    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "id='" + id + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
