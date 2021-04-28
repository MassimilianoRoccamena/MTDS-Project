package smarthome;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.backend.Backend;
import smarthome.messages.ActivateMessage;

import java.io.File;

public class BackendMain {
    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/backend.conf"));
        ActorSystem sys = ActorSystem.create("Backend", conf);
        ActorRef supervisor = sys.actorOf(Backend.props(), "backend");
        supervisor.tell(new ActivateMessage(), ActorRef.noSender());
    }
}
