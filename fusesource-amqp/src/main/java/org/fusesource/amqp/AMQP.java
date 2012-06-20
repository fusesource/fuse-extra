package org.fusesource.amqp;

import org.fusesource.amqp.internal.Connection;
import org.fusesource.amqp.internal.Receiver;
import org.fusesource.amqp.internal.Sender;
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
        if( o.transport !=null ) {
            return open(o.copy(), cb);
        } else {
            AMQPClientOptions options = (AMQPClientOptions) o.copy();
            if( options.dispatchQueue==null ) {
                if( o.transport !=null ) {
                    options.dispatchQueue = o.transport.getDispatchQueue();
                } else {
                    options.dispatchQueue = createQueue("amqp client to: "+options.host);
                }
            }

            final Connection connection = new Connection(options);
            try {
                createTransport(options, new Callback<Transport>() {
                    public void onFailure(Throwable value) {
                        cb.onFailure(value);
                    }

                    public void onSuccess(Transport transport) {
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
    }

    static public AMQPConnection open(AMQPConnectionOptions options, final Callback<AMQPConnection> cb) {
        AMQPConnectionOptions o = options.copy();
        final Connection connection = new Connection(o);
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
        String scheme = options.host.getScheme();

        if( "tcp".equals(scheme) ) {
            options.transport = new TcpTransport();
        }  else if( SslTransport.protocol(scheme)!=null ) {
            SslTransport ssl = new SslTransport();
            if( options.sslContext == null ) {
                options.sslContext = SSLContext.getInstance(SslTransport.protocol(scheme));
            }
            ssl.setSSLContext(options.sslContext);
            if( options.blockingExecutor == null ) {
                options.blockingExecutor = getBlockingThreadPool();
            }
            ssl.setBlockingExecutor(options.blockingExecutor);
            options.transport = ssl;
        } else {
            throw new Exception("Unsupported URI scheme '"+scheme+"'");
        }

        options.transport.setDispatchQueue(options.dispatchQueue);
        if( options.transport instanceof TcpTransport ) {
            TcpTransport tcp = (TcpTransport)options.transport;
            tcp.setMaxReadRate(options.maxReadRate);
            tcp.setMaxWriteRate(options.maxWriteRate);
            tcp.setReceiveBufferSize(options.receiveBufferSize);
            tcp.setSendBufferSize(options.sendBufferSize);
            tcp.setTrafficClass(options.trafficClass);
            tcp.setUseLocalHost(options.useLocalHost);
            tcp.connecting(options.host, options.localAddress);
        }

        options.transport.setTransportListener(new DefaultTransportListener(){
            public void onTransportConnected() {
                onConnect.onSuccess(options.transport);
            }

            public void onTransportFailure(final IOException error) {
                onFailure(error);
            }

            private void onFailure(final Throwable error) {
                if(!options.transport.isClosed()) {
                    options.transport.stop(new Task() {
                        public void run() {
                            onConnect.onFailure(error);
                        }
                    });
                }
            }
        });
        options.transport.start(NOOP);
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
