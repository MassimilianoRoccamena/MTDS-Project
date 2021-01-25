package SmartHome.Home;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import SmartHome.Messages.RequestMessage;
import SmartHome.Messages.ResponseMessage;

import java.util.Random;

public abstract class Appliance extends AbstractActor {
    public boolean isOn;
    public boolean timableFunctioning;
    public ActorRef server;

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
            this.notifyStop(false);
        }else {
            this.isOn = true;
            this.notifyStart(timableFunctioning);
            if(this.timableFunctioning){
                try {
                    Thread.sleep(10000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
                this.isOn = false;
                this.notifyStop(true);
            }
        }
    }
    public void notifyStop(boolean timer){

    }
    public void notifyStart(boolean timer){}

    @Override
    public void postStop() throws Exception, Exception {
        throw new InterruptedException();
    }


}
