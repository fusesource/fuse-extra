/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.fabric.apollo.amqp.internal;

import org.fusesource.fabric.apollo.amqp.*;
import org.fusesource.fabric.apollo.amqp.codec.api.AnnotatedMessage;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.DeliveryState;
import org.fusesource.fabric.apollo.amqp.codec.marshaller.MessageSupport;
import org.fusesource.fabric.apollo.amqp.codec.types.*;
import org.fusesource.fabric.apollo.amqp.codec.types.Error;
import org.fusesource.hawtbuf.AbstractVarIntSupport;
import org.fusesource.hawtbuf.Buffer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import static org.fusesource.fabric.apollo.amqp.internal.Support.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class Endpoint extends StateMachine<Endpoint.BaseState> implements AMQPEndpoint {

    AMQPEndpointOptions options;
    long deliveryCount;
    long linkCredit;
    long available;
    boolean drain;

    protected Endpoint(AMQPEndpointOptions options) {
        this.options = options.copy();
        if( this.options.name == null ) {
            this.options.name = UUID.randomUUID().toString();
        }
    }

    protected BaseState init() {
        return new Detached();
    }

    public AMQPSession getSession() {
        return current().getSession();
    }

    public  void attach(AMQPSession session, Callback<Void> callback) {
        current().attach((Session) session, callback);
    }
    
    public void detach(boolean closed, String error, Callback<Void> callback) {
        current().detach(closed, error, callback);
    }
    
    void onAttachFrame(Session source, Attach attach) throws Exception {
        current().onAttachFrame(source, attach);
    }

    void onDetachFrame(Session source, Detach detach) throws Exception {
        current().onDetachFrame(source, detach);
    }

    void onFlowFrame(Session source, Flow flow) throws Exception {
        current().onFlowFrame(source, flow);
    }

    public void onTransferFrame(Session source, Transfer transfer, Buffer payload) throws Exception {
        current().onTransferFrame(source, transfer, payload);
    }


    final public LinkedList<Runnable> deferred = new LinkedList<Runnable>();
    public void executeDeferred() {
        for (Runnable runnable : deferred) {
            runnable.run();
        }
    }

    public String name() {
        return options.name;
    }

    abstract class BaseState extends StateMachine.State {
        public void attach(final Session session, final Callback<Void> callback) {
            deferred.add(new Runnable() {
                public void run() {
                    current().attach(session, callback);
                }
            });
        }

        public void detach(final boolean closed, final String error, final Callback<Void> callback) {
            deferred.add(new Runnable() {
                public void run() {
                    current().detach(closed, error, callback);
                }
            });
        }

        public void sendFlow() {
            deferred.add(new Runnable() {
                public void run() {
                    current().sendFlow();
                }
            });
        }

        public void onAttachFrame(Session source, Attach attach) throws Exception {
            throw invalidState();
        }

        public void onDetachFrame(Session source, Detach remoteDetach) throws Exception {
            throw invalidState();
        }

        public void onFlowFrame(Session source, Flow flow) {
            processFlowFrame(source, flow);
        }

        public void onTransferFrame(Session source, Transfer transfer, Buffer payload) throws Exception {
            processTransferFrame(source, transfer, payload);
        }

        public Session getSession() {
            return null;
        }

        public void sendDisposition(Disposition disposition) {
            throw invalidState();
        }

        public void send(AnnotatedMessage message, Callback<DeliveryState> callback) {
            callback.onFailure(invalidState());
        }

        void pumpOverflow() {
        }
    }

    class Detached extends BaseState {

        public void onActivate() {
            executeDeferred();
        }

        public void detach(boolean closed, String error, Callback<Void> callback) {
            success(callback, null);
        }

        public void attach(Session session, Callback<Void> callback) {
            Long handle = session.assignHandle(Endpoint.this);
            if( handle == null ) {
                fail(callback, new AMQPException("Exceed maximum handles"));
            } else {
                become(new Attaching(session, handle, callback));
            }
        }

        public void onAttachFrame(Session source, Attach attach) throws Exception {
            Long handle = source.assignHandle(Endpoint.this);
            if( handle == null ) {
                die("Exceed maximum handles");
            } else {
                source.send(createAttachFrame(handle));
                become(new Attached(source, handle, attach));
            }
        }

        public void onFlowFrame(Session source, Flow flow) {
            throw invalidState();
        }

        public void onTransferFrame(Session source, Transfer transfer) throws Exception {
            throw invalidState();
        }

        public void sendFlow() {
            // drop these flow updates since we are detached..
        }

    }

    class Attaching extends BaseState {
        private final Session session;
        private final Long handle;
        private final Callback<Void> callback;

        public Attaching(Session session, Long handle, Callback<Void> callback) {
            this.session = session;
            this.handle = handle;
            this.callback = callback;
        }

        @Override
        public void onActivate() {
            session.send(createAttachFrame(handle));
        }

        public void onAttachFrame(Session source, Attach remoteAttach) {
            become(new Attached(this.session, handle, remoteAttach));
        }

        @Override
        public void onDeactivate() {
            callback.onSuccess(null);
        }

        @Override
        public Session getSession() {
            return session;
        }
    }

    class Attached extends BaseState {
        private final Session session;
        private final Long handle;
        private final Attach remoteAttach;

        public Attached(Session session, Long handle, Attach remoteAttach) {
            this.session = session;
            this.handle = handle;
            this.remoteAttach = remoteAttach;
        }

        public void onActivate() {
            executeDeferred();
            if( active() ) {
                sendFlow();
            }
        }

        public void sendFlow() {
            Flow flow = session.createFlow();
            flow.setHandle(handle);
            flow.setDeliveryCount(deliveryCount);
            flow.setLinkCredit(linkCredit);
            flow.setAvailable(available);
            flow.setDrain(drain);
            session.send(flow);
        }

        public void attach(final Session next, final Callback<Void> callback) {
            // detach, and then attach..
            detach(false, null, new Callback<Void>() {
                public void onSuccess(Void value) {
                    current().attach(next, callback);
                }
                public void onFailure(Throwable value) {
                    callback.onFailure(value);
                }
            });
        }

        @Override
        public void detach(boolean closed, String error, Callback<Void> callback) {
            Detach rc = new Detach();
            rc.setHandle(handle);
            rc.setClosed(closed);
            if( error != null ) {
                rc.setError(new Error(null, error));
            }
            session.send(rc);
            become(new Dettaching(session, handle, remoteAttach, callback));
        }

        @Override
        public void onDetachFrame(Session source, Detach remoteDetach) throws Exception {
            boolean senderClosed = false;
            if( remoteDetach.getClosed()!=null ) {
                senderClosed = remoteDetach.getClosed();
            }
            options.listener.onClosed(senderClosed, remoteDetach.getError());
            Detach rc = new Detach();
            if( senderClosed ) {
                rc.setClosed(true);
            }
            rc.setHandle(handle);
            session.send(rc);
            session.releaseHandles(handle, remoteAttach.getHandle());
            become(new Detached());
        }


        @Override
        public Session getSession() {
            return session;
        }
        
        public void sendDisposition(Disposition disposition) {
            session.send(disposition);
        }
        
        public void send(AnnotatedMessage message, Callback<DeliveryState> callback) {
            long delivery_id = session.nextOutgoingId++;

            Buffer tag = toTag(delivery_id);

            Transfer transfer = new Transfer();
            transfer.setHandle(handle);
            transfer.setDeliveryID(delivery_id);
            transfer.setDeliveryTag(tag);
            transfer.setMessageFormat(0L);
            if( options.senderSettleMode.getValue().equals( SenderSettleMode.SETTLED.getValue() ) ) {
                transfer.setSettled(true);
                success(callback, new Accepted());
            } else {
                unsettled.put(tag, new UnsettledContext(transfer, callback));
            }
            
            transfer.setMore(false);
            AMQPTransportFrame frame = frame(session.outgoingChannel, transfer);
            int maxPayload = session.getMaxFrameSize() - frame.getFrameSize();

            Buffer encodedMessage;
            try {
                encodedMessage = MessageSupport.toBuffer(message);
            } catch (Exception e) {
                fail(callback, e);
                return;
            }

            // the encoded message might be too big to fit in 1 transfer frame..
            while( encodedMessage.length > 0 ) {
                if( encodedMessage.length <= maxPayload ) {
                    transfer.setMore(false);
                    frame.setPayload(encodedMessage.buffer());
                    encodedMessage.length = 0;
                    overflowFrames.add(frame);
                } else {
                    Buffer slice = encodedMessage.buffer();
                    slice.length = maxPayload;
                    transfer.setMore(true);
                    frame.setPayload(slice);
                    encodedMessage.moveHead(maxPayload);
                    overflowFrames.add(frame);

                    // Setup the next transfer frame...
                    transfer = new Transfer();
                    transfer.setHandle(handle);
                    transfer.setMore(false);
                    frame = frame(0, transfer);
                    maxPayload = session.getMaxFrameSize() - frame.getFrameSize();
                }
            }
            pumpOverflow();
        }

        void pumpOverflow() {
            if( !overflowFrames.isEmpty()) {
                while( linkCredit > 0 && !overflowFrames.isEmpty()) {
                    AMQPTransportFrame frame = overflowFrames.poll();
                    linkCredit--;
                    available++;
                    deliveryCount++;
                    session.remoteIncomingWindow -= 1;
                    session.send(frame.getPerformative(), frame.getPayload());
                }
                if( overflowFrames.isEmpty()) {
                    options.listener.onTransfer();
                }
            }
        }
        
    }

    LinkedList<AMQPTransportFrame> overflowFrames = new LinkedList<AMQPTransportFrame>();

    private Buffer toTag(long delivery_id) {
        Buffer tag  = null;
        try {
            tag = new Buffer(AbstractVarIntSupport.computeVarLongSize(delivery_id));
            tag.bigEndianEditor().writeVarLong(delivery_id);
            tag.offset = 0;
        } catch (IOException e) {
            throw new RuntimeException(e); // not expected
        }
        return tag;
    }

    static class UnsettledContext {
        private final Transfer transfer;
        private final Callback<DeliveryState> callback;

        public UnsettledContext(Transfer transfer, Callback<DeliveryState> callback){
            this.transfer = transfer;
            this.callback = callback;
        }
    }

    public HashMap<Buffer, UnsettledContext> unsettled = new HashMap<Buffer, UnsettledContext>();

    class Dettaching extends BaseState {
        private final Session session;
        private final Long handle;
        private final Attach remoteAttach;
        private final Callback<Void> callback;

        public Dettaching(Session session, Long handle, Attach remoteAttach, Callback<Void> callback) {
            this.session = session;
            this.handle = handle;
            this.remoteAttach = remoteAttach;
            this.callback = callback;
        }

        public void onDetachFrame(Session source, Detach remoteDetach) {
            session.releaseHandles(handle, remoteAttach.getHandle());
            become(new Detached());
        }

        @Override
        public void onDeactivate() {
            callback.onSuccess(null);
        }

        @Override
        public Session getSession() {
            return session;
        }
    }

    protected Attach createAttachFrame(Long handle) {
//        String name = ;
//        public Role role = Role.SENDER;
//        public SenderSettleMode senderSettleMode = SenderSettleMode.UNSETTLED;
//        public ReceiverSettleMode receiverSettleMode = ;
//
//        public AMQPType source;
//        public AMQPType target;
//
//        public Map unsettled;
//        public Boolean incompleteUnsettled;
//
//        public Map properties;
//        public Long initialDeliveryCount;
//        public BigInteger maxMessageSize;
//
//        public AMQPSymbol[] desiredCapabilities;
//        public AMQPSymbol[] offeredCapabilities;

        Attach rc = new Attach();
        rc.setHandle(handle);
        rc.setName(options.name);
        rc.setRole(options.getClass() == AMQPSenderOptions.class);
        rc.setRcvSettleMode(options.receiverSettleMode.getValue());
        rc.setSndSettleMode(options.senderSettleMode.getValue());
        rc.setMaxMessageSize(new BigInteger("" + options.maxMessageSize, 10));
        rc.setTarget(options.target);
        rc.setSource(options.source);
//        rc.setInitialDeliveryCount(initialDeliveryCount);
//        rc.setIncompleteUnsettled(incompleteUnsettled);
//        rc.setUnsettled(unsettled);
//        rc.setProperties(null);
//        rc.setDesiredCapabilities(desiredCapabilities);
//        rc.setOfferedCapabilities(offeredCapabilities);
        return rc;
    }

    abstract protected void processFlowFrame(Session source, Flow flow);

    protected void processTransferFrame(Session source, Transfer transfer, Buffer payload) throws Exception {
        die("This endpoint does not accept Transfer frames");
    }

}
