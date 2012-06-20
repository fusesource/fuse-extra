package org.fusesource.fabric.apollo.amqp;

import org.fusesource.fabric.apollo.amqp.codec.api.AnnotatedMessage;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.DeliveryState;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSender extends AMQPEndpoint {
    boolean full();
    void send(AnnotatedMessage message, Callback<DeliveryState> callback);
}
