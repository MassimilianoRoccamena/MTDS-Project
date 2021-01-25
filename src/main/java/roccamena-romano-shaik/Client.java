package com.SmartHome;

import akka.actor.*;
import com.SmartHome.Messages.ActivateMessage;
import com.SmartHome.Messages.MessageType;
import com.SmartHome.Messages.RequestMessage;
import com.SmartHome.Messages.ResponseMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static akka.pattern.Patterns.ask;

public class Client extends AbstractActor {
    private ActorSelection server;
    private Scanner scanner;
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::initializeCli)
                .match(ResponseMessage.class, this::showMessage)
                .build();
    }

    private void initializeCli(ActivateMessage message){
        this.server = context().actorSelection("akka.tcp://Server@127.0.0.1:2552/user/controlPanel");
        this.scanner = new Scanner(System.in);
        System.out.println("-----------WELCOME TO THE SMART HOUSE INTERFACE-----------------\n");
        server.tell(new RequestMessage(MessageType.MACHINELIST, null), self());
    }

    private void chooseFunction(){
        System.out.println("Choose the function:");
        System.out.println("(1) Refresh appliances list");
        System.out.println("(2) Turn ON/OFF");
        System.out.println("(3) Change temperature");
        switch (scanner.nextInt()){
            case 1:
                server.tell(new RequestMessage(MessageType.MACHINELIST,null), self());
                break;
            case 2:
                chooseAppliance();
                break;
        }
    }

    private void chooseAppliance(){
        System.out.println("Insert the appliance name");
        if(scanner.hasNextLine()){
            scanner.nextLine();
        }
        String name = scanner.nextLine();
        server.tell(new RequestMessage(MessageType.SWITCHMACHINE, name), self());
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


    public static void main(String[] args) {
        Config conf =
                ConfigFactory.parseFile(new File("src/main/java/com/SmartHome/config/client.conf"));
        ActorSystem sys = ActorSystem.create("Client", conf);
        ActorRef client = sys.actorOf(Client.props(), "clientActor");
        final ExecutorService exec = Executors.newFixedThreadPool(1);
        exec.submit(() -> client.tell(new ActivateMessage(), ActorRef.noSender()));
    }
}
