package smarthome.home.rooms;

import akka.actor.Props;
import smarthome.home.appliances.AirConditioning;
import smarthome.home.appliances.Television;
import smarthome.home.appliances.Thermostat;
import smarthome.messages.ActivateMessage;
import smarthome.messages.MessageType;
import smarthome.messages.RequestMessage;

public class LivingRoom extends Room{
    public void activateRoom(ActivateMessage message){
        this.roomName = "Living Room";
        this.panel.tell(new RequestMessage(MessageType.NEWROOM, this.roomName),self());
        createActor("Television", Television.props());
        createActor("Thermostat", Thermostat.props());
        createActor("AirConditioning", AirConditioning.props());
    }
    public static Props props() {
        return Props.create(LivingRoom.class);
    }
}
