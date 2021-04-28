package smarthome;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.home.rooms.LivingRoom;
import smarthome.messages.ActivateMessage;

import java.io.File;

public class LivingRoomMain {
    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/living.conf"));
        ActorSystem sys = ActorSystem.create("LivingRoom", conf);
        ActorRef supervisor = sys.actorOf(LivingRoom.props(), "livingRoom");
        supervisor.tell(new ActivateMessage(), ActorRef.noSender());
    }
}
