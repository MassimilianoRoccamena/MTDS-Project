package smarthome.home;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import smarthome.messages.RequestMessage;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;

import java.time.Duration;
import java.util.Random;

public abstract class Appliance extends AbstractActor {
    public boolean isOn;
    public boolean timableFunctioning;
    public int durationMilli;
    public ActorRef server;
    public ActorSystem system;
    Cancellable functioningProcess;

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
            if(this.timableFunctioning){
                this.functioningProcess.cancel();
            }
            this.notifyStop(timableFunctioning, sender());
        }else {
            this.isOn = true;
            this.notifyStart(timableFunctioning);
            if(this.timableFunctioning){
               functioningProcess = system
                       .scheduler()
                       .scheduleOnce(Duration.ofMillis(durationMilli), new Runnable() {
                           @Override
                           public void run() {
                               isOn = false;
                               notifyStop(timableFunctioning, server);
                           }
                       },system.dispatcher());

                ;
            }
        }
    }
    public void notifyStop(boolean timer, ActorRef sender){

    }
    public void notifyStart(boolean timer){}

    /*@Override
    public void postStop() throws Exception, Exception {
        throw new InterruptedException();
    }*/
}
