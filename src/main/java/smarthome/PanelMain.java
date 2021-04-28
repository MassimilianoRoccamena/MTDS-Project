package smarthome;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.home.ControlPanel;
import smarthome.messages.ActivateMessage;

import java.io.File;

public class PanelMain {
    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/panel.conf"));
        ActorSystem sys = ActorSystem.create("Panel", conf);
        ActorRef panel = sys.actorOf(ControlPanel.props(), "controlPanel");
        panel.tell(new ActivateMessage(), ActorRef.noSender());
    }
}
