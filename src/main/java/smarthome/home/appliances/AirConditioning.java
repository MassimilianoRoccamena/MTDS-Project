package smarthome.home.appliances;
import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

import java.time.Duration;

public class AirConditioning extends Appliance {

    @Override
    public void activate(ActivateMessage message) {
        this.room = getContext().getParent();
        room.tell(new ResponseMessage(false, "Air conditioning connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Air Conditioning";
        this.isOn = false;
        this.functionWithTimer = false;
        this.functionWithTemperature = true;
        this.notify_consumption = system
                .scheduler()
                .scheduleWithFixedDelay(Duration.ZERO, Duration.ofMillis(1000), this::updateConsumption, system.dispatcher());
    }

    @Override
    public void notifyChangeTemperature(ActorRef sender) {
        sender.tell(new ResponseMessage(false, "-1"),self());
    }

    public static Props props() {
        return Props.create(AirConditioning.class);
    }
}
