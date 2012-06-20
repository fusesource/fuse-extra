package org.fusesource.fabric.apollo.amqp;

import org.fusesource.fabric.apollo.amqp.codec.api.AnnotatedMessage;
import org.fusesource.fabric.apollo.amqp.codec.types.Attach;
import org.fusesource.fabric.apollo.amqp.codec.types.Detach;
import org.fusesource.hawtbuf.Buffer;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPDelivery {

    long format();
    Buffer payload();
    AnnotatedMessage getMessage() throws Exception;

    boolean isSettled();
    void ack();
    void nack();

}
