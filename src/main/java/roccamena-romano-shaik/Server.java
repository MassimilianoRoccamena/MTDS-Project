package com.SmartHome;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.SmartHome.Home.AC;
import com.SmartHome.Messages.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Server extends AbstractActor {
    private final Map<ActorRef, Float> timerAppliancesWorking = new HashMap<>();
    private final float temperature = 20;
    private final Map<String, ActorRef> appliances = new HashMap<>();
    private final scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
    private final static SupervisorStrategy strategy =
            new OneForOneStrategy(
                    10,
                    Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(InterruptedException.class, e -> (SupervisorStrategy.Directive) SupervisorStrategy.restart())
                            .build());
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RequestMessage.class, this::onRequest)
                .match(CreateActorMessage.class, this::createActor)
                .match(ResponseMessage.class, this::applianceManage)
                .build();
    }
    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    //Function that handles the requests coming from the client
    private void onRequest(RequestMessage message) throws InterruptedException, TimeoutException{
        ResponseMessage response;
        switch (message.getType()){
            case MACHINELIST:
                response = getAppliancesList();
                break;
            case SWITCHMACHINE:
                response = switchAppliance(message.getArg());
                break;
            default:
                response = new ResponseMessage(false, "Wrong request", 0);
                break;
        }
        sender().tell(response, self());
    }
    //Function to handle the creation of a new appliance Actor
    private void createActor(CreateActorMessage message){
       ActorRef ref = getContext().actorOf(message.getProps(), message.getName());
       ref.tell(new ActivateMessage(), self());
       this.appliances.put(message.getName(), ref);
    }
    //Function called when a time Appliance stops working
    private void applianceManage(ResponseMessage message){
        if(message.isArg() && this.timerAppliancesWorking.containsKey(sender())){
            this.timerAppliancesWorking.remove(sender());
        }
        System.out.println(message.getMessage());
    }

    //Functions that communicate with the appliances
    private ResponseMessage getAppliancesList() throws InterruptedException,TimeoutException{
        String list = "---------APPLIANCES LIST---------\n";
        float totalConsumption = 0;
        list+= "NAME\t\t\tSTATE\tCONSUMPTION\n";
        for(Map.Entry<String, ActorRef> ref: this.appliances.entrySet()){
            list += ref.getKey() + "\t";
            if(this.timerAppliancesWorking.containsKey(ref.getValue())){
                list += "ON\t\t" + this.timerAppliancesWorking.get(ref.getValue()) + "W\n";
                totalConsumption += timerAppliancesWorking.get(ref.getValue());
            }else {
                scala.concurrent.Future<Object> waitingForState = ask(ref.getValue(), new RequestMessage(MessageType.MACHINELIST, null), 5000);
                list += waitingForState.result(timeout, null) + "\t\t";
                scala.concurrent.Future<Object> waitingForConsumption = ask(ref.getValue(), new RequestMessage(MessageType.GETCONSUMPTION, null), 5000);
                float consumption = (Float) waitingForConsumption.result(timeout, null);
                totalConsumption += consumption;
                list += consumption  + "W\n";
            }
            list+= "---------------------------------\n";
            list+= "TOTAL CONSUMPTION: " + totalConsumption + "W\n";
            list+= "TEMPERATURE: " + this.temperature + "°C\n";
        }
        return new ResponseMessage(false, list, 0);
    }
    private ResponseMessage switchAppliance(String name) throws InterruptedException, TimeoutException{
        String response;
        boolean error = false;
        ActorRef appliance = this.appliances.get(name);
        if(appliance == null){
            response = "The appliance does not exist";
            error = true;
        }else {
            scala.concurrent.Future<Object> waitingForAppliance = ask(appliance, new RequestMessage(MessageType.SWITCHMACHINE, null), 5000);
            ResponseMessage applianceMessage =(ResponseMessage) waitingForAppliance.result(timeout, null);
            response = applianceMessage.getMessage() + "\n";
            if(applianceMessage.isArg() && !this.timerAppliancesWorking.containsKey(sender())){
                this.timerAppliancesWorking.put(appliance,applianceMessage.getData());
            }
            response += getAppliancesList().getMessage();
        }
        System.out.println(response);
        return new ResponseMessage(error, response, 0);
    }

    static Props props() {
        return Props.create(Server.class);
    }


    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("src/main/java/com/SmartHome/config/server.conf"));
        ActorSystem sys = ActorSystem.create("Server", conf);
        ActorRef supervisor = sys.actorOf(Server.props(), "controlPanel");
        System.out.println("The control panel is functional");
        supervisor.tell(new CreateActorMessage(AC.props(), "airConditioning"), ActorRef.noSender());
    }
}
