package smarthome.home.appliances;

import akka.actor.*;
import smarthome.messages.ActivateMessage;
import smarthome.messages.RequestMessage;
import smarthome.messages.ResponseMessage;

import java.time.Duration;
import java.util.Optional;
import java.util.Random;

public abstract class Appliance extends AbstractActor {
    public boolean isOn;
    public String name;
    public boolean functionWithTimer;
    public int durationMilli;
    public boolean functionWithTemperature;
    public ActorRef room;
    public ActorSystem system;
    Cancellable functioningProcess;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::activate)
                .match(RequestMessage.class, this::handleRequest)
                .build();
    }

    public float getConsumption(){
        float cons = 0.0f;
        if(this.isOn) {
            Random random = new Random();
            cons = random.nextInt(49)+1 + random.nextFloat();
        }
        return cons;
    }
    public void handleRequest(RequestMessage message){
        switch (message.getType()){
            case MACHINELIST:
                sender().tell(this.isOn ? "ON" : "OFF", self());
                break;
            case SWITCHMACHINE:
                switchAppliance();
                break;
            case GETCONSUMPTION:
                sender().tell(this.getConsumption(),self());
        }
    }
    public void switchAppliance(){
        if (this.isOn){
            this.isOn = false;
            if(this.functionWithTimer || this.functionWithTemperature){
                this.functioningProcess.cancel();
            }
            this.notifyStop(functionWithTimer, sender());
        }else {
            this.isOn = true;
            this.notifyStart(functionWithTimer);
            if(this.functionWithTimer){
               functioningProcess = system
                       .scheduler()
                       .scheduleOnce(Duration.ofMillis(durationMilli), () -> {
                           isOn = false;
                           notifyStop(functionWithTimer, room);
                       },system.dispatcher());

            }
            if(this.functionWithTemperature){
                functioningProcess = system
                        .scheduler()
                        .scheduleWithFixedDelay(Duration.ZERO, Duration.ofMillis(1000), () -> notifyChangeTemperature(room),system.dispatcher());
            }
        }
    }

    //Functions to override
    public void notifyStop(boolean timer, ActorRef sender){
        sender.tell(new ResponseMessage(timer, this.name + " has stopped its execution!"), self());
    }
    public void notifyStart(boolean timer){
        sender().tell(new ResponseMessage(timer, this.name + " has started working!"),self());

    }
    public void notifyChangeTemperature(ActorRef sender){}
    public void activate(ActivateMessage message){}

    @Override
    public void preRestart(Throwable reason, Optional<Object> message){
        if(functioningProcess != null){
            functioningProcess.cancel();
        }
        System.out.println(this.name + " is being restarted");
    }

    @Override
    public void postRestart(Throwable reason){
        activate(new ActivateMessage());
        System.out.println(this.name + " has been restarted");
    }


}
