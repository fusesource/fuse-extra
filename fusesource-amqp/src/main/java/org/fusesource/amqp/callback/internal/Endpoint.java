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

import org.fusesource.amqp.*;
import org.fusesource.amqp.callback.*;
import org.fusesource.amqp.codec.AMQPTransportFrame;
import org.fusesource.amqp.types.*;
import org.fusesource.amqp.types.MessageSupport;
import org.fusesource.amqp.types.Error;
import org.fusesource.amqp.types.Envelope;
import org.fusesource.hawtbuf.AbstractVarIntSupport;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtdispatch.Dispatch;
import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.Task;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import static org.fusesource.amqp.callback.internal.Support.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class Endpoint extends StateMachine<Endpoint.BaseState> implements AMQPEndpoint {

    long deliveryCount;
    long linkCredit;
    long available;
    boolean drain;
    DispatchQueue queue;

    public Endpoint() {
        if( queue == null ) {
            queue = Dispatch.getCurrentQueue();
            if( queue == null ) {
                queue = Dispatch.createQueue();
            }
        }
    }

    abstract protected AMQPEndpointOptions options();

    public DispatchQueue queue() {
        return queue;
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
    
    void onAttachFrame(final Session source, final Attach attach) throws Exception {
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
        final ArrayList<Runnable> tmp = new ArrayList<Runnable>(deferred);
        deferred.clear();
        for (Runnable runnable : tmp) {
            runnable.run();
        }
    }

    public String name() {
        if( options().getName() == null ) {
            options().setName(UUID.randomUUID().toString());
        }
        return options().getName();
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

        public void send(Envelope message, Callback<AMQPDeliveryState> callback) {
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

        public void attach(final Session session, final Callback<Void> callback) {
            final Task task = new Task() {
                public void run() {
                    Long handle = session.assignHandle(Endpoint.this);
                    if (handle == null) {
                        fail(callback, new AMQPException("Exceed maximum handles"));
                    } else {
                        become(new Attaching(session, handle, callback));
                    }
                }
            };
            if( queue != session.queue() ) {
                queue = session.queue();
                queue.execute(task);
            } else {
                task.run();
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
            options().getListener().onClosed(senderClosed, remoteDetach.getError());
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
        
        public void send(Envelope message, Callback<AMQPDeliveryState> callback) {
            long delivery_id = session.nextOutgoingId++;


            Transfer transfer = new Transfer();
            transfer.setHandle(handle);
            transfer.setDeliveryID(delivery_id);
            
            transfer.setMessageFormat(0L);
            Callback<AMQPDeliveryState> sendSettledCallback = null;
            if( options().getSenderSettleMode().getValue().equals( SenderSettleMode.SETTLED.getValue() ) ) {
                transfer.setSettled(true);
                sendSettledCallback = callback;
            } else {
                Buffer tag = toTag(delivery_id);
                transfer.setDeliveryTag(tag);
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
//            success(callback, new Accepted());
            
            // the encoded message might be too big to fit in 1 transfer frame..
            while( encodedMessage.length > 0 ) {
                if( encodedMessage.length <= maxPayload ) {
                    transfer.setMore(false);
                    frame.setPayload(encodedMessage.buffer());
                    encodedMessage.length = 0;
                    overflowFrames.add(new OverflowEntry(frame, sendSettledCallback));
                } else {
                    Buffer slice = encodedMessage.buffer();
                    slice.length = maxPayload;
                    transfer.setMore(true);
                    frame.setPayload(slice);
                    encodedMessage.moveHead(maxPayload);
                    overflowFrames.add(new OverflowEntry(frame, null));

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
                    OverflowEntry entry = overflowFrames.poll();
                    linkCredit--;
                    available++;
                    deliveryCount++;
                    session.remoteIncomingWindow -= 1;
                    session.send(entry.frame.getPerformative(), entry.frame.getPayload());
                    if( entry.callback !=null ) {
                        entry.callback.onSuccess(new Accepted());
                    }
                }
                if( overflowFrames.isEmpty()) {
                    options().getListener().onTransfer();
                }
            }
        }
        
    }

    static class OverflowEntry {
        final AMQPTransportFrame frame;
        final Callback<AMQPDeliveryState> callback;

        OverflowEntry(AMQPTransportFrame frame, Callback<AMQPDeliveryState> callback) {
            this.frame = frame;
            this.callback = callback;
        }

    }

    LinkedList<OverflowEntry> overflowFrames = new LinkedList<OverflowEntry>();

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
        private final Callback<AMQPDeliveryState> callback;

        public UnsettledContext(Transfer transfer, Callback<AMQPDeliveryState> callback){
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
        Attach rc = new Attach();
        rc.setHandle(handle);
        rc.setName(name());
        rc.setRole(options().getClass() == AMQPReceiverOptions.class);
        rc.setRcvSettleMode(options().getReceiverSettleMode().getValue());
        rc.setSndSettleMode(options().getSenderSettleMode().getValue());
        rc.setMaxMessageSize(new BigInteger("" + options().getMaxMessageSize(), 10));
        rc.setTarget(options().getTarget());
        rc.setSource(options().getSource());
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
