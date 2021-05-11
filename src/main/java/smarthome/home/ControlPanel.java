package smarthome.home;

import akka.actor.*;
import smarthome.messages.*;
import java.util.HashMap;
import java.util.Map;

public class ControlPanel extends AbstractActor{
    private ActorSelection backend;
    private ActorRef room;
    private ActorRef ui;
    private Map<String, ActorRef> rooms = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConsumptionMessage.class, this::notifyConsumption)
                .match(ResponseMessage.class, this::showMessage)
                .match(ActivateMessage.class, this::activatePanel)
                .match(RequestMessage.class, this::requestHandle)
                .match(Terminated.class, this::disconnectHandle)
                .build();
    }


    private void activatePanel(ActivateMessage message){
        this.backend = context().actorSelection("akka://Backend@192.168.56.101:2550/user/backend");
        this.backend.tell(new ResponseMessage(false, "Control Panel Activated"), self());
        this.ui = context().actorOf(UserInterface.props(), "userInterface");
        ui.tell(new ActivateMessage(), self());
    }

    private void requestHandle(RequestMessage message){
        boolean forward = false;
        switch (message.getType()){
            case NEWROOM:
                this.rooms.put(message.getArg(), sender());
                context().watch(sender());
                this.backend.tell(new ConsumptionMessage(message.getArg(), 0), self());
                break;
            case ROOMSLIST:
                sender().tell(new RoomsMessage(this.rooms), self());
                break;
            case MACHINELIST:
                this.room = rooms.get(message.getArg());
                forward = true;
                break;
            case SWITCHMACHINE:
            case CHANGETEMPERATURE:
                forward = true;
                break;
        }
        if(forward){
            this.room.tell(new RequestMessage(message.getType(), message.getArg()), self());
        }
    }

    private void disconnectHandle(Terminated message){
        for (Map.Entry<String, ActorRef> entry : rooms.entrySet()) {
            if (entry.getValue().equals(message.getActor())) {
                rooms.remove(entry.getKey());
            }
        }

    }

    private void showMessage(ResponseMessage message){
       this.ui.tell(message, self());
    }

    public void notifyConsumption(ConsumptionMessage message){
        this.backend.tell(message, self());
    }

    public static Props props() {
        return Props.create(ControlPanel.class);
    }

}
