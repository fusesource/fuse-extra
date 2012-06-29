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
import org.fusesource.hawtbuf.Buffer;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fusesource.amqp.codec.TestSupport.encodeDecode;
import static org.fusesource.amqp.types.MessageSupport.envelope;
import static org.fusesource.amqp.types.MessageSupport.message;
import static org.fusesource.hawtbuf.Buffer.ascii;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class MessageTest {

    @Test
    public void testEncodeDecodeEmptyMessage() throws Exception {
        Envelope in = envelope();
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testEncodeDecodeSimpleDataMessage() throws Exception {
        Message message = message(ascii("Hello world!").buffer());
        Envelope in = envelope(message);
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testEncodeDecodeMultipartDataMessage() throws Exception {
        Message msg = message(Buffer.ascii("Hello").buffer(),
                ascii("World").buffer(),
                ascii("!").buffer());
        Envelope in = envelope(msg);
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testEncodeDecodeSimpleValueMessage() throws Exception {
        Message msg = message(new AMQPString("Hello World!"));
        Envelope in = envelope(msg);
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testEncodeDecodeSimpleSequenceMessage() throws Exception {
        List<AMQPType> list = new ArrayList<AMQPType>();
        list.add(new AMQPString("Hello"));
        list.add(new AMQPString("World"));
        list.add(new AMQPChar('!'));
        Message msg = message(new AMQPSequence(list));
        Envelope in = envelope(msg);
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    @Test
    public void testEncodeDecodeSequenceMessageWithList() throws Exception {
        List<AMQPSequence> list = new ArrayList<AMQPSequence>();

        for ( int i = 0; i < 10; i++ ) {
            List<AMQPInt> inner = new ArrayList<AMQPInt>();
            list.add(new AMQPSequence(inner));
            for ( int j = 0; j < 10; j++ ) {
                inner.add(new AMQPInt(i + j));
            }
        }

        Message msg = message(list);
        Envelope in = envelope(msg);
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());

    }

    @Test
    public void testEncodeDecodeLessSimpleMessage() throws Exception {
        Envelope in = getMessage();
        Envelope out = encodeDecode(in);
        assertEquals(in.toString(), out.toString());
    }

    private static Envelope getMessage() {

        ArrayList<AMQPString> payload1 = new ArrayList<AMQPString>();
        ArrayList<AMQPString> payload2 = new ArrayList<AMQPString>();

        for ( int i = 0; i < 10; i++ ) {
            payload1.add(new AMQPString("payload item " + i));
            payload2.add(new AMQPString("and payload item " + (i + 10)));
        }

        AMQPSequence body = new AMQPSequence();
        body.setValue(new ArrayList());
        body.getValue().addAll(payload1);
        body.getValue().addAll(payload2);

        Message msg = message(
                body,
                new Properties(null,
                        ascii("foo").buffer(),
                        new AMQPString("nowhere"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        new Date()));

        Envelope in = envelope(msg);
        in.setDeliveryAnnotations(new DeliveryAnnotations());
        in.getDeliveryAnnotations().setValue(new MapEntries());
        in.getDeliveryAnnotations().getValue().add(new AMQPSymbol(Footer.CONSTRUCTOR.getBuffer()), new AMQPString("Hi!"));
        in.setHeader(new Header());
        in.getHeader().setDurable(true);
        in.getHeader().setDeliveryCount(0L);
        in.setFooter(new Footer());
        in.getFooter().setValue(new MapEntries());
        in.getFooter().getValue().add(new AMQPSymbol(Buffer.ascii("test").buffer()), new AMQPString("value"));
        return in;
    }


}
