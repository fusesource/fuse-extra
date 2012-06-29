package org.fusesource.amqp.blocking.internal;

import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.blocking.AMQPSession;

import static org.fusesource.amqp.future.internal.Promise.awaitWithAMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class Endpoint implements org.fusesource.amqp.blocking.AMQPEndpoint {

    abstract protected org.fusesource.amqp.future.internal.Endpoint endpoint();

    public void attach(AMQPSession session) throws AMQPException {
        final org.fusesource.amqp.future.internal.Session s = ((Session) session).futureSession();
        awaitWithAMQPException(endpoint().attach(s));
    }

    public void close() throws AMQPException {
        awaitWithAMQPException(endpoint().close());
    }

    public void close(String error) throws AMQPException {
        awaitWithAMQPException(endpoint().close(error));
    }

    public void detach() throws AMQPException {
        awaitWithAMQPException(endpoint().detach());
    }

    public void detach(String error) throws AMQPException {
        awaitWithAMQPException(endpoint().detach(error));
    }

    public AMQPSession getSession() {
        final org.fusesource.amqp.future.internal.Session session = (org.fusesource.amqp.future.internal.Session) endpoint().getSession();
        if( session == null ) {
            return null;
        }
        return (AMQPSession) session.getAttachment();
    }
}
