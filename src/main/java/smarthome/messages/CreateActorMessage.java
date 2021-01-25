package smarthome.messages;

import akka.actor.Props;

public class CreateActorMessage {
    private Props props;
    private String name;

    public CreateActorMessage(Props props, String name) {
        this.props = props;
        this.name = name;
    }

    public Props getProps() {
        return props;
    }

    public void setProps(Props props) {
        this.props = props;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
