package org.fusesource.amqp.blocking.internal;

import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.future.AMQPSession;

import static org.fusesource.amqp.future.internal.Promise.awaitWithAMQPException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Session implements org.fusesource.amqp.blocking.AMQPSession {

    private final org.fusesource.amqp.future.internal.Session session;

    public Session(org.fusesource.amqp.future.AMQPSession session) {
        this.session = (org.fusesource.amqp.future.internal.Session) session;
        this.session.setAttachment(this);
    }

    public org.fusesource.amqp.future.internal.Session futureSession() {
        return session;
    }

    public void close() throws AMQPException {
        awaitWithAMQPException(session.close());
    }
}
