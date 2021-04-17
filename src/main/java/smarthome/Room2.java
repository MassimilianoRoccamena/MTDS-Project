package smarthome;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.messages.ActivateMessage;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Room2 {
    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/room2.conf"));
        ActorSystem sys = ActorSystem.create("Room2", conf);
        ActorRef client = sys.actorOf(Client.props(), "clientActor");
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        exec.submit(() -> client.tell(new ActivateMessage(), ActorRef.noSender()));
    }
}
