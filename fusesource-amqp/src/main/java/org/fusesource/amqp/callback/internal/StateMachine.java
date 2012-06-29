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

package org.fusesource.amqp.callback.internal;

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
