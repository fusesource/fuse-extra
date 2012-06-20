package org.fusesource.fabric.apollo.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPReceiver extends AMQPEndpoint {

    void addCredit(int value);
    AMQPDelivery poll();
    AMQPDelivery peek();

}
