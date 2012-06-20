package org.fusesource.fabric.apollo.amqp.internal;

import org.fusesource.fabric.apollo.amqp.Callback;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.Frame;
import org.fusesource.fabric.apollo.amqp.codec.types.AMQPTransportFrame;
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
