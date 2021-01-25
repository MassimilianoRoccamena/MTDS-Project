package smarthome.messages;

import java.io.Serializable;

public class ResponseMessage implements Serializable {
    boolean arg;
    private String message;

    public ResponseMessage(boolean arg, String message) {
        this.arg = arg;
        this.message = message;
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

}
