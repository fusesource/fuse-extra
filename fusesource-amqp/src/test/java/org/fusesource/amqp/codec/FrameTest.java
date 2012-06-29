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

package org.fusesource.amqp.codec;

import org.fusesource.amqp.types.*;
import org.fusesource.amqp.types.AMQPString;
import org.fusesource.amqp.types.Begin;
import org.fusesource.amqp.types.Transfer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.DataByteArrayInputStream;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;
import org.junit.Test;
import static org.fusesource.amqp.types.MessageSupport.*;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class FrameTest {

    @Test
    public void testBeginFrame() throws Exception {
        Begin in = new Begin();
        in.setIncomingWindow(10L);
        in.setOutgoingWindow(10L);
        in.setNextOutgoingID(0L);

        Begin out = (Begin)TestSupport.encodeDecode(new AMQPTransportFrame(0, in)).getPerformative();
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testTransferFrame() throws Exception {
        Message message = message(new AMQPString("HelloWorld!"));
        Transfer transfer = new Transfer(0L, 0L, Buffer.ascii("0").buffer());
        Envelope Envelope = envelope(message);

        AMQPTransportFrame frame = new AMQPTransportFrame(0, transfer, MessageSupport.toBuffer(Envelope));

        AMQPTransportFrame outFrame = TestSupport.encodeDecode(frame);

        Transfer outTransfer = (Transfer)outFrame.getPerformative();

        System.out.printf("Transfer : %s\n", outTransfer);

        Envelope msg = MessageSupport.decodeEnvelope(outFrame.getPayload());

        System.out.printf("Msg : %s\n", msg);
        assertEquals(transfer.toString(), outTransfer.toString());
        assertEquals(Envelope.toString(), msg.toString());
    }

    @Test
    public void createFrameFromHeaderAndBody() throws Exception {
        Message message = message(new AMQPString("HelloWorld!"));
        Transfer transfer = new Transfer(0L, 0L, Buffer.ascii("0").buffer());
        Envelope Envelope = envelope(message);
        AMQPTransportFrame inFrame = new AMQPTransportFrame(0, transfer, MessageSupport.toBuffer(Envelope));

        DataByteArrayOutputStream out = new DataByteArrayOutputStream((int)inFrame.getSize());
        inFrame.write(out);
        Buffer buf = out.toBuffer();


        Buffer header = new Buffer(8);

        DataByteArrayInputStream in = new DataByteArrayInputStream(buf);
        in.read(header.data);
        Buffer body = new Buffer(in.available());
        in.read(body.data);

        AMQPTransportFrame outFrame = new AMQPTransportFrame(header, body);

        Transfer outTransfer = (Transfer)outFrame.getPerformative();
        Envelope msg = MessageSupport.decodeEnvelope(outFrame.getPayload());
        assertEquals(transfer.toString(), outTransfer.toString());
        assertEquals(Envelope.toString(), msg.toString());
    }
}
