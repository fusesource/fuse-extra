package org.fusesource.amqp.blocking;

import org.fusesource.amqp.AMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPEndpoint {
    void attach(AMQPSession session) throws AMQPException;

    void close() throws AMQPException;

    void close(String error) throws AMQPException;

    void detach() throws AMQPException;

    void detach(String error) throws AMQPException;

    AMQPSession getSession();
}
