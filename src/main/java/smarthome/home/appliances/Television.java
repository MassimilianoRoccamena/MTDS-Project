package smarthome.home.appliances;

import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

import java.time.Duration;

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
        this.notify_consumption = system
                .scheduler()
                .scheduleWithFixedDelay(Duration.ZERO, Duration.ofMillis(1000), this::updateConsumption, system.dispatcher());
    }

    public static Props props(){
        return Props.create(Television.class);
    }
}
