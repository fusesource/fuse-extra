package org.fusesource.amqp;

import org.fusesource.amqp.codec.api.AnnotatedMessage;
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
