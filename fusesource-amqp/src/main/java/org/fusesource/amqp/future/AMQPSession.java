package org.fusesource.amqp.future;

import org.fusesource.amqp.Future;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSession {
    Future<Void> close();

    Future<Void> close(org.fusesource.amqp.types.Error error);
}
