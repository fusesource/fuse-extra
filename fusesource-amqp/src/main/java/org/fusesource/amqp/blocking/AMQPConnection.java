package org.fusesource.amqp.blocking;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.hawtdispatch.DispatchQueue;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPConnection {
    void close() throws AMQPException;

    void close(String error) throws AMQPException;

    AMQPSession createSession(long incomingWindow, long outgoingWindow) throws AMQPException;

    AMQPSession createSession(AMQPSessionOptions sessionOptions) throws AMQPException;

    AMQPConnectionOptions getOptions();

    DispatchQueue queue();

    String remoteContainerId();
}
