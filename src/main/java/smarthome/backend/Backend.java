package smarthome.backend;

import akka.actor.*;
import smarthome.messages.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Backend extends AbstractActor {

    private Map<String, Float> rooms = new HashMap<>();

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActivateMessage.class, this::startBackend)
                .match(ConsumptionMessage.class, this::updateRoom)
                .match(ResponseMessage.class, this::printLog)
                .match(RoomDisconnectedMessage.class, this::removeRoom)
                .build();
    }

    private void startBackend(ActivateMessage message){
        System.out.println("[LOG] Backend Server started and functional");
    }
    private void printLog(ResponseMessage message){
        System.out.println("[LOG] " + message.getMessage());
    }
    private void updateRoom(ConsumptionMessage message){
        rooms.put(message.getName(), message.getConsumption());
        printRooms();
    }
    private void removeRoom(RoomDisconnectedMessage message){
        rooms.remove(message.getRoomName());
        printRooms();
    }

    private void printRooms(){
        float total = 0;
        StringBuilder list = new StringBuilder("----------SMARTHOME CONSUMPTION---------\n");
        list.append("ROOM\t\t\tCONSUMPTION\n");
        for(Map.Entry<String, Float> entry: rooms.entrySet()){
            int nameLength = 16;
            String space = " ";
            String repeatedSpace = IntStream.range(0, nameLength-entry.getKey().length()).mapToObj(i->space).collect(Collectors.joining(""));
            list.append(entry.getKey()).append(repeatedSpace).append("\t");
            list.append(entry.getValue()).append("W\n");
            total += entry.getValue();
        }
        list.append("----------------------------------------\n");
        list.append("TOTAL CONSUMPTION: ").append(total).append("W\n\n");
        System.out.println(list);
    }


    public static Props props(){
        return Props.create(Backend.class);
    }


}
