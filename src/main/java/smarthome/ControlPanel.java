package smarthome;

import smarthome.home.AirConditioning;
import akka.actor.*;
import akka.japi.pf.DeciderBuilder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.File;

import smarthome.home.Television;
import smarthome.home.Thermostat;
import smarthome.home.WashingMachine;
import smarthome.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ControlPanel extends AbstractActor{
    private final Map<String, ActorRef> appliances = new HashMap<>();
    private float desiredTemperature = 20;
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
            case CHANGETEMPERATURE:
                response = changeTemperature(message.getArg());
                break;
            default:
                response = new ResponseMessage(false, "Wrong request");
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
    private float getTemperature() throws InterruptedException, TimeoutException{
        ActorRef thermostat = this.appliances.get("Thermostat");
        scala.concurrent.Future<Object> waitingForAppliance = ask(thermostat, new RequestMessage(MessageType.GETTEMPERATURE, null), 5000);
        return (Float) waitingForAppliance.result(timeout, null);
    }
    private boolean checkConflict(ActorRef appliance) {
        ActorRef thermostat = this.appliances.get("Thermostat");
        ActorRef ac = this.appliances.get("AirConditioning");
        ActorRef check;
        boolean conflict;
        if(appliance.equals(thermostat)){
            check = ac;
        }else {
            check = thermostat;
        }
        try {
            scala.concurrent.Future<Object> waitingForState = ask(check, new RequestMessage(MessageType.MACHINELIST, null), 5000);
            String result =(String) waitingForState.result(timeout, null);
            conflict = !result.equals("OFF");
        }catch (Exception e){
            System.out.println("[ERROR] Could not find if there is a conflict between appliances");
            return true;
        }
        return conflict;
    }

    //Function called when a time Appliance stops working
    private void applianceManage(ResponseMessage message) {
        String responseMessage = message.getMessage();
        float actualTemperature;
        try {
             actualTemperature = getTemperature();
        }catch (Exception e){
            actualTemperature = 0;
            System.out.println("[ERROR] Something went wrong with getting the temperature");
        }
        if(responseMessage.equals("-1") || responseMessage.equals("1")){

            if(actualTemperature < 10 || (actualTemperature <= desiredTemperature && responseMessage.equals("-1"))){
                switchAppliance("AirConditioning");
            }else if(actualTemperature > 25 || (actualTemperature >= desiredTemperature && responseMessage.equals("1"))){
                switchAppliance("Thermostat");
            }else {
                ActorRef thermostat = this.appliances.get("Thermostat");
                thermostat.tell(new RequestMessage(MessageType.CHANGETEMPERATURE, message.getMessage()),self());
            }
            System.out.println("The temperature is now " + actualTemperature + "°C");
        }else {
            System.out.println(message.getMessage());
        }
    }

    //Functions that communicate with the appliances
    private ResponseMessage getAppliancesList() {
        StringBuilder list = new StringBuilder("\n---------APPLIANCES LIST---------\n");

        float totalConsumption = 0;
        list.append("NAME\t\t\t\tSTATE\tCONSUMPTION\n");
        for(Map.Entry<String, ActorRef> ref: this.appliances.entrySet()){
            int nameLength = 16;
            String space = " ";
            String repeatedSpace = IntStream.range(0, nameLength-ref.getKey().length()).mapToObj(i->space).collect(Collectors.joining(""));
            list.append(ref.getKey()).append(repeatedSpace).append("\t");
            try{
                scala.concurrent.Future<Object> waitingForState = ask(ref.getValue(), new RequestMessage(MessageType.MACHINELIST, null), 5000);
                list.append(waitingForState.result(timeout, null)).append("\t\t");
                scala.concurrent.Future<Object> waitingForConsumption = ask(ref.getValue(), new RequestMessage(MessageType.GETCONSUMPTION, null), 5000);
                float consumption = (Float) waitingForConsumption.result(timeout, null);
                totalConsumption += consumption;
                list.append(consumption).append("W\n");
            }catch (Exception e){
                list = new StringBuilder("[ERROR] Something went wrong getting the appliances list");
                return new ResponseMessage(false, list.toString());
            }
        }
        list.append("---------------------------------\n");
        list.append("TOTAL CONSUMPTION: ").append(totalConsumption).append("W\n");
        list.append("DESIRED TEMPERATURE: ").append(this.desiredTemperature).append("°C\n");
        try {
            list.append("TEMPERATURE: ").append(this.getTemperature()).append("°C\n");
        }catch (Exception e){
            list = new StringBuilder("[ERROR] Something went wrong getting the temperature.");
            return new ResponseMessage(false, list.toString());
        }
        return new ResponseMessage(false, list.toString());
    }
    private ResponseMessage switchAppliance(String name) {
        String response;
        boolean error = false;
        ActorRef appliance = this.appliances.get(name);
        if(appliance == null){
            response = "[ERROR] The appliance does not exist\n";
            error = true;
        }else {
            if(checkConflict(appliance)){
                String opposite = "Air Conditioning";
                if(!name.equals(opposite)){
                    opposite = "Thermostat";
                }
                response = "[ERROR] " + name + " cannot be turn on because the " + opposite + " is working\n";
            }else {
                try {
                    scala.concurrent.Future<Object> waitingForAppliance = ask(appliance, new RequestMessage(MessageType.SWITCHMACHINE, null), 5000);
                    ResponseMessage applianceMessage =(ResponseMessage) waitingForAppliance.result(timeout, null);
                    response = applianceMessage.getMessage() + "\n";
                }catch (Exception e){
                    response = "[ERROR] Something went wrong with switching the appliance";
                    return new ResponseMessage(false, response);
                }

            }
        }
        response += getAppliancesList().getMessage();
        System.out.println(response);
        return new ResponseMessage(error, response);
    }
    private ResponseMessage changeTemperature(String newTemperature) throws InterruptedException, TimeoutException{
        float actualTemperature = getTemperature();
        float newTemp = Float.parseFloat(newTemperature);
        ResponseMessage response;
        if(newTemp <= actualTemperature - 1 && newTemp > 10){
           response = switchAppliance("AirConditioning");
           this.desiredTemperature = newTemp;
        }else if(newTemp >= actualTemperature + 1 && newTemp < 30){
           response = switchAppliance("Thermostat");
           this.desiredTemperature = newTemp;
        }else{
            response = new ResponseMessage(false, "[ERROR] The temperature inserted is not valid");
        }
        return response;
    }



    static Props props() {
        return Props.create(ControlPanel.class);
    }

    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("config/server.conf"));
        ActorSystem sys = ActorSystem.create("Server", conf);
        ActorRef supervisor = sys.actorOf(ControlPanel.props(), "controlPanel");
        System.out.println("[LOG] The control panel is functional");
        supervisor.tell(new CreateActorMessage(AirConditioning.props(), "AirConditioning"), ActorRef.noSender());
        supervisor.tell(new CreateActorMessage(Thermostat.props(), "Thermostat"), ActorRef.noSender());
        supervisor.tell(new CreateActorMessage(WashingMachine.props(), "WashingMachine"), ActorRef.noSender());
        supervisor.tell(new CreateActorMessage(Television.props(), "Television"), ActorRef.noSender());
    }
}
