package smarthome;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import smarthome.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ControlPanel extends AbstractActor{
    private final ActorRef backend = getContext().getParent();
    private final Map<String, ActorRef> appliances = new HashMap<>();
    private float desiredTemperature = 20;
    private final scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
    private final SupervisorStrategy strategy =
            new CustomSupervisorStrategy(10,
                    timeout,
                    true,
                    DeciderBuilder
                            .match(Exception.class, e -> (SupervisorStrategy.Directive) SupervisorStrategy.escalate())
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
    private void onRequest(RequestMessage message) {
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
    private float getTemperature() {
        try {
            ActorRef thermostat = this.appliances.get("Thermostat");
            scala.concurrent.Future<Object> waitingForAppliance = ask(thermostat, new RequestMessage(MessageType.GETTEMPERATURE, null), 5000);
            return (Float) waitingForAppliance.result(timeout, null);
        }catch (Exception e){
            backend.tell(new ResponseMessage(true, "Problem encountered with getting the temperature!"), self());
            return 0.0f;
        }

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
            backend.tell(new ResponseMessage(true, "Could not find if there is a conflict between appliances"), self());
            return true;
        }
        return conflict;
    }
    //Function called when a time Appliance stops working
    private void applianceManage(ResponseMessage message) {
        String responseMessage = message.getMessage();
        float actualTemperature = getTemperature();
        if(responseMessage.equals("-1") || responseMessage.equals("1")){

            if(actualTemperature < 10 || (actualTemperature <= desiredTemperature && responseMessage.equals("-1"))){
                switchAppliance("AirConditioning");
                if(actualTemperature >= desiredTemperature + 1 || actualTemperature <= desiredTemperature - 1){
                    changeTemperature(Float.toString(desiredTemperature));
                }
            }else if(actualTemperature > 25 || (actualTemperature >= desiredTemperature && responseMessage.equals("1"))){
                switchAppliance("Thermostat");
                if(actualTemperature >= desiredTemperature + 1 || actualTemperature <= desiredTemperature - 1){
                    changeTemperature(Float.toString(desiredTemperature));
                }
            }else {
                ActorRef thermostat = this.appliances.get("Thermostat");
                thermostat.tell(new RequestMessage(MessageType.CHANGETEMPERATURE, message.getMessage()),self());
            }
            backend.tell(new ResponseMessage(false, "The temperature is now " + actualTemperature + "°C"), self());
        }else {
            backend.tell(new ResponseMessage(message.isArg(), message.getMessage()), self());
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
                String opposite = "AirConditioning";
                if(name.equals(opposite)){
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
        backend.tell(new ResponseMessage(error, response), self());
        return new ResponseMessage(error, response);
    }
    private ResponseMessage changeTemperature(String newTemperature){
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
}
