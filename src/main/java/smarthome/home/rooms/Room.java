package smarthome.home.rooms;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import smarthome.messages.*;
import smarthome.supervisor.CustomSupervisorStrategy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class Room extends AbstractActor {
    public float desiredTemperature = 20;
    public float totalConsumption = 0;
    public ActorSelection panel = context().actorSelection("akka.tcp://Panel@192.168.56.1:2552/user/controlPanel");
    public String roomName;
    public Map<String, ActorRef> appliances = new HashMap<>();
    public final scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);
    public final SupervisorStrategy strategy =
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
                .match(RequestMessage.class, this::menageRequest)
                .match(ActivateMessage.class, this::activateRoom)
                .match(ResponseMessage.class, this::applianceManage)
                .build();
    }

    public abstract void activateRoom(ActivateMessage message);
    public void createActor(String name, Props prop){
        ActorRef ref = getContext().actorOf(prop, name);
        ref.tell(new ActivateMessage(), self());
        this.appliances.put(name, ref);
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
            printError("Could not find if there is a conflict between appliances");
            return true;
        }
        return conflict;
    }
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
            printLog("The temperature is now " + actualTemperature + "°C");
        }else {
            printLog(message.getMessage());
        }
    }
    private void menageRequest(RequestMessage message) {
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
        sender().tell(new ConsumptionMessage(this.roomName, this.totalConsumption), self());
        sender().tell(response, self());
    }
    private ResponseMessage getAppliancesList() {
        StringBuilder list = new StringBuilder("\n");
        list.append(this.roomName.toUpperCase(Locale.ROOT));
        list.append("\n---------APPLIANCES LIST---------\n");

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
        this.totalConsumption = totalConsumption;
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
    private float getTemperature() {
        try {
            ActorRef thermostat = this.appliances.get("Thermostat");
            scala.concurrent.Future<Object> waitingForAppliance = ask(thermostat, new RequestMessage(MessageType.GETTEMPERATURE, null), 5000);
            return (Float) waitingForAppliance.result(timeout, null);
        }catch (Exception e){
            printError("Problem encountered with getting the temperature!");
            return 0.0f;
        }

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
        if(error){
            printError(response);
        }else {
            printLog(response);
        }
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

    private void printLog(String message){
        System.out.println("[LOG] " + message);
    }
    private void printError(String message){
        System.out.println("[ERROR] " + message);
    }


}
