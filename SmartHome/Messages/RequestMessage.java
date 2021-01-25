package com.SmartHome.Messages;

import com.SmartHome.Messages.MessageType;

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

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }


}
