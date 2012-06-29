package org.fusesource.amqp.future;

import org.fusesource.amqp.Future;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPEndpoint {
    Future<Void> attach(AMQPSession session);

    Future<Void> close();

    Future<Void> close(String error);

    Future<Void> detach();

    Future<Void> detach(String error);

    AMQPSession getSession();
}
