package org.fusesource.amqp.internal;

import org.fusesource.amqp.Callback;
import org.fusesource.amqp.AMQPEndpoint;
import org.fusesource.amqp.AMQPSession;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.amqp.codec.interfaces.Frame;
import org.fusesource.amqp.codec.types.*;
import org.fusesource.amqp.codec.types.Error;
import org.fusesource.hawtbuf.Buffer;

import java.util.*;

import static org.fusesource.amqp.internal.Support.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class Session extends StateMachine<Session.BaseState> implements AMQPSession {

    boolean locallyInitiated;
    Callback<AMQPSession> begunCallback;

    // Outgoing control state
    int outgoingChannel;
    private final AMQPSessionOptions options;
    long nextOutgoingId;
    long outgoingWindow;
    long remoteIncomingWindow;

    // Incoming control state
    int incomingChannel;
    Long nextIncomingId;
    long incomingWindow;
    long remoteOutgoingWindow;
    
    private ConnectionState connection;
    long handleMax = Long.MAX_VALUE;

    final HashMap<Long, Endpoint> endpointsByHandle = new HashMap<Long, Endpoint>();
    final HashMap<Long, Endpoint> endpointsByRemoteHandle = new HashMap<Long, Endpoint>();

    
    Flow createFlow() {
        Flow flow = new Flow();
        flow.setNextIncomingID(nextIncomingId);
        flow.setIncomingWindow(incomingWindow);
        flow.setNextOutgoingID(nextOutgoingId);
        flow.setOutgoingWindow(outgoingWindow);
        return flow;
    }

    Long assignHandle(Endpoint endpoint) {
        for (long handle = 0; handle < handleMax; handle++) {
            Endpoint value = endpointsByHandle.get(handle);
            if( value == null ) {
                Long rc = handle;
                endpointsByHandle.put(rc, endpoint);
                return rc;
            }
        }
        return null;
    }

    void releaseHandles(Long handle, Long remoteHandle) {
        endpointsByHandle.remove(handle);
        endpointsByRemoteHandle.remove(remoteHandle);
    }

    Endpoint endpointByName(String name) {
        for (Endpoint entry : endpointsByHandle.values()) {
            if( name.equals(entry.name()) ) {
                return entry;
            }
        }
        return null;
    }

    Endpoint endpointByRemoteHandle(Long handle) throws Exception {
        Endpoint endpoint = endpointsByRemoteHandle.get(handle);
        if( endpoint == null ) {
            die("Invalid handle: %s", handle);
        }
        return endpoint;
    }

    public AMQPEndpoint[] endpoints() {
        ArrayList<AMQPEndpoint> rc = new ArrayList<AMQPEndpoint>();
        for( Endpoint s: endpointsByHandle.values()) {
            if( s.current().getClass() == Endpoint.Attached.class ) {
                rc.add(s);
            }
        }
        return rc.toArray(new AMQPEndpoint[rc.size()]);
    }
    
    public Session(ConnectionState connection, int outgoingChannel, AMQPSessionOptions options, Callback<AMQPSession> begunCallback) {
        this.options = options.copy();
        this.connection = connection;
        this.outgoingChannel = outgoingChannel;
        this.incomingWindow = options.incomingWindow;
        this.outgoingWindow = options.outgoingWindow;
        this.begunCallback = begunCallback;
        this.locallyInitiated = begunCallback!=null;
    }

    @Override
    protected BaseState init() {
        return new Created();
    }

    public void send(Frame frame) {
        current().send(frame);
    }
    public void send(Frame frame, Buffer payload) {
        current().send(frame, payload);
    }

    public int getMaxFrameSize() {
        return connection.getMaxFrameSize();
    }


    abstract class BaseState extends StateMachine.State {
        public void begun(int channel, Begin begin) throws Exception {
            throw invalidState();
        }

        public void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            throw invalidState();
        }

        public void close(Error error, Callback<Void> callback) {
            if( !become(new WaitingForEnd(callback)) ) {
                current().close(error, callback);
            }
        }

        public void send(Frame frame) {
            send(frame, AMQPTransportFrame.EMPTY);
        }

        public void send(Frame frame, Buffer payload) {
            connection.send(frame(outgoingChannel, frame, payload));
        }
    }

    class  Created extends BaseState {
        LinkedList<Runnable> deferred = new LinkedList<Runnable>();

        public void begun(int channel, Begin begin) throws Exception {
            if( begin.getNextOutgoingID() == null ) {
                die("begin next outgoing id not set");
            }
            if( begin.getOutgoingWindow() == null ) {
                die("begin next outgoing window not set");
            }
            if( begin.getIncomingWindow() == null ) {
                die("begin next incoming window not set");
            }
            if( begin.getHandleMax()!= null) {
                handleMax = begin.getHandleMax().longValue();
            }
            incomingChannel = channel;
            nextIncomingId = begin.getNextOutgoingID();
            remoteOutgoingWindow = begin.getOutgoingWindow();
            remoteIncomingWindow = begin.getIncomingWindow();
            outgoingWindow = remoteIncomingWindow;
            become(new Open());

        }

        @Override
        public void onDeactivate() {
            // Execute the deferred...
            for(Runnable r: deferred) {
                r.run();
            }
        }
    }

    class  Open extends BaseState {
        public void onActivate() {
            // Trigger the callbacks to let the world
            // know that this session is now open..
            Callback<AMQPSession> cb = begunCallback;
            begunCallback = null;

            if( cb != null ) {
                cb.onSuccess(Session.this);
            } else {
                connection.listener().onAccepted(Session.this);
            }
        }

        public void close(Error error, Callback<Void> callback) {
            send(new End(error));
            become(new WaitingForEnd(callback));
        }

        public void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
            Class<? extends Frame> kind = performative.getClass();
            if( kind == Transfer.class ) {
                Transfer transfer = (Transfer)performative;
                Endpoint endpoint = endpointByRemoteHandle(transfer.getHandle());
                if( endpoint!=null ) {
                    endpoint.onTransferFrame(Session.this, transfer, frame.getPayload());
                } else {
                    die("Invalid handle");
                }
            } else if( kind == Flow.class ) {
                Flow flow = (Flow)performative;
                if( flow.getHandle() != null ) {
                    Endpoint endpoint = endpointByRemoteHandle(flow.getHandle());
                    if( endpoint!=null ) {
                        endpoint.onFlowFrame(Session.this, flow);
                    } else {
                        die("Invalid handle");
                    }
                }
            } else if( kind == Attach.class ) {
                final Attach attach = (Attach)performative;
                Endpoint endpoint = endpointByName(attach.getName());
                if( endpoint != null ) {
                    endpointsByRemoteHandle.put(attach.getHandle(), endpoint);
                    endpoint.onAttachFrame(Session.this, attach);
                } else {
                    options.listener.onAttach(attach, new Callback<AMQPEndpoint>() {
                        public void onSuccess(AMQPEndpoint value) {
                            try {
                                Endpoint endpoint = (Endpoint) value;
                                endpointsByRemoteHandle.put(attach.getHandle(), endpoint);
                                endpoint.onAttachFrame(Session.this, attach);
                            } catch (Exception e) {
                                onFailure(e);
                            }
                        }

                        public void onFailure(Throwable value) {
                            value.printStackTrace();
                            throw new RuntimeException("Not Yet implemented..");
                        }
                    });
                }
            } else if( kind == Detach.class ) {
                Detach detach = (Detach)performative;
                Endpoint endpoint = endpointByRemoteHandle(detach.getHandle());
                if( endpoint!=null ) {
                    endpoint.onDetachFrame(Session.this, detach);
                } else {
                    die("Invalid handle");
                }
            } else if( kind == Disposition.class ) {
                Disposition disposition = (Disposition)performative;
                die("Not implemented");
            } else if( kind == End.class ) {
                End end = (End) performative;
                options.listener.onClose(end.getError());
                send(new End());
                become(new Closed());
            } else {
                die("Unexpected performative type: "+kind);
            }
        }

    }

    class WaitingForEnd extends BaseState {
        private final LinkedList<Callback<Void>> cbs = new LinkedList<Callback<Void>>();
        public WaitingForEnd(Callback<Void> cb) {
            if( cb!=null ) {
                this.cbs.add(cb);
            }
        }

        public void close(Error error, Callback<Void> cb) {
            if( cb!=null ) {
                this.cbs.add(cb);
            }
        }

        public void onDeactivate() {
            for(Callback<Void> cb: cbs) {
                cb.onSuccess(null);
            }
        }
    }

    class Closed extends BaseState {

        @Override
        public void onActivate() {
            connection.releaseChannel(outgoingChannel);
        }

        public void close(Error error, Callback<Void> cb) {
            cb.onSuccess(null);
        }
    }

    /////////////////////////////////////////////////////////////////////
    // Internal interface
    /////////////////////////////////////////////////////////////////////
    void onPerformative(AMQPTransportFrame frame, Frame performative) throws Exception {
        current().onPerformative(frame, performative);
    }


    /////////////////////////////////////////////////////////////////////
    // Public Session Interface
    /////////////////////////////////////////////////////////////////////
    public void close(Error error, Callback<Void> callback) {
        connection.queue().assertExecuting();
        current().close(error, callback);
    }

}
