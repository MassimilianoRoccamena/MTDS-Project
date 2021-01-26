package smarthome.home;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import smarthome.messages.ActivateMessage;
import smarthome.messages.RequestMessage;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;

import java.time.Duration;
import java.util.Random;

public abstract class Appliance extends AbstractActor {
    public boolean isOn;
    public boolean functionWithTimer;
    public int durationMilli;
    public boolean functionWithTemperature;
    public ActorRef server;
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
        Float cons = 0.0f;
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
                       .scheduleOnce(Duration.ofMillis(durationMilli), new Runnable() {
                           @Override
                           public void run() {
                               isOn = false;
                               notifyStop(functionWithTimer, server);
                           }
                       },system.dispatcher());

                ;
            }
            if(this.functionWithTemperature){
                functioningProcess = system
                        .scheduler()
                        .scheduleWithFixedDelay(Duration.ZERO, Duration.ofMillis(1000), new Runnable() {
                            @Override
                            public void run() {

                            }
                        },system.dispatcher());
            }
        }
    }

    //Functions to override
    public void notifyStop(boolean timer, ActorRef sender){

    }
    public void notifyStart(boolean timer){}
    public void notifyChangeTemperature(ActorRef sender){}
    public void activate(ActivateMessage message){};

    @Override
    public void postStop() throws Exception, Exception {
        throw new InterruptedException();
    }
}
