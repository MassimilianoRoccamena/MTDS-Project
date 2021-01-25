package com.SmartHome.Messages;

import java.io.Serializable;

public class ResponseMessage implements Serializable {
    boolean arg;
    private String message;
    private float data;

    public ResponseMessage(boolean arg, String message, float data) {
        this.arg = arg;
        this.message = message;
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isArg() {
        return arg;
    }

    public void setArg(boolean arg) {
        this.arg = arg;
    }

    public float getData() {
        return data;
    }

    public void setData(float data) {
        this.data = data;
    }
}
