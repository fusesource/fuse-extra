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

package org.fusesource.amqp.cli;

import org.fusesource.amqp.codec.AMQPProtocolCodec;
import org.fusesource.amqp.codec.types.AMQPTransportFrame;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Dump {

    public static void main(String[] args) {
        boolean skipHeader = false;
        for (String arg : args) {
            if ("--help".equals(arg) || "-help".equals(arg) || "-h".equals(arg) || "/?".equals(arg)) {
                showUsage();
                System.exit(0);
            }
        }
        if( args.length == 0 ) {
            showUsage();
            System.exit(1);
        }
        int rc = 0;
        for (String arg : args) {
            if ("--without-header".equals(arg)) {
                skipHeader = true;
            } else if ("--with-header".equals(arg)) {
                skipHeader = false;
            }
            println("--------------------------------------------------------");
            println(" File: " + arg);
            println("--------------------------------------------------------");

            FileInputStream is = null;
            try {
                is = new FileInputStream(arg);
            } catch (FileNotFoundException e) {
                System.err.println("Could not read file: " + arg);
                rc |= 4;
                continue;
            }
            try {
                AMQPProtocolCodec codec = new AMQPProtocolCodec();
                codec.setReadableByteChannel(is.getChannel());
                if( skipHeader ) {
                    codec.skipProtocolHeader();
                }

                long pos = 0L;
                Object frame = null;
                frame = readAMQPFrame(codec);
                while (frame != null) {
                    long next_pos = codec.getReadCounter() - codec.getReadBytesPendingDecode();
                    String label = String.format("@%08d ", pos);
                    System.out.print(label);
                    if( frame instanceof AMQPTransportFrame) {
                        String indent = label.replaceAll(".", " ");
                        println(((AMQPTransportFrame)frame).toString(indent));
                    } else {
                        println(frame);
                    }
                    pos = next_pos;
                    frame = readAMQPFrame(codec);
                }

            } catch (Exception e) {
                System.err.println("Failure occurred while decoding AMQP stream: "+e);
                rc |= 2;
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        System.exit(rc);
    }

    private static void showUsage() {
        println("Usage: dump ([options] file)+");
        println("");
        println("Summary:");
        println("");
        println("  Decodes a raw AMQP 1.0 stream and dumps it's");
        println("  frames to stdout for visual inspection.");
        println("");
        println("Options:");
        println("");
        println("  --help");
        println("      Show this help screen.");
        println("");
        println("  --with-header");
        println("      Expect the AMQP header at the starts of the");
        println("      file.  (Enabled by default).");
        println("");
        println("  --without-header");
        println("      Don't expect the AMQP header at the starts of the");
        println("      file.");
        println("");
        println("Exampe:");
        println("");
        println("   dump withheader.bin --without-header noheader.bin");
        println("");
    }

    private static void println(Object msg) {
        System.out.println(msg);
    }

    private static Object readAMQPFrame(AMQPProtocolCodec codec) throws IOException {
        Object frame;
        try {
            frame = codec.read();
        } catch (EOFException e) {
            frame = null;
        }
        return frame;
    }

}
