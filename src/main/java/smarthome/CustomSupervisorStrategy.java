package smarthome;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;

public class CustomSupervisorStrategy extends OneForOneStrategy {
    public CustomSupervisorStrategy( int maxNrOfRetries, Duration withinTimeRange, boolean loggingEnabled, PartialFunction<Throwable, Directive> decider) {
        super(maxNrOfRetries, withinTimeRange, loggingEnabled, decider);
    }

    @Override
    public void logFailure(ActorContext context, ActorRef child, Throwable cause, Directive decision) {
        System.out.println("The appliance " + child + " has thrown an exception. Restarting the Appliance.....");
    }
}
