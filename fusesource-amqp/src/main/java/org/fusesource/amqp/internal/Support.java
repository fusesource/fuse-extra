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

package org.fusesource.amqp.internal;

import org.fusesource.amqp.Callback;
import org.fusesource.amqp.codec.interfaces.Frame;
import org.fusesource.amqp.codec.types.AMQPTransportFrame;
import org.fusesource.hawtbuf.Buffer;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
class Support {

    static void die(String s, Object...args) throws Exception {
        throw new Exception(String.format(s, args));
    }
    
    static void fail(Callback<?> cb, Throwable failure) {
        if(cb!=null) {
            cb.onFailure(failure);
        }
    }

    static <T> void success(Callback<T> cb, T result) {
        if(cb!=null) {
            cb.onSuccess(result);
        }
    }
    
    static void requireArgument(String name, Object value) {
        if( value==null ) {
            throw new IllegalArgumentException(String.format("The %s argument cannot be null", name));
        }
    }

    static AMQPTransportFrame frame(Frame frame) {
        return new AMQPTransportFrame(frame);
    }
    static AMQPTransportFrame frame(int channel, Frame frame) {
        return new AMQPTransportFrame(channel, frame);
    }
    static AMQPTransportFrame frame(int channel, Frame frame, Buffer payload) {
        return new AMQPTransportFrame(channel, frame, payload);
    }

}
