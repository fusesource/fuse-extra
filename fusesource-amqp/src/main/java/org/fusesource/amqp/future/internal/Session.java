package org.fusesource.amqp.future.internal;

import org.fusesource.amqp.Future;
import org.fusesource.amqp.future.AMQPConnection;
import org.fusesource.amqp.future.AMQPSession;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Session implements AMQPSession {

    private final AMQPConnection connection;
    private final org.fusesource.amqp.callback.internal.Session session;
    public Object attachment;

    public Session(AMQPConnection connection, org.fusesource.amqp.callback.AMQPSession session) {
        this.connection = connection;
        this.session = (org.fusesource.amqp.callback.internal.Session) session;
        this.session.setAttachment(this);
    }

    public Future<Void> close() {
        return close(null);
    }
    
    public Future<Void> close(final org.fusesource.amqp.types.Error error) {
        final Promise<Void> promise = new Promise<Void>();
        connection.queue().execute(new Task() {
            public void run() {
                session.close(error, promise);
            }
        });
        return promise;
    }


    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    public org.fusesource.amqp.callback.internal.Session callbackSession() {
        return session;
    }
}
