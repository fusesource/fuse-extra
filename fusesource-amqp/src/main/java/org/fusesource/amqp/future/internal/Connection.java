package org.fusesource.amqp.future.internal;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.amqp.Future;
import org.fusesource.amqp.callback.Callback;
import org.fusesource.amqp.future.AMQPConnection;
import org.fusesource.amqp.future.AMQPSession;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Connection implements AMQPConnection {

    private final org.fusesource.amqp.callback.internal.Connection connection;

    public Connection(org.fusesource.amqp.callback.AMQPConnection connection) {
        this.connection = (org.fusesource.amqp.callback.internal.Connection) connection;
    }

    public DispatchQueue queue() {
        return connection.queue();
    }
    public AMQPConnectionOptions getOptions() {
        return connection.getOptions();
    }

    private void queue(Task task) {
        queue().execute(task);
    }

    public Future<Void> close() {
        final Promise<Void> promise = new Promise<Void>();
        queue(new Task(){
            public void run() {
                connection.close(promise);
            }
        });
        return promise;
    }

    public Future<Void> close(final String error) {
        final Promise<Void> promise = new Promise<Void>();
        queue(new Task(){
            public void run() {
                connection.close(error, promise);
            }
        });
        return promise;
    }

    public Future<AMQPSession> createSession(final AMQPSessionOptions sessionOptions) {
        final Promise<AMQPSession> promise = new Promise<AMQPSession>();
        queue(new Task(){
            public void run() {
                connection.createSession(sessionOptions, new Callback<org.fusesource.amqp.callback.AMQPSession>(){
                    public void onSuccess(org.fusesource.amqp.callback.AMQPSession value) {
                        promise.onSuccess(new Session(Connection.this, value));
                    }
                    public void onFailure(Throwable value) {
                        promise.onFailure(value);
                    }
                });
            }
        });
        return promise;
    }


    public String remoteContainerId() {
        final Promise<String> promise = new Promise<String>();
        queue(new Task(){
            public void run() {
                promise.onSuccess(connection.remoteContainerId());
            }
        });
        return promise.awaitNoException();
    }

}
