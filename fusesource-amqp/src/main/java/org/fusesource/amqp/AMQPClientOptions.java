package org.fusesource.amqp;

import org.fusesource.hawtdispatch.DispatchQueue;
import org.fusesource.hawtdispatch.transport.TcpTransport;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executor;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPClientOptions extends AMQPConnectionOptions {

    URI host = AMQP.DEFAULT_HOST;
    URI localAddress;
    SSLContext sslContext;
    DispatchQueue dispatchQueue;
    Executor blockingExecutor;
    int maxReadRate;
    int maxWriteRate;
    int trafficClass = TcpTransport.IPTOS_THROUGHPUT;
    int receiveBufferSize = 1024*64;
    int sendBufferSize = 1024*64;
    boolean useLocalHost = true;
//    long reconnectDelay = 10;
//    long reconnectDelayMax = 30*1000;
//    double reconnectBackOffMultiplier = 2.0f;
//    long reconnectAttemptsMax = -1;
//    long connectAttemptsMax = -1;

    public AMQPClientOptions() {
    }

    public AMQPClientOptions(AMQPClientOptions other) {
        super(other);
        this.host = other.host;
        this.localAddress = other.localAddress;
        this.sslContext = other.sslContext;
        this.dispatchQueue = other.dispatchQueue;
        this.blockingExecutor = other.blockingExecutor;
        this.maxReadRate = other.maxReadRate;
        this.maxWriteRate = other.maxWriteRate;
        this.trafficClass = other.trafficClass;
        this.receiveBufferSize = other.receiveBufferSize;
        this.sendBufferSize = other.sendBufferSize;
        this.useLocalHost = other.useLocalHost;
//        this.reconnectDelay = other.reconnectDelay;
//        this.reconnectDelayMax = other.reconnectDelayMax;
//        this.reconnectBackOffMultiplier = other.reconnectBackOffMultiplier;
//        this.reconnectAttemptsMax = other.reconnectAttemptsMax;
//        this.connectAttemptsMax = other.connectAttemptsMax;
    }

    public AMQPConnectionOptions copy() {
        return new AMQPClientOptions(this);
    }

    public String getHostName() {
        String hostName = super.getHostName();
        if( hostName ==null && host!=null ) {
            return host.getHost();
        }
        return hostName;
    }


    public Executor getBlockingExecutor() {
        return blockingExecutor;
    }

    public void setBlockingExecutor(Executor blockingExecutor) {
        this.blockingExecutor = blockingExecutor;
    }

    public DispatchQueue getDispatchQueue() {
        return dispatchQueue;
    }

    public void setDispatchQueue(DispatchQueue dispatchQueue) {
        this.dispatchQueue = dispatchQueue;
    }

    public URI getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) throws URISyntaxException {
        this.setLocalAddress(new URI(localAddress));
    }
    public void setLocalAddress(URI localAddress) {
        this.localAddress = localAddress;
    }

    public int getMaxReadRate() {
        return maxReadRate;
    }

    public void setMaxReadRate(int maxReadRate) {
        this.maxReadRate = maxReadRate;
    }

    public int getMaxWriteRate() {
        return maxWriteRate;
    }

    public void setMaxWriteRate(int maxWriteRate) {
        this.maxWriteRate = maxWriteRate;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public URI getHost() {
        return host;
    }
    public void setHost(String host, int port) throws URISyntaxException {
        this.setHost(new URI("tcp://"+host+":"+port));
    }
    public void setHost(String host) throws URISyntaxException {
        this.setHost(new URI(host));
    }
    public void setHost(URI host) {
        this.host = host;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    public int getTrafficClass() {
        return trafficClass;
    }

    public void setTrafficClass(int trafficClass) {
        this.trafficClass = trafficClass;
    }

    public boolean isUseLocalHost() {
        return useLocalHost;
    }

    public void setUseLocalHost(boolean useLocalHost) {
        this.useLocalHost = useLocalHost;
    }

//    public long getConnectAttemptsMax() {
//        return connectAttemptsMax;
//    }
//
//    public void setConnectAttemptsMax(long connectAttemptsMax) {
//        this.connectAttemptsMax = connectAttemptsMax;
//    }
//
//    public long getReconnectAttemptsMax() {
//        return reconnectAttemptsMax;
//    }
//
//    public void setReconnectAttemptsMax(long reconnectAttemptsMax) {
//        this.reconnectAttemptsMax = reconnectAttemptsMax;
//    }
//
//    public double getReconnectBackOffMultiplier() {
//        return reconnectBackOffMultiplier;
//    }
//
//    public void setReconnectBackOffMultiplier(double reconnectBackOffMultiplier) {
//        this.reconnectBackOffMultiplier = reconnectBackOffMultiplier;
//    }
//
//    public long getReconnectDelay() {
//        return reconnectDelay;
//    }
//
//    public void setReconnectDelay(long reconnectDelay) {
//        this.reconnectDelay = reconnectDelay;
//    }
//
//    public long getReconnectDelayMax() {
//        return reconnectDelayMax;
//    }
//
//    public void setReconnectDelayMax(long reconnectDelayMax) {
//        this.reconnectDelayMax = reconnectDelayMax;
//    }

}
