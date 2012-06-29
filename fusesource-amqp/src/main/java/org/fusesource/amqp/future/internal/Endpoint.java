package org.fusesource.amqp.future.internal;

import org.fusesource.amqp.Future;
import org.fusesource.amqp.future.*;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class Endpoint implements AMQPEndpoint {
        
    abstract protected org.fusesource.amqp.callback.AMQPEndpoint endpoint();

    public Future<Void> attach(final org.fusesource.amqp.future.AMQPSession session) {
        final Promise<Void> promise = new Promise<Void>();
        endpoint().queue().execute(new Task() {
            public void run() {
                endpoint().attach(((Session) session).callbackSession(), promise);
            }
        });
        return promise;
    }

    public Future<Void> close() {
        return this.close(null);
    }
    public Future<Void> close(final String error) {
        final Promise<Void> promise = new Promise<Void>();
        endpoint().queue().execute(new Task() {
            public void run() {
                endpoint().detach(true, error, promise);
            }
        });
        return promise;
    }
    
    public Future<Void> detach() {
        return this.detach(null);
    }
    public Future<Void> detach(final String error) {
        final Promise<Void> promise = new Promise<Void>();
        endpoint().queue().execute(new Task() {
            public void run() {
                endpoint().detach(false, error, promise);
            }
        });
        return promise;
    }

    public AMQPSession getSession() {
        final Promise<AMQPSession> promise = new Promise<AMQPSession>();
        endpoint().queue().execute(new Task() {
            public void run() {
                org.fusesource.amqp.callback.internal.Session session = (org.fusesource.amqp.callback.internal.Session) endpoint().getSession();
                if( session == null ) {
                    promise.onSuccess(null);
                } else {
                    promise.onSuccess((AMQPSession) session.getAttachment());
                }
            }
        });
        return promise.awaitNoException();
    }

}
