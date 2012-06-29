package org.fusesource.amqp.future;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.amqp.Future;
import org.fusesource.hawtdispatch.DispatchQueue;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPConnection {
    DispatchQueue queue();

    AMQPConnectionOptions getOptions();

    Future<Void> close();

    Future<Void> close(String error);

    Future<AMQPSession> createSession(AMQPSessionOptions sessionOptions);

    String remoteContainerId();
}
