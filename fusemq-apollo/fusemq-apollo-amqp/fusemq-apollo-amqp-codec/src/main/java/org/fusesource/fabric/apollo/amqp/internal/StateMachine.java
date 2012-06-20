package org.fusesource.fabric.apollo.amqp.internal;

import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
abstract public class StateMachine<S extends StateMachine.State> {

    public class State {
        public void onActivate() {
        }

        public void onDeactivate() {
        }

        /**
         * @return creates an IllegalStateException
         */
        protected IllegalStateException invalidState() {
            return new IllegalStateException("Not allowed from state: "+getClass().getSimpleName());
        }

        /**
         * Changes to the new state only if it is still the current state.
         *
         * @param next
         */
        final protected boolean become(S next) {
            if (active()) {
                current = next;
                this.onDeactivate();
                next.onActivate();
                return true;
            }
            return false;
        }

        public boolean active() {
            return current == this;
        }

        /**
         * Executes the code block only if we are still the current state.
         *
         * @param func
         */
        final boolean react(Task func) {
            if (active()) {
                func.run();
                return true;
            }
            return false;
        }
    }

    private S current = init();

    protected S current() {
        return current;
    }

    abstract protected S init();

    protected void react(Class<? extends S> state, Task func) {
        if (state == current.getClass()) {
            current.react(func);
        }
    }

}
