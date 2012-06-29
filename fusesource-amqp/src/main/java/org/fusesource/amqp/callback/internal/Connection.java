/**
 * Copyright (C) 2012 FuseSource Corp. All rights reserved.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.amqp.callback.internal;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.amqp.callback.*;
import org.fusesource.amqp.codec.AMQPFrame;
import org.fusesource.amqp.codec.AMQPProtocolCodec;
import org.fusesource.amqp.types.*;
import org.fusesource.amqp.types.Error;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.hawtdispatch.transport.DefaultTransportListener;
import org.fusesource.hawtdispatch.transport.ProtocolCodec;
import org.fusesource.hawtdispatch.transport.Transport;

import java.io.IOException;

import static org.fusesource.amqp.callback.internal.Support.fail;
import static org.fusesource.amqp.callback.internal.Support.requireArgument;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Connection implements AMQPConnection {

    final ConnectionState state = new ConnectionState(this);
    private Transport transport;
    private final Listener listener;
    final AMQPConnectionOptions options;
    private DispatchQueue queue;

    public Connection(AMQPConnectionOptions connectionOptions, Listener listener) {
        this.options = connectionOptions;
        this.listener = listener;
    }

    private void init() throws Exception {
        queue = transport.getDispatchQueue();
        queue().assertExecuting();
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
        return transport;
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
        return listener;
    }

    public boolean isClosed() {
        return state.current().getClass() == ConnectionState.Closed.class;
    }

    AMQPConnectionOptions.Logger logger() {
        return options.getLogger();
    }

    ///////////////////////////////////////////////////////////////////
    // Public Connection Interface
    ///////////////////////////////////////////////////////////////////

    public DispatchQueue queue() {
        return queue;
    }

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

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }
}
