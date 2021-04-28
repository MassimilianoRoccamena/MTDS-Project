package smarthome.messages;

import java.io.Serializable;

public class ConsumptionMessage implements Serializable {
    private String name;
    private float consumption;

    public ConsumptionMessage(String name, float consumption) {
        this.name = name;
        this.consumption = consumption;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getConsumption() {
        return consumption;
    }

    public void setConsumption(float consumption) {
        this.consumption = consumption;
    }
}
