package smarthome.home;

import akka.actor.Props;
import scala.sys.Prop;
import smarthome.messages.ActivateMessage;
import smarthome.messages.ResponseMessage;

public class Television extends Appliance{

    @Override
    public void activate(ActivateMessage message) {
        this.server = getContext().getParent();
        server.tell(new ResponseMessage(false, "Television connected and activated"), self());
        this.system = getContext().getSystem();
        this.name = "Television";
        this.isOn = false;
        this.functionWithTimer = false;
        this.functionWithTemperature = false;
    }

    public static Props props(){
        return Props.create(Television.class);
    }
}
