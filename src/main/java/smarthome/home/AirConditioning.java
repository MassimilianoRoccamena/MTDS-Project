package smarthome.home;
import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

public class AirConditioning extends Appliance {

    @Override
    public void activate(ActivateMessage message) {
        sender().tell(new ResponseMessage(false, "Air conditioning connected and activated"), self());
        this.system = getContext().getSystem();
        this.durationMilli = 10000;
        this.isOn = false;
        this.functionWithTimer = false;
        this.functionWithTemperature = true;
        this.server = sender();
    }

    @Override
    public void notifyStop(boolean timer, ActorRef sender) {
        sender.tell(new ResponseMessage(timer, "Air conditioning has stopped its execution!"), self());
    }
    @Override
    public void notifyStart(boolean timer) {
        sender().tell(new ResponseMessage(timer, "Air Conditioning has started working!"),self());
    }

    @Override
    public void notifyChangeTemperature(ActorRef sender) {
        sender.tell(new ResponseMessage(false, "-1"),self());
    }

    @Override
    public void postRestart(Throwable reason) throws Exception, Exception {
        System.out.println("The Air Conditioning system has been restarted");
    }

    public static Props props() {
        return Props.create(AirConditioning.class);
    }
}
