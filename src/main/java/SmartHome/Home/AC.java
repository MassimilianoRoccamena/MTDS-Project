package SmartHome.Home;
import akka.actor.ActorRef;
import akka.actor.Props;
import SmartHome.Messages.ActivateMessage;
import SmartHome.Messages.RequestMessage;
import SmartHome.Messages.ResponseMessage;

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
        this.timableFunctioning = true;
        this.server = sender();
    }

    @Override
    public void notifyStop(boolean timer) {
        server.tell(new ResponseMessage(timer, "The air conditioning has stopped its execution.", 0), self());
    }
    @Override
    public void notifyStart(boolean timer) {
        sender().tell(new ResponseMessage(timer, "The air conditioning has started working!", this.getConsumption()),self());
    }

    @Override
    public void postRestart(Throwable reason) throws Exception, Exception {
        System.out.println("The Air Conditioning system has been restarted");
    }

    public static Props props() {
        return Props.create(AC.class);
    }
}
