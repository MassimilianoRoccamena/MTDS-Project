package smarthome.home;
import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

public class AirConditioning extends Appliance {

    @Override
    public void activate(ActivateMessage message) {
        this.server = getContext().getParent();
        server.tell(new ResponseMessage(false, "[LOG] Air conditioning connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Air Conditioning";
        this.isOn = false;
        this.functionWithTimer = false;
        this.functionWithTemperature = true;
    }

    @Override
    public void notifyChangeTemperature(ActorRef sender) {
        sender.tell(new ResponseMessage(false, "-1"),self());
    }

    public static Props props() {
        return Props.create(AirConditioning.class);
    }
}
