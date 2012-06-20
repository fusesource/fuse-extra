package org.fusesource.fabric.apollo.amqp.internal;

import org.fusesource.fabric.apollo.amqp.*;
import org.fusesource.fabric.apollo.amqp.codec.AMQPProtocolCodec;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPFrame;
import org.fusesource.fabric.apollo.amqp.codec.types.*;
import org.fusesource.fabric.apollo.amqp.codec.types.Error;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.hawtdispatch.transport.DefaultTransportListener;
import org.fusesource.hawtdispatch.transport.ProtocolCodec;
import org.fusesource.hawtdispatch.transport.Transport;

import java.io.IOException;

import static org.fusesource.fabric.apollo.amqp.internal.Support.fail;
import static org.fusesource.fabric.apollo.amqp.internal.Support.requireArgument;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Connection implements AMQPConnection {

    final ConnectionState state = new ConnectionState(this);
    final AMQPConnectionOptions options;
    private DispatchQueue queue;

    public Connection(AMQPConnectionOptions connectionOptions) {
        this.options = connectionOptions;
    }

    private void init() throws Exception {
        queue = options.getTransport().getDispatchQueue();
        queue().assertExecuting();
        final Transport transport = options.getTransport();
        ProtocolCodec current = transport.getProtocolCodec();
        if( current == null || !(current instanceof AMQPProtocolCodec)) {
            transport.setProtocolCodec(new AMQPProtocolCodec());
        }
        transport.setTransportListener(new DefaultTransportListener() {
            @Override
            public void onTransportConnected() {
                transport.resumeRead();
            }

            public void onTransportCommand(Object command) {
                try {
                    state.current().onAMQPFrame((AMQPFrame) command);
                } catch (Throwable error) {
                    state.current().onProcessingError(error);
                }
            }
            public void onTransportFailure(IOException error) {
                state.current().onTransportError(error);
            }
            public void onRefill() {
                state.current().onTransportRefill();
            }
        });
    }

    Transport transport() {
        return options.getTransport();
    }

    DispatchQueue queue() {
        return queue;
    }
    void queue(Task task) {
        queue.execute(task);
    }

    ///////////////////////////////////////////////////////////////////
    // Internal Interface
    ///////////////////////////////////////////////////////////////////
    public void open(final Callback<AMQPConnection> callback) {
        requireArgument("callback", callback);
        try {
            init();
            state.openConnection(callback);
        } catch (final Exception e) {
            transport().stop(new Runnable() {
                public void run() {
                    callback.onFailure(e);
                }
            });
        }
    }

    Listener listener() {
        return options.getListener();
    }

    public boolean isClosed() {
        queue().assertExecuting();
        return state.current().getClass() == ConnectionState.Closed.class;
    }

    AMQPConnectionOptions.Logger logger() {
        return options.getLogger();
    }

    ///////////////////////////////////////////////////////////////////
    // Public Connection Interface
    ///////////////////////////////////////////////////////////////////

    public AMQPConnectionOptions getOptions() {
        return options.copy();
    }

    public String remoteContainerId() {
        queue().assertExecuting();
        if( state.remoteOpen == null ) {
            return null;
        }
        return state.remoteOpen.getContainerID();
    }

    public void close(String description, final Callback<Void> callback) {
        close(AMQPError.INTERNAL_ERROR, description, callback);
    }

    public void close(AMQPError condition, String description, final Callback<Void> callback) {
        queue().assertExecuting();
        Close close = new Close(new Error(condition.getValue(), description));
        state.current().onClose(close, callback);
    }

    public void close(final Callback<Void> callback) {
        queue().assertExecuting();
        Close close = new Close();
        state.current().onClose(close, callback);
    }
    
    public AMQPSession createSession(AMQPSessionOptions sessionOptions, Callback<AMQPSession> callback) {
        queue().assertExecuting();
        requireArgument("callback", callback);
        Session session = state.createSessionImpl(sessionOptions, callback);
        if( session==null ) {
            fail(callback, new AMQPException("Too many open sessions"));
        } else {
            state.openSession(session);
        }
        return session;
    }

    public AMQPSession[] sessions() {
        return state.sessions();
    }

}
