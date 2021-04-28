package smarthome.home.rooms;

import akka.actor.Props;
import smarthome.home.appliances.AirConditioning;
import smarthome.home.appliances.DishWasher;
import smarthome.home.appliances.Thermostat;
import smarthome.messages.ActivateMessage;

public class Kitchen extends Room{
    public void activateRoom(ActivateMessage message){
        this.roomName = "Kitchen";
        createActor("DishWasher", DishWasher.props());
        createActor("Thermostat", Thermostat.props());
        createActor("AirConditioning", AirConditioning.props());
    }
    public static Props props() {
        return Props.create(Kitchen.class);
    }
}
