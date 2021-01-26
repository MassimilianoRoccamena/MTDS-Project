package smarthome.home;

import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.RequestMessage;
import smarthome.messages.ResponseMessage;

public class Termostat extends Appliance{

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
                sender().tell(this.getConsumption(),self());
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
        sender().tell(new ResponseMessage(false, "[LOG] Termostat connected and activated"), self());
        this.system = getContext().getSystem();
        this.functionWithTimer = false;
        this.functionWithTemperature = true;
        this.durationMilli = 10000;
        this.isOn = false;
        this.server = sender();
        this.temperature = 20;
    }

    @Override
    public void notifyStop(boolean timer, ActorRef sender) {
        sender.tell(new ResponseMessage(timer, "[LOG] The heating system has stopped its execution."), self());
    }

    @Override
    public void notifyStart(boolean timer) {
        sender().tell(new ResponseMessage(timer, "[LOG] The heating system has started working!"),self());
    }
    @Override
    public void notifyChangeTemperature(ActorRef sender) {
        sender.tell(new ResponseMessage(false, "1"),self());
    }

    public static Props props() {
        return Props.create(Termostat.class);
    }
}
