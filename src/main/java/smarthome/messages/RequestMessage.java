package smarthome.messages;

import java.io.Serializable;

public class RequestMessage implements Serializable {
    private MessageType type;
    private String arg;

    public RequestMessage(MessageType type, String arg) {
        this.type = type;
        this.arg = arg;
    }

    public MessageType getType() {
        return type;
    }


    public String getArg() {
        return arg;
    }



}
