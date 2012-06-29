package org.fusesource.amqp.blocking.internal;

import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.types.*;
import org.fusesource.amqp.future.AMQPSender;

import static org.fusesource.amqp.future.internal.Promise.awaitWithAMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Sender extends Endpoint {
    private final org.fusesource.amqp.future.internal.Sender sender;

    public Sender(AMQPSender sender) {
        this.sender = (org.fusesource.amqp.future.internal.Sender) sender;
    }

    @Override
    protected org.fusesource.amqp.future.internal.Endpoint endpoint() {
        return sender;
    }

    public boolean full() {
        return sender.full();
    }

    public AMQPDeliveryState send(Envelope message) throws AMQPException {
        return awaitWithAMQPException(sender.send(message));
    }

    public AMQPDeliveryState send(Message message) throws AMQPException {
        return awaitWithAMQPException(sender.send(message));
    }
}
