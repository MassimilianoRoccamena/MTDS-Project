package smarthome.messages;

import akka.actor.ActorRef;

import java.util.Map;

public class RoomsMessage {
    private Map<String, ActorRef> rooms;

    public RoomsMessage(Map<String, ActorRef> rooms) {
        this.rooms = rooms;
    }

    public Map<String, ActorRef> getRooms() {
        return rooms;
    }

    public void setRooms(Map<String, ActorRef> rooms) {
        this.rooms = rooms;
    }
}
