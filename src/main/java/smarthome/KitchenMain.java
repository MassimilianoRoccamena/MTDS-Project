package smarthome;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.home.rooms.Kitchen;
import smarthome.messages.ActivateMessage;

import java.io.File;

public class KitchenMain {
    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/kitchen.conf"));
        ActorSystem sys = ActorSystem.create("Kitchen", conf);
        ActorRef supervisor = sys.actorOf(Kitchen.props(), "kitchen");
        supervisor.tell(new ActivateMessage(), ActorRef.noSender());
    }
}
