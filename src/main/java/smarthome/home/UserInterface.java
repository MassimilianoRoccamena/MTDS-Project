package smarthome.home;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import smarthome.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class UserInterface extends AbstractActor {
    private String room;
    private ActorRef panel;
    private Map<String, ActorRef> rooms = new HashMap<>();
    public final scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::controlPanel)
                .match(ResponseMessage.class, this::showMessage)
                .build();
    }

    private void controlPanel(ActivateMessage message){
        this.panel = sender();
        roomsList();
    }

    public void roomsList(){
        boolean room = true;
        StringBuilder list;
        String name = "";
        while (room){
            list = new StringBuilder("-----------WELCOME TO THE SMART HOUSE INTERFACE-----------------\n-----ROOMS LIST-----\n");
            for(Map.Entry<String, ActorRef> ref: rooms.entrySet()){
                list.append(ref.getKey()).append("\n");
            }
            list.append("---------------------\n");
            list.append("Insert 1 to refresh rooms list\n");
            list.append("Insert room name");
            System.out.println(list);
            Scanner scanner = new Scanner(System.in);
            name = scanner.nextLine();
            if(name.equals("1")){
                updateRoomsList();
            }
            else if(rooms.containsKey(name)){
                room = false;
                this.room = name;
            }
            else{
                System.out.println("[ERROR] The room does not exist");
            }
        }
        this.panel.tell(new RequestMessage(MessageType.MACHINELIST, this.room), self());
    }

    private void showMessage(ResponseMessage message){
        System.out.println(message.getMessage());
        if (message.getMessage().contains("shut down")){
            updateRoomsList();
            roomsList();
        }
        else if(message.isArg()){
            chooseAppliance();
        }else {
            chooseFunction();
        }
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
                this.panel.tell(new RequestMessage(MessageType.MACHINELIST, room), self());
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
                this.panel.tell(new RequestMessage(MessageType.MACHINELIST, room), self());
        }
    }
    public void chooseAppliance(){
        System.out.println("Insert the appliance name");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        panel.tell(new RequestMessage(MessageType.SWITCHMACHINE, name), self());
    }
    public void chooseTemperature(){
        System.out.println("Insert the desired temperature");
        Scanner scanner = new Scanner(System.in);
        try{
            float temperature = scanner.nextFloat();
            panel.tell(new RequestMessage(MessageType.CHANGETEMPERATURE, Float.toString(temperature)), self());
        }catch (Exception e){
            System.out.println("[ERROR] Insert a temperature in the correct format (e.g. 15,5)");
            chooseTemperature();
        }
    }

    private void updateRoomsList(){
        try {
            scala.concurrent.Future<Object> waitingForRooms = ask(this.panel, new RequestMessage(MessageType.ROOMSLIST, null), 5000);
            RoomsMessage msg = (RoomsMessage) waitingForRooms.result(timeout, null);
            this.rooms = msg.getRooms();
        }catch (Exception e){
            System.out.println("[ERROR] Could not get the rooms list");
            roomsList();
        }
    }

    public static Props props() {
        return Props.create(UserInterface.class);
    }
}
