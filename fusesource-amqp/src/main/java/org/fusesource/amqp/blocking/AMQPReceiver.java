package org.fusesource.amqp.blocking;

import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.callback.AMQPDelivery;
import org.fusesource.amqp.types.Envelope;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPReceiver extends AMQPEndpoint {

    void addCredit(int value);
    AMQPDelivery peek();
    AMQPDelivery poll();

    Envelope receive() throws AMQPException;
}
