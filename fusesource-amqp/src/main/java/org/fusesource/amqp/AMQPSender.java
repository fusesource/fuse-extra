package org.fusesource.amqp;

import org.fusesource.amqp.codec.api.AnnotatedMessage;
import org.fusesource.amqp.codec.interfaces.DeliveryState;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSender extends AMQPEndpoint {
    boolean full();
    void send(AnnotatedMessage message, Callback<DeliveryState> callback);
}
