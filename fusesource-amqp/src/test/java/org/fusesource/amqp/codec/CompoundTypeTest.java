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
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.fusesource.amqp.codec.TestSupport.writeRead;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class CompoundTypeTest {

    @Test
    public void testList8() throws Exception {
        List in = new ArrayList();
        in.add(new AMQPByte((byte) 0x3));
        in.add(new AMQPShort((short) 5));
        in.add(new AMQPInt(10));
        in.add(new AMQPString("hi"));
        List out = writeRead(new AMQPList(in)).getValue();
        assertEquals(in, out);
    }

    @Test
    public void testList32() throws Exception {
        List in = new ArrayList();

        for ( int i = 0; i < 128; i++ ) {
            in.add(new AMQPByte((byte) i));
        }
        for ( int i = 0; i < 128; i++ ) {
            in.add(new AMQPInt((int) i));
        }
        for ( int i = 0; i < 128; i++ ) {
            in.add(new AMQPLong((long) i));
        }
        for ( int i = 0; i < 128; i++ ) {
            in.add(new AMQPULong(new BigInteger("" + i)));
        }
        for ( int i = 0; i < 128; i++ ) {
            in.add(new AMQPString("String number " + i));
        }

        List out = writeRead(new AMQPList(in)).getValue();

        assertEquals(in, out);
    }

    @Test
    public void testMap8() throws Exception {
        MapEntries in = new MapEntries();
        in.add(new AMQPString("key"), new AMQPString("value"));
        in.add(new AMQPString("int"), new AMQPInt(23));
        MapEntries out = writeRead(new AMQPMap(in)).getValue();
        assertEquals(in, out);
    }

    @Test
    public void testMap32() throws Exception {
        MapEntries in = new MapEntries();
        for ( int i = 0; i < 2048; i++ ) {
            in.add(new AMQPString("key" + i), new AMQPString("value" + i));
        }
        MapEntries out = writeRead(new AMQPMap(in)).getValue();
        assertEquals(in, out);
    }

    @Test
    public void testArray8() throws Exception {
        AMQPLong in[] = new AMQPLong[]{new AMQPLong(0L), new AMQPLong(1L), new AMQPLong(8192L)};
        AMQPLong out[] = (AMQPLong[]) writeRead(new AMQPArray(in)).getValue();
        assertArrayEquals(in, out);
    }

    @Test
    public void testArray32() throws Exception {
        AMQPString in[] = new AMQPString[512];
        for ( int i = 0; i < in.length; i++ ) {
            in[i] = new AMQPString("some kinda string with " + i + " in it");
        }
        AMQPString out[] = (AMQPString[]) writeRead(new AMQPArray(in)).getValue();
        assertArrayEquals(in, out);
    }

}
