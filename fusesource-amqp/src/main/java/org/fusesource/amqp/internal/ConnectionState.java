package org.fusesource.amqp.internal;

import org.fusesource.amqp.*;
import org.fusesource.amqp.codec.interfaces.AMQPFrame;
import org.fusesource.amqp.codec.interfaces.Frame;
import org.fusesource.amqp.codec.types.*;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;
import org.fusesource.hawtdispatch.transport.HeartBeatMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import static org.fusesource.amqp.internal.Support.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class ConnectionState extends StateMachine<ConnectionState.BaseState> {

    LinkedList<SendRequest> outbound = new LinkedList<SendRequest>();
    Open remoteOpen;
    private Connection connection;

    final HashMap<Integer, Session> sessions = new HashMap<Integer, Session>();
    
    public ConnectionState(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected BaseState init() {
        return new Created();
    }

    /////////////////////////////////////////////////////////////////////
    // Internal interface
    /////////////////////////////////////////////////////////////////////

    void openConnection(Callback<AMQPConnection> callback) {
        current().openConnection(callback);
    }

    public void openSession(Session session) {
        current().openSession(session);
    }

    AMQPConnection.Listener listener() {
        return connection.listener();
    }

    DispatchQueue queue() {
        return connection.queue();
    }

    void send(AMQPTransportFrame frame) {
        current().send(frame);
    }

    Session createSessionImpl(AMQPSessionOptions sessionOptions, Callback<AMQPSession> cb) {
        int max = Integer.MAX_VALUE;
        if( remoteOpen!=null && remoteOpen.getChannelMax()!=null ) {
            max = remoteOpen.getChannelMax();
        }
        for (int channel = 0; channel < max; channel++) {
            Session session = sessions.get(channel);
            if( session == null ) {
                session = new Session(this, channel, sessionOptions, cb);
                sessions.put(channel, session);
                return session;
            }
        }
        return null;
    }
    
    void releaseChannel(int channel) {
        sessions.remove(channel);
    }
    
    Session session(int channel) throws Exception {
        Session session = sessions.get(channel);
        if( session == null ) {
            die("Invalid channel: %d", channel);
        }
        return session;
    }

    public AMQPSession[] sessions() {
        queue().assertExecuting();
        ArrayList<AMQPSession> rc = new ArrayList<AMQPSession>();
        for( Session s: sessions.values()) {
            if( s!=null && s.begunCallback==null ) {
                rc.add(s);
            }
        }
        return rc.toArray(new AMQPSession[rc.size()]);
    }

    public int getMaxFrameSize() {
        int rc = connection.options.getMaxFrameSize();
        if( remoteOpen != null && remoteOpen.getMaxFrameSize()!=null ) {
            rc = (int) Math.min(rc, remoteOpen.getMaxFrameSize());
        }
        return rc;
    }

    /////////////////////////////////////////////////////////////////////
    // Connection States
    /////////////////////////////////////////////////////////////////////

    abstract class BaseState extends StateMachine.State {

        public void openConnection(Callback<AMQPConnection> cb) {
            cb.onFailure(invalidState());
        }

        public void onClose(Close error, Callback<Void> cb) {
            if(!become(new WaitingForClose(cb))) {
                current().onClose(error, cb);
            }
        }

        public void send(AMQPFrame frame) {
            send(frame, null);
        }

        public void send(AMQPFrame frame, Callback<Void> cb) {
            if( !outbound.isEmpty() || !connection.transport().offer(frame)) {
                outbound.add(new SendRequest(frame, cb));
            } else {
                connection.logger().trace("sent: %s", frame);
                if( cb!=null ) {
                    cb.onSuccess(null);
                }
            }
        }

        protected void onTransportRefill() {
            while( !outbound.isEmpty() ) {
                SendRequest request = outbound.peekFirst();
                if( connection.transport().offer(request.frame) ) {
                    outbound.removeFirst();
                    if( request.cb !=null ) {
                        request.cb.onSuccess(null);
                    }
                }
            }
        }

        protected void onAMQPFrame(AMQPFrame frame) throws Exception {
            connection.logger().trace("received: %s", frame);
            if( frame instanceof AMQPTransportFrame) {
                onAMQPTransportFrame((AMQPTransportFrame) frame);
            } else if( frame instanceof AMQPHeaderFrame) {
                onAMQPProtocolHeader((AMQPHeaderFrame)frame);
            } else {
                die("Unhandled frame type: " + frame);
            }
        }

        protected void onAMQPProtocolHeader(AMQPHeaderFrame frame) throws Exception {
            die("AMQP protocol header not exepceted");
        }

        protected void onAMQPTransportFrame(AMQPTransportFrame frame) throws Exception {
            Frame performative = frame.getPerformative();
            if(performative!=null) {
                onPerformative(frame, performative);
            }
        }

        protected void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            die("Unhandled performative frame: " + performative);
        }

        protected void onProcessingError(Throwable error) {
            error.printStackTrace();
            connection.options.getLogger().debug("Internal error: "+error);
            connection.close("Internal error: "+error.getMessage(), null);
        }

        protected void onTransportError(Throwable error) {
            error.printStackTrace();
            connection.options.getLogger().debug("Transport failed: "+error);
            become(new WaitingForDisconnect(null));
            current().onTransportError(error);
        }

        public AMQPSession beginSession(AMQPSessionOptions sessionOptions, Callback<AMQPSession> cb) {
            cb.onFailure(invalidState());
            return null;
        }

        public void openSession(Session session) {
            fail(session.begunCallback, invalidState());
        }

    }

    abstract class WaitingWithTimeoutState extends BaseState {

        abstract protected long waitStateTimeoutMs();
        abstract protected void onWaitStateTimeout();

        public void onActivate() {
            // We should transition out of this state
            connection.queue().executeAfter(waitStateTimeoutMs(), TimeUnit.MILLISECONDS, new Runnable() {
                public void run() {
                    if( WaitingWithTimeoutState.this == current() ) {
                        onWaitStateTimeout();
                    }
                }
            });
        }
    }

    class Created extends BaseState {
        public void openConnection(Callback<AMQPConnection> cb) {
            become(new WaitingForProtocolHeader(cb));
        }
    }

    class WaitingForProtocolHeader extends WaitingWithTimeoutState {

        private final Callback<AMQPConnection> cb;
        public WaitingForProtocolHeader(Callback<AMQPConnection> cb) {
            this.cb = cb;
        }

        @Override
        protected long waitStateTimeoutMs() {
            return 30 * 1000;
        }

        @Override
        protected void onWaitStateTimeout() {
            onTransportError(new IOException("Timed out waiting for protocol header."));
        }

        @Override
        public void onActivate() {
            if(!connection.options.isServer()) {
                // Clients send the AMQPProtocolHeader first...
                AMQPHeaderFrame header = new AMQPHeaderFrame();
                send(header);
            }
            super.onActivate();
        }

        @Override
        protected void onAMQPProtocolHeader(AMQPHeaderFrame frame) {
            if( connection.options.isServer() ) {
                // Servers send the AMQPProtocolHeader 2nd...
                AMQPHeaderFrame header = new AMQPHeaderFrame();
                send(header);
            }
            become(new WaitingForOpenFrame(cb));
        }
    }

    class WaitingForOpenFrame extends BaseState {
        private final Callback<AMQPConnection> cb;
        public WaitingForOpenFrame(Callback<AMQPConnection> cb) {
            this.cb = cb;
        }

        @Override
        public void onActivate() {
            // Clients send the open first..
            if( !connection.options.isServer() ) {
                Open open = new Open();
                open.setChannelMax(65535);
                open.setContainerID(connection.options.getContainerId());
                open.setHostname(connection.options.getHostName());
                open.setIdleTimeout(connection.options.getIdleTimeout());
                open.setDesiredCapabilities(connection.options.getDesiredCapabilities());
                open.setIncomingLocales(connection.options.getIncomingLocales());
                open.setMaxFrameSize((long)connection.options.getMaxFrameSize());
                open.setOfferedCapabilities(connection.options.getOfferedCapabilities());
                open.setOutgoingLocales(connection.options.getOutgoingLocales());
                open.setProperties(new MapEntries(connection.options.getProperties()));
                send(frame(open));
            }
        }

        @Override
        protected void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            Class kind = performative.getClass();
            if( kind == Open.class ) {
                remoteOpen = (Open) performative;

                // Invalid idle timeout request...
                long remoteIdleTimeout = -1;
                if( remoteOpen.getIdleTimeout()!=null ) {
                    remoteIdleTimeout = remoteOpen.getIdleTimeout().longValue();
                }
                if( remoteIdleTimeout > 0 && remoteIdleTimeout < 200 ) {
                    die("Requested idle timeout is not supported: "+remoteIdleTimeout);
                }

                if( !connection.options.isServer() ){
                    become(new Opened());
                } else {

                    // Server responses to the send frame..
                    Open open = new Open();
                    open.setChannelMax(Integer.MAX_VALUE);
                    open.setContainerID(connection.options.getContainerId());
                    open.setHostname(connection.options.getHostName());
                    open.setIdleTimeout(connection.options.getIdleTimeout());
                    open.setDesiredCapabilities(connection.options.getDesiredCapabilities());
                    open.setIncomingLocales(connection.options.getIncomingLocales());
                    open.setMaxFrameSize((long)connection.options.getMaxFrameSize());
                    open.setOfferedCapabilities(connection.options.getOfferedCapabilities());
                    open.setOutgoingLocales(connection.options.getOutgoingLocales());
                    open.setProperties(MapEntries.create(connection.options.getProperties()));

                    // Give the listener a chance to customize the open response..
                    listener().onOpen(remoteOpen, open, new Callback<Open>() {
                        public void onSuccess(Open open) {
                            if( open!=null ) {
                                send(frame(open));
                                become(new Opened());
                            } else {
                                onFailure(new AMQPException("Connection refused"));
                            }
                        }

                        public void onFailure(Throwable value) {
                            current().onProcessingError(value);
                        }
                    });

                }
            } else {
                super.onPerformative(frame, performative);
            }
        }
    }

    class Opened extends BaseState {

        HeartBeatMonitor heartBeatMonitor;

        public void onActivate() {
            long remoteIdleTimeout = -1;
            if( remoteOpen.getIdleTimeout()!=null ) {
                remoteIdleTimeout = remoteOpen.getIdleTimeout().longValue();
            }
            if( connection.options.getIdleTimeout() > 0 ||
                    remoteIdleTimeout > 0 ) {
                heartBeatMonitor = new HeartBeatMonitor();
                heartBeatMonitor.setTransport(connection.transport());
                if( connection.options.getIdleTimeout() > 0 ) {
                    heartBeatMonitor.setReadInterval(connection.options.getIdleTimeout());
                    heartBeatMonitor.setOnDead(new Task() {
                        public void run() {
                            onTransportError(new IOException("Peer heartbeat timeout."));
                        }
                    });
                }
                if( remoteIdleTimeout > 0 ) {
                    heartBeatMonitor.setWriteInterval((long)(remoteIdleTimeout)/2);
                    heartBeatMonitor.setOnKeepAlive(new Task() {
                        public void run() {
                            send(frame(new NoPerformative()));
                        }
                    });
                }
                heartBeatMonitor.start();
            }
        }

        @Override
        public void onDeactivate() {
            if(heartBeatMonitor!=null) {
                heartBeatMonitor.stop();
                heartBeatMonitor = null;
            }
        }

        public void openSession(Session session) {
            Begin begin = new Begin();
            begin.setNextOutgoingID(session.nextOutgoingId);
            begin.setOutgoingWindow(session.outgoingWindow);
            begin.setIncomingWindow(session.incomingWindow);
            begin.setHandleMax(connection.options.getSessionHandleMax());
            // response.setProperties(...);
            // response.setDesiredCapabilities(...);
            // response.setOfferedCapabilities(..);
            send(frame(session.outgoingChannel, begin));
        }

        @Override
        public void onClose(Close close, Callback<Void> cb) {
            send(frame(close));
            super.onClose(close, cb);
        }

        public void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            Class<? extends Frame> kind = performative.getClass();
            if( kind == Begin.class ) {
                Begin begin = (Begin) performative;
                Session session;
                if( begin.getRemoteChannel() == null ) {
                    AMQPSessionOptions sessionOptions = connection.listener().onBegin(begin);
                    session = createSessionImpl(sessionOptions, null);
                    if( session == null ) {
                        die("Too many open sessions");
                    }
                    Begin response = new Begin();
                    response.setRemoteChannel(frame.getChannel());
                    response.setNextOutgoingID(session.nextOutgoingId);
                    response.setOutgoingWindow(session.outgoingWindow);
                    response.setIncomingWindow(session.incomingWindow);
                    response.setHandleMax(connection.options.getSessionHandleMax());
                    send(frame(session.outgoingChannel, response));
                    // response.setProperties(...);
                    // response.setDesiredCapabilities(...);
                    // response.setOfferedCapabilities(..);
                } else {
                    session = session(begin.getRemoteChannel().intValue());
                }
                // Transition the session to the open state..
                session.current().begun(frame.getChannel(), begin);

            } else if( kind == Close.class ) {
                Close close = (Close) performative;
                if( close.getError()!=null ) {
                    listener().onException(new AMQPException(close.getError().getDescription()));
                } else {
                    listener().onClose();
                }
                send(frame(new Close()));
                become(new WaitingForDisconnect(null));
            } else if( kind == NoPerformative.class ) {
                // Just a heartbeat..
            } else {
                Session session = session(frame.getChannel());
                session.onPerformative(frame, performative);
            }
        }

    }

    class WaitingForClose extends WaitingForDisconnect {

        boolean closed = false;

        public WaitingForClose(Callback<Void> cb) {
            super(cb);
        }

        @Override
        protected void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            Class<? extends Frame> kind = performative.getClass();
            if( !closed && kind == Close.class ) {
                closed = true;
                stopTransport();
            } else {
                super.onPerformative(frame, performative);
            }
        }

    }

    class WaitingForDisconnect extends WaitingWithTimeoutState {
        private final LinkedList<Callback<Void>> cbs = new LinkedList<Callback<Void>>();
        boolean stoppingTransport = false;

        public WaitingForDisconnect(Callback<Void> cb) {
            if( cb!=null ) {
                this.cbs.add(cb);
            }
        }

        protected long waitStateTimeoutMs() {
            return 30 * 1000;
        }

        protected void onWaitStateTimeout() {
            connection.options.getLogger().debug("Timed out waiting for peer close.");
            stopTransport();
        }
        
        public void onClose(Callback<Void> cb) {
            if( cb!=null ) {
                this.cbs.add(cb);
            }
        }

        protected void onTransportError(Throwable error) {
            stopTransport();
        }

        protected void stopTransport() {
            if( stoppingTransport )
                return;
            stoppingTransport = true;
            connection.transport().stop(new Task(){
                public void run() {
                    if( active() ) {
                        become(new Closed());
                    }
                }
            });
        }

        public void onDeactivate() {
            for(Callback<Void> cb: cbs) {
                cb.onSuccess(null);
            }
        }
    }

    class Closed extends BaseState {

        public void onClose(Callback<Void> cb) {
            cb.onSuccess(null);
        }

        protected void onTransportError(Throwable error) {
            throw invalidState();
        }
    }

}
