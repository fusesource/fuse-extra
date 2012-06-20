package org.fusesource.amqp.codec;

import org.fusesource.amqp.codec.marshaller.AMQPProtocolHeaderCodec;
import org.fusesource.amqp.codec.types.AMQPHeaderFrame;
import org.fusesource.amqp.codec.types.AMQPTransportFrame;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtdispatch.transport.AbstractProtocolCodec;

import java.io.DataInputStream;
import java.io.IOException;

/**
 */
public class AMQPProtocolCodec extends AbstractProtocolCodec {

    static final public Buffer MAGIC = new Buffer(AMQPDefinitions.MAGIC);

    @Override
    protected void encode(Object object) throws IOException {
        if (object instanceof AMQPTransportFrame) {
            try {
                AMQPTransportFrame frame = (AMQPTransportFrame) object;
                frame.write(nextWriteBuffer);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if (object instanceof AMQPHeaderFrame) {
            AMQPHeaderFrame header = (AMQPHeaderFrame) object;
            AMQPProtocolHeaderCodec.INSTANCE.encode(header, nextWriteBuffer);
        } else {
            throw new IOException("Invalid object type");
        }
    }

    @Override
    protected Action initialDecodeAction() {
        return new Action() {
            public Object apply() throws IOException {
                Buffer magic = readBytes(8);
                if (magic != null) {
                    nextDecodeAction = read_frame_size;
                    if (!magic.startsWith(MAGIC)) {
                        throw new IOException("Invalid protocol header");
                    }
                    return AMQPProtocolHeaderCodec.INSTANCE.decode(new DataInputStream(magic.in()));
                } else {
                    return null;
                }
            }
        };
    }

    private final Action read_frame_size = new Action() {
        public Object apply() throws IOException {
            Buffer size_bytes = readBytes(4);
            if (size_bytes != null) {
                // rewind as we want to include the frame size bytes in the
                // next read step...
                readStart -= 4;
                int size = size_bytes.bigEndianEditor().readInt();
                if (size < 8) {
                    throw new IOException(String.format("specified frame size %d smaller than minimum frame size", size));
                }
                // TODO: check frame min and max size..
                nextDecodeAction = read_frame(size);
                return nextDecodeAction.apply();
            } else {
                return null;
            }
        }
    };

    private final Action read_frame(final int size) {
        return new Action() {
            public Object apply() throws IOException {
                Buffer frame_data = readBytes(size);
                if (frame_data != null) {
                    nextDecodeAction = read_frame_size;
                    Buffer header = new Buffer(frame_data);
                    header.moveTail(8 - size);
                    frame_data.moveHead(8);

                    AMQPTransportFrame frame = new AMQPTransportFrame(header, frame_data);
                    return frame;
                } else {
                    return null;
                }
            }
        };
    }
    
    public int getReadBytesPendingDecode() {
        return readBuffer.position() - readStart;
    }

    public void skipProtocolHeader() {
        nextDecodeAction = read_frame_size;
    }

}
