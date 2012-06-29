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

package org.fusesource.amqp.callback;

import org.fusesource.amqp.*;
import org.fusesource.amqp.callback.internal.Connection;
import org.fusesource.amqp.callback.internal.Receiver;
import org.fusesource.amqp.callback.internal.Sender;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.hawtdispatch.transport.DefaultTransportListener;
import org.fusesource.hawtdispatch.transport.SslTransport;
import org.fusesource.hawtdispatch.transport.TcpTransport;
import org.fusesource.hawtdispatch.transport.Transport;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.fusesource.hawtdispatch.Dispatch.NOOP;
import static org.fusesource.hawtdispatch.Dispatch.createQueue;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQP {

    private static final long KEEP_ALIVE = Long.parseLong(System.getProperty("amqp.thread.keep_alive", ""+1000));
    private static final long STACK_SIZE = Long.parseLong(System.getProperty("amqp.thread.stack_size", ""+1024*512));

    private static ThreadPoolExecutor blockingThreadPool;
    public synchronized static ThreadPoolExecutor getBlockingThreadPool() {
        if( blockingThreadPool == null ) {
            blockingThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE, AMQP.KEEP_ALIVE, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), new ThreadFactory() {
                    public Thread newThread(Runnable r) {
                        Thread rc = new Thread(null, r, "AMQP Task", AMQP.STACK_SIZE);
                        rc.setDaemon(true);
                        return rc;
                    }
                }) {

                    @Override
                    public void shutdown() {
                        // we don't ever shutdown since we are shared..
                    }

                    @Override
                    public List<Runnable> shutdownNow() {
                        // we don't ever shutdown since we are shared..
                        return Collections.emptyList();
                    }
                };
        }
        return blockingThreadPool;
    }
    public synchronized static void setBlockingThreadPool(ThreadPoolExecutor pool) {
        blockingThreadPool = pool;
    }

    public static final URI DEFAULT_HOST;
    static {
        try {
            DEFAULT_HOST = new URI("tcp://127.0.0.1:5672");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    static public AMQPConnection open(AMQPClientOptions o, final Callback<AMQPConnection> cb) {
        return open(o, (ExceptionListener) null, cb);
    }

    static public AMQPConnection open(AMQPClientOptions o, ExceptionListener el, final Callback<AMQPConnection> cb) {
        final ExceptionListener exceptionListener;
        if( el != null ) {
            exceptionListener = el;
        } else {
            exceptionListener = new ExceptionListener();
        }

        final AMQPConnection.Listener connectionListener = new AMQPConnection.Listener() {
            @Override
            public void onException(Throwable error) {
                exceptionListener.onException(error);
            }
        };

        AMQPClientOptions options = (AMQPClientOptions) o.copy();
        if( options.getDispatchQueue()==null ) {
            options.setDispatchQueue(createQueue("amqp client to: "+options.getHost()));
        }

        final Connection connection = new Connection(options, connectionListener);
        try {
            createTransport(options, new Callback<Transport>() {
                public void onFailure(Throwable value) {
                    cb.onFailure(value);
                }

                public void onSuccess(Transport transport) {
                    connection.setTransport(transport);
                    if (connection.isClosed()) {
                        transport.stop(new Task() {
                            public void run() {
                                cb.onFailure(new IllegalStateException("connection closed."));
                            }
                        });
                    } else {
                        connection.open(cb);
                    }
                }
            });
        } catch (Throwable e) {
            cb.onFailure(e);
        }
        return connection;

    }

    static public AMQPConnection open(AMQPServerConnectionOptions options, final Callback<AMQPConnection> cb) {
        AMQPConnectionOptions o = options.copy();
        final Connection connection = new Connection(o, options.getListener());
        connection.setTransport(options.getTransport());
        connection.open(cb);
        return connection;
    }

    /**
     * Creates and start a transport to the MQTT server.  Passes it to the onConnect
     * once the transport is connected.
     *
     *
     * @param onConnect
     * @throws Exception
     */
    static void createTransport(final AMQPClientOptions options, final Callback<Transport> onConnect) throws Exception {
        String scheme = options.getHost().getScheme();
        final Transport transport;
        if( "tcp".equals(scheme) ) {
            transport = new TcpTransport();
        }  else if( SslTransport.protocol(scheme)!=null ) {
            SslTransport ssl = new SslTransport();
            if( options.getSslContext() == null ) {
                options.setSslContext(SSLContext.getInstance(SslTransport.protocol(scheme)));
            }
            ssl.setSSLContext(options.getSslContext());
            if( options.getBlockingExecutor() == null ) {
                options.setBlockingExecutor(getBlockingThreadPool());
            }
            ssl.setBlockingExecutor(options.getBlockingExecutor());
            transport = ssl;
        } else {
            throw new Exception("Unsupported URI scheme '"+scheme+"'");
        }

        transport.setDispatchQueue(options.getDispatchQueue());
        if( transport instanceof TcpTransport ) {
            TcpTransport tcp = (TcpTransport)transport;
            tcp.setMaxReadRate(options.getMaxReadRate());
            tcp.setMaxWriteRate(options.getMaxWriteRate());
            tcp.setReceiveBufferSize(options.getReceiveBufferSize());
            tcp.setSendBufferSize(options.getSendBufferSize());
            tcp.setTrafficClass(options.getTrafficClass());
            tcp.setUseLocalHost(options.isUseLocalHost());
            tcp.connecting(options.getHost(), options.getLocalAddress());
        }

        transport.setTransportListener(new DefaultTransportListener() {
            public void onTransportConnected() {
                transport.resumeRead();
                onConnect.onSuccess(transport);
            }

            public void onTransportFailure(final IOException error) {
                onFailure(error);
            }

            private void onFailure(final Throwable error) {
                if (!transport.isClosed()) {
                    transport.stop(new Task() {
                        public void run() {
                            onConnect.onFailure(error);
                        }
                    });
                }
            }
        });
        transport.start(NOOP);
    }

    public final static AMQPQoS AT_MOST_ONCE = AMQPQoS.AT_MOST_ONCE;
    public final static AMQPQoS AT_LEAST_ONCE = AMQPQoS.AT_LEAST_ONCE;
    public final static AMQPQoS EXACTLY_ONCE = AMQPQoS.EXACTLY_ONCE;

    public static class Delivery {
        public final Buffer tag;
        public final Buffer message;
        Delivery(Buffer tag, Buffer message) {
            this.message = message;
            this.tag = tag;
        }
    }

    static public AMQPReceiver createReceiver(AMQPReceiverOptions options) {
        return new Receiver(options);
    }

    static public AMQPSender createSender(AMQPSenderOptions options) {
        return new Sender(options);
    }
}
