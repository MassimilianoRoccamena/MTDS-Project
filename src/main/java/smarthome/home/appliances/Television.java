package smarthome.home.appliances;

import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

public class Television extends Appliance {

    @Override
    public void activate(ActivateMessage message) {
        this.room = getContext().getParent();
        room.tell(new ResponseMessage(false, "Television connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Television";
        this.isOn = false;
        this.functionWithTimer = false;
        this.functionWithTemperature = false;
    }

    public static Props props(){
        return Props.create(Television.class);
    }
}
