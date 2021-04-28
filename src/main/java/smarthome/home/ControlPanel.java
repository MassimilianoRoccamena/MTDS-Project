package smarthome.home;

import akka.actor.*;
import smarthome.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ControlPanel extends AbstractActor{
    private ActorSelection backend;
    private ActorSelection room;
    private final Map<String, ActorSelection> rooms = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConsumptionMessage.class, this::notifyConsumption)
                .match(ResponseMessage.class, this::showMessage)
                .match(ActivateMessage.class, this::activatePanel)
                .build();
    }

    private void activatePanel(ActivateMessage message){
        this.backend = context().actorSelection("akka.tcp://Backend@192.168.56.101:2550/user/backend");
        this.backend.tell(new ResponseMessage(false, "Control Panel Activated"), self());
        ActorSelection lr = context().actorSelection("akka.tcp://LivingRoom@192.168.56.1:2551/user/livingRoom");
        ActorSelection kitchen = context().actorSelection("akka.tcp://Kitchen@192.168.56.1:2553/user/kitchen");
        rooms.put("livingRoom", lr);
        rooms.put("kitchen", kitchen);
        roomsList();
    }

    private void showMessage(ResponseMessage message){
        System.out.println(message.getMessage());
        if(message.isArg()){
            chooseAppliance();
        }else {
            chooseFunction();
        }
    }
    public void roomsList(){
        boolean room = true;
        StringBuilder list;
        String name = "";
        while (room){
            list = new StringBuilder("-----------WELCOME TO THE SMART HOUSE INTERFACE-----------------\n-----ROOMS LIST-----\n");
            for(Map.Entry<String, ActorSelection> ref: rooms.entrySet()){
                list.append(ref.getKey()).append("\n");
            }
            list.append("---------------------\n");
            list.append("Insert room name");
            System.out.println(list);
            Scanner scanner = new Scanner(System.in);
            name = scanner.nextLine();
            if(rooms.containsKey(name)){
                room = false;
                this.room = rooms.get(name);
            }
            else{
                System.out.println("[ERROR] The room does not exist");
            }
        }
        this.room.tell(new RequestMessage(MessageType.MACHINELIST, null), self());
    }


    public void chooseFunction(){
        System.out.println("Choose the function:");
        System.out.println("(1) Refresh appliances list");
        System.out.println("(2) Turn ON/OFF");
        System.out.println("(3) Change temperature");
        System.out.println("(4) Return to rooms");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextInt()){
            case 1:
                room.tell(new RequestMessage(MessageType.MACHINELIST,null), self());
                break;
            case 2:
                chooseAppliance();
                break;
            case 3:
                chooseTemperature();
                break;
            case 4:
                roomsList();
                break;
            default:
                System.out.println("[ERROR] Insert a valid function number");
                room.tell(new RequestMessage(MessageType.MACHINELIST, null), self());
        }
    }

    public void chooseAppliance(){
        System.out.println("Insert the appliance name");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        room.tell(new RequestMessage(MessageType.SWITCHMACHINE, name), self());
    }
    public void chooseTemperature(){
        System.out.println("Insert the desired temperature");
        Scanner scanner = new Scanner(System.in);
        try{
            float temperature = scanner.nextFloat();
            room.tell(new RequestMessage(MessageType.CHANGETEMPERATURE, Float.toString(temperature)), self());
        }catch (Exception e){
            System.out.println("[ERROR] Insert a temperature in the correct format (e.g. 15,5)");
            chooseTemperature();
        }
    }

    public void notifyConsumption(ConsumptionMessage message){
        this.backend.tell(message, self());
    }

    public static Props props() {
        return Props.create(ControlPanel.class);
    }

}
