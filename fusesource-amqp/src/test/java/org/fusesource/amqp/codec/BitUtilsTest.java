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

import org.junit.Test;

import static org.fusesource.amqp.codec.marshaller.BitUtils.unsigned;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BitUtilsTest {

    @Test
    public void testUnsignedByte() throws Exception {
        byte wrapped = -96;
        short value = unsigned(wrapped);

        System.out.printf("in : %X out : %X\n", wrapped, value);

        assertEquals(wrapped, (byte) value);


    }
}
