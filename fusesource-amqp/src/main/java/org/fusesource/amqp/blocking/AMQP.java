package org.fusesource.amqp.blocking;

import org.fusesource.amqp.*;
import org.fusesource.amqp.blocking.internal.Connection;
import org.fusesource.amqp.blocking.internal.Receiver;
import org.fusesource.amqp.blocking.internal.Sender;
import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.AMQPReceiverOptions;
import org.fusesource.amqp.AMQPSenderOptions;

import static org.fusesource.amqp.future.internal.Promise.awaitWithAMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQP {
    
    static public AMQPConnection open(AMQPClientOptions o) throws AMQPException {
        return new Connection(awaitWithAMQPException(org.fusesource.amqp.future.AMQP.open(o)));
    }

    static public AMQPReceiver createReceiver(AMQPReceiverOptions options) {
        return new Receiver(org.fusesource.amqp.future.AMQP.createReceiver(options));
    }

    static public Sender createSender(AMQPSenderOptions options) {
        return new Sender(org.fusesource.amqp.future.AMQP.createSender(options));
    }
    
}
