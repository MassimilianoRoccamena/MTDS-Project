package smarthome.home.appliances;

import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

public class DishWasher extends Appliance {
    @Override
    public void activate(ActivateMessage message) {
        this.room = getContext().getParent();
        room.tell(new ResponseMessage(false, "Washing Machine connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Washing Machine";
        this.isOn = false;
        this.functionWithTimer = true;
        this.durationMilli = 10000;
        this.functionWithTemperature = false;
    }

    public static Props props(){
        return Props.create(DishWasher.class);
    }
}
