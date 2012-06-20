package org.fusesource.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Callback<T> {
    public void onSuccess(T value) {}
    public void onFailure(Throwable value){}
}
