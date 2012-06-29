package org.fusesource.amqp.blocking.internal;

import org.fusesource.amqp.blocking.AMQPConnection;
import org.fusesource.amqp.blocking.AMQPSession;
import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.hawtdispatch.DispatchQueue;
import static org.fusesource.amqp.future.internal.Promise.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Connection implements AMQPConnection {
    private final org.fusesource.amqp.future.AMQPConnection connection;

    public Connection(org.fusesource.amqp.future.AMQPConnection connection) {
        this.connection = connection;
    }

    public void close() throws AMQPException {
        awaitWithAMQPException(connection.close());
    }

    public void close(String error) throws AMQPException {
        awaitWithAMQPException(connection.close(error));
    }

    public AMQPSession createSession(long incomingWindow, long outgoingWindow) throws AMQPException {
        return createSession(new AMQPSessionOptions(incomingWindow, outgoingWindow));
    }

    public AMQPSession createSession(AMQPSessionOptions sessionOptions) throws AMQPException {
        return new Session(awaitWithAMQPException(connection.createSession(sessionOptions)));
    }

    public AMQPConnectionOptions getOptions() {
        return connection.getOptions();
    }

    public DispatchQueue queue() {
        return connection.queue();
    }

    public String remoteContainerId() {
        return connection.remoteContainerId();
    }
}
