package smarthome.home.rooms;

import akka.actor.Props;
import smarthome.home.appliances.AirConditioning;
import smarthome.home.appliances.Television;
import smarthome.home.appliances.Thermostat;
import smarthome.messages.ActivateMessage;

public class LivingRoom extends Room{
    public void activateRoom(ActivateMessage message){
        this.roomName = "Living Room";
        createActor("Television", Television.props());
        createActor("Thermostat", Thermostat.props());
        createActor("AirConditioning", AirConditioning.props());
    }
    public static Props props() {
        return Props.create(LivingRoom.class);
    }
}
