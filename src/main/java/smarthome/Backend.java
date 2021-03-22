package smarthome;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import smarthome.home.AirConditioning;
import smarthome.home.Television;
import smarthome.home.Thermostat;
import smarthome.home.WashingMachine;
import smarthome.messages.ActivateMessage;
import smarthome.messages.CreateActorMessage;
import smarthome.messages.ResponseMessage;

import java.io.File;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Backend extends AbstractActor {
    private final scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
    private final SupervisorStrategy strategy =
            new CustomSupervisorStrategy(10,
                    timeout,
                    true,
                    DeciderBuilder
                            .match(Exception.class, e -> (SupervisorStrategy.Directive) SupervisorStrategy.restart())
                            .build()
            );

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::startBackend)
                .match(ResponseMessage.class, this::handleMessages)
                .build();
    }

    private void startBackend(ActivateMessage message){
        printLog("Backend started and functional");
        ActorRef panel = getContext().actorOf(ControlPanel.props(), "controlPanel");
        panel.tell(new CreateActorMessage(AirConditioning.props(), "AirConditioning"), ActorRef.noSender());
        panel.tell(new CreateActorMessage(Thermostat.props(), "Thermostat"), ActorRef.noSender());
        panel.tell(new CreateActorMessage(WashingMachine.props(), "WashingMachine"), ActorRef.noSender());
        panel.tell(new CreateActorMessage(Television.props(), "Television"), ActorRef.noSender());
    }
    private void handleMessages(ResponseMessage message){
        if(message.isArg()){
            printError(message.getMessage());
        }else {
            printLog(message.getMessage());
        }
    }

    private void printLog(String message){
        System.out.println("[LOG] " + message);
    }
    private void printError(String message){
        System.out.println("[ERROR] " + message);
    }

    static Props props(){
        return Props.create(Backend.class);
    }

    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/server.conf"));
        ActorSystem sys = ActorSystem.create("Server", conf);
        ActorRef supervisor = sys.actorOf(Backend.props(), "backend");
        supervisor.tell(new ActivateMessage(), ActorRef.noSender());
    }
}
