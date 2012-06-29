package org.fusesource.amqp.blocking;

import org.fusesource.amqp.AMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSession {
    void close() throws AMQPException;
}
