package org.fusesource.amqp.future;

import org.fusesource.amqp.callback.AMQPDelivery;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPReceiver extends AMQPEndpoint {
    void addCredit(int value);

    AMQPDelivery peek();

    AMQPDelivery poll();
}
