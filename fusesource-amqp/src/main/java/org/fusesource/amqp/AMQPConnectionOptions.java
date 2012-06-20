package org.fusesource.amqp;

import org.fusesource.amqp.codec.types.AMQPSymbol;
import org.fusesource.hawtdispatch.transport.Transport;

import java.util.Map;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPConnectionOptions {

    Logger logger = new ConsoleLogger();

    String containerId;
    private String hostName;
    private long idleTimeout = 90000;
    private int maxFrameSize = 1024*1024*1024*2 - 1;
    private AMQPSymbol[] desiredCapabilities;
    private AMQPSymbol[] offeredCapabilities;

//    = new AMQPSymbol[]{
//        new AMQPSymbol(ascii("APACHE.ORG:NO_LOCAL")),
//        new AMQPSymbol(ascii("APACHE.ORG:SELECTOR"))
//    };
    private AMQPSymbol[] incomingLocales;
    private AMQPSymbol[] outgoingLocales;
    private Map properties;
    private long sessionHandleMax = 1024*1024*1024*2 - 1;
    private AMQPConnection.Listener listener;
    Transport transport;
    boolean server;
    String userName;
    String password;

    public AMQPConnectionOptions() {
    }

    public AMQPConnectionOptions(AMQPConnectionOptions other) {
        this.logger = other.logger;
        this.containerId = other.containerId;
        this.hostName = other.hostName;
        this.idleTimeout = other.idleTimeout;
        this.maxFrameSize = other.maxFrameSize;
        this.desiredCapabilities = other.desiredCapabilities;
        this.offeredCapabilities = other.offeredCapabilities;
        this.incomingLocales = other.incomingLocales;
        this.properties = other.properties;
        this.sessionHandleMax = other.sessionHandleMax;
        this.listener = other.listener;
        this.transport = other.transport;
        this.server = other.server;
        this.userName = other.userName;
        this.password = other.password;
    }

    public AMQPConnectionOptions copy() {
        return new AMQPConnectionOptions(this);
    }

    public AMQPConnection.Listener getListener() {
        return listener;
    }

    public void setListener(AMQPConnection.Listener listener) {
        this.listener = listener;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public AMQPSymbol[] getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void setDesiredCapabilities(AMQPSymbol[] desiredCapabilities) {
        this.desiredCapabilities = desiredCapabilities;
    }

    public AMQPSymbol[] getOfferedCapabilities() {
        return offeredCapabilities;
    }

    public void setOfferedCapabilities(AMQPSymbol[] offeredCapabilities) {
        this.offeredCapabilities = offeredCapabilities;
    }

    public AMQPSymbol[] getIncomingLocales() {
        return incomingLocales;
    }

    public void setIncomingLocales(AMQPSymbol[] incomingLocales) {
        this.incomingLocales = incomingLocales;
    }

    public AMQPSymbol[] getOutgoingLocales() {
        return outgoingLocales;
    }

    public void setOutgoingLocales(AMQPSymbol[] outgoingLocales) {
        this.outgoingLocales = outgoingLocales;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public long getSessionHandleMax() {
        return sessionHandleMax;
    }

    public void setSessionHandleMax(long sessionHandleMax) {
        this.sessionHandleMax = sessionHandleMax;
    }

    static public class Logger {
        public void debug(String format, Object... args) {
            _debug(format, args);
        }
        public void trace(String format, Object... args) {
            _trace(format, args);
        }
        protected void _debug(String format, Object[] args) {
        }
        protected void _trace(String format, Object[] args) {
        }
    }

    static public class ConsoleLogger extends Logger {
        public void debug(String format, Object... args) {
            System.out.println("[debug] "+String.format(format, args));
        }
        public void trace(String format, Object... args) {
            System.out.println("[trace] "+String.format(format, args));
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public boolean isServer() {
        return server;
    }

    public void setServer(boolean server) {
        this.server = server;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
