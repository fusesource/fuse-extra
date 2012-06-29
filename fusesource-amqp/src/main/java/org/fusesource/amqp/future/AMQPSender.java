package org.fusesource.amqp.future;

import org.fusesource.amqp.Future;
import org.fusesource.amqp.types.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSender extends AMQPEndpoint {
    boolean full();

    Future<AMQPDeliveryState> send(Envelope message);
}
