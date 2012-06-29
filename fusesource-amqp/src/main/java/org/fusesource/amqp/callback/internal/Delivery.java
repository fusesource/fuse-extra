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

import org.fusesource.amqp.types.MessageSupport;
import org.fusesource.amqp.callback.AMQPDelivery;
import org.fusesource.amqp.callback.Callback;
import org.fusesource.amqp.types.*;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.BufferOutputStream;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract class Delivery implements AMQPDelivery {

    final LinkedList<TransferNode> transfers = new LinkedList<TransferNode>();
    private long messageFormat = -1;
    boolean settled = false;
    boolean more = true;

    static class TransferNode {
        private final Long id;
        private Buffer tag;
        private final Buffer payload;

        TransferNode(Long id, Buffer tag, Buffer payload) {
            this.id = id;
            this.tag = tag;
            this.payload = payload;
        }
    } 

    public int messagePayloads() {
        return transfers.size();
    }
    public Buffer messagePayload(int i) {
        return transfers.get(i).payload;
    }

    public long format() {
        return messageFormat;
    }

    public boolean isSettled() {
        return settled;
    }

    public Buffer payload() {
        if( transfers.size() == 1 ) {
            return transfers.getFirst().payload;
        } else {
            long size = payloadSize();
            if( size > Integer.MAX_VALUE ) {
                throw new UnsupportedOperationException("Message is too large");
            }
            Buffer buffer = new Buffer((int)size);
            BufferOutputStream out = buffer.out();
            for (TransferNode node : transfers) {
                try {
                    node.payload.writeTo(out);
                } catch (IOException notExpected) {
                    throw new RuntimeException(notExpected);
                }
            }
            return buffer;
        }
    }

    public Envelope getMessage() throws Exception {
        return MessageSupport.decodeEnvelope(payload());
    }

    public long payloadSize() {
        long rc = 0;
        for (TransferNode node : transfers) {
            rc += node.payload.length();
        }
        return rc;
    }
    
    public void append(Transfer transfer, Buffer payload) {
        if( !more ) {
            throw new IllegalStateException("No more transfer frames expected.");
        }
        if( transfer.getSettled()!=null ) {
            settled |= transfer.getSettled().booleanValue();
        }
        if( transfer.getMore() ) {
            more = transfer.getMore();
        } else {
            more = false;
        }
        if( transfer.getMessageFormat() != null ) {
            messageFormat = transfer.getMessageFormat();
        }

        transfers.add(new TransferNode(transfer.getDeliveryID(), transfer.getDeliveryTag(), payload));
    }

    protected void forechDisposition(Callback<Disposition> callback) {
        long last=0;
        boolean senderSettled=false;
        Disposition disposition = null;
        if( !senderSettled ) {
            for( TransferNode t: transfers) {
                if( disposition == null ) {
                    disposition = new Disposition();
                    disposition.setFirst(t.id);

                } else {
                    if( last+1 == t.id) {
                        disposition.setLast(t.id);
                    } else {
                        callback.onSuccess(disposition);
                        disposition = null;
                    }
                }
                last = t.id;
            }
            if(disposition!=null) {
                callback.onSuccess(disposition);
            }
        }
    }
        
}
