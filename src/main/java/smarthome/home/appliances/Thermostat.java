package smarthome.home.appliances;

import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.RequestMessage;
import smarthome.messages.ResponseMessage;

import java.time.Duration;

public class Thermostat extends Appliance {

    private float temperature;

    @Override
    public void handleRequest(RequestMessage message) {
        switch (message.getType()){
            case MACHINELIST:
                sender().tell(this.isOn ? "ON" : "OFF", self());
                break;
            case SWITCHMACHINE:
                switchAppliance();
                break;
            case GETCONSUMPTION:
                sender().tell(this.consumption,self());
                break;
            case GETTEMPERATURE:
                sender().tell(this.temperature, self());
                break;
            case CHANGETEMPERATURE:
                this.temperature += Float.parseFloat(message.getArg());
                break;
        }
    }

    @Override
    public void activate(ActivateMessage message) {
        sender().tell(new ResponseMessage(false, "Thermostat connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Thermostat";
        this.functionWithTimer = false;
        this.functionWithTemperature = true;
        this.durationMilli = 10000;
        this.isOn = false;
        this.room = sender();
        this.temperature = 20;
        this.notify_consumption = system
                .scheduler()
                .scheduleWithFixedDelay(Duration.ZERO, Duration.ofMillis(1000), this::updateConsumption, system.dispatcher());
    }

    @Override
    public void notifyChangeTemperature(ActorRef sender) {
        sender.tell(new ResponseMessage(false, "1"),self());
    }

    public static Props props() {
        return Props.create(Thermostat.class);
    }
}
