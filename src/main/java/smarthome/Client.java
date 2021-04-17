package smarthome;

import akka.actor.*;
import smarthome.messages.ActivateMessage;
import smarthome.messages.MessageType;
import smarthome.messages.RequestMessage;
import smarthome.messages.ResponseMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client extends AbstractActor {
    private ActorSelection server;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::initializeCli)
                .match(ResponseMessage.class, this::showMessage)
                .build();
    }

    private void initializeCli(ActivateMessage message){
        this.server = context().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/backend/controlPanel");
        System.out.println("-----------WELCOME TO THE SMART HOUSE INTERFACE-----------------\n");
        server.tell(new RequestMessage(MessageType.MACHINELIST, null), self());
    }

    private void chooseFunction(){
        System.out.println("Choose the function:");
        System.out.println("(1) Refresh appliances list");
        System.out.println("(2) Turn ON/OFF");
        System.out.println("(3) Change temperature");
        Scanner scanner = new Scanner(System.in);
        switch (scanner.nextInt()){
            case 1:
                server.tell(new RequestMessage(MessageType.MACHINELIST,null), self());
                break;
            case 2:
                chooseAppliance();
                break;
            case 3:
                chooseTemperature();
                break;
            default:
                System.out.println("[ERROR] Insert a valid function number");
                server.tell(new RequestMessage(MessageType.MACHINELIST, null), self());
        }
    }

    private void chooseAppliance(){
        System.out.println("Insert the appliance name");
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        server.tell(new RequestMessage(MessageType.SWITCHMACHINE, name), self());
    }
    private void chooseTemperature(){
        System.out.println("Insert the desired temperature");
        Scanner scanner = new Scanner(System.in);
        try{
            float temperature = scanner.nextFloat();
            server.tell(new RequestMessage(MessageType.CHANGETEMPERATURE, Float.toString(temperature)),self());
        }catch (Exception e){
            System.out.println("[ERROR] Insert a temperature in the correct format (e.g. 15,5)");
            chooseTemperature();
        }
    }

    private void showMessage(ResponseMessage message){
        System.out.println(message.getMessage());
        if(message.isArg()){
            chooseAppliance();
        }else {
            chooseFunction();
        }
    }


    static Props props() {
        return Props.create(Client.class);
    }
}
