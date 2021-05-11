package smarthome.messages;

import java.io.Serializable;

public class RoomDisconnectedMessage implements Serializable {
    private String roomName;

    public RoomDisconnectedMessage(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
