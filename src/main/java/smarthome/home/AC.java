package smarthome.home;
import akka.actor.ActorRef;
import akka.actor.Props;
import smarthome.messages.ActivateMessage;
import smarthome.messages.RequestMessage;
import smarthome.messages.ResponseMessage;

public class AC extends Appliance {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::activate)
                .match(RequestMessage.class, this::handleRequest)
                .build();
    }

    private void activate(ActivateMessage message){
        System.out.println("Air Conditioning activated!");
        this.system = getContext().getSystem();
        this.durationMilli = 10000;
        this.isOn = false;
        this.timableFunctioning = true;
        this.server = sender();
    }

    @Override
    public void notifyStop(boolean timer, ActorRef sender) {
        sender.tell(new ResponseMessage(timer, "The air conditioning has stopped its execution."), self());
    }
    @Override
    public void notifyStart(boolean timer) {
        sender().tell(new ResponseMessage(timer, "The air conditioning has started working!"),self());
    }

    @Override
    public void postRestart(Throwable reason) throws Exception, Exception {
        System.out.println("The Air Conditioning system has been restarted");
    }

    public static Props props() {
        return Props.create(AC.class);
    }
}
