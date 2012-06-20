package org.fusesource.fabric.apollo.amqp.codec.types;

import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPType;

import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

/**
 */
public class MapEntries extends ArrayList<SimpleImmutableEntry<AMQPType, AMQPType>>{
    public MapEntries() {
    }

    public MapEntries(Collection<? extends SimpleImmutableEntry<AMQPType, AMQPType>> c) {
        super(c);
    }

    public MapEntries(int initialCapacity) {
        super(initialCapacity);
    }
    
    public MapEntries(Map<AMQPType, AMQPType> c) {
        super(c.size());
        for (Map.Entry<AMQPType, AMQPType> entry : c.entrySet()) {
            add(new SimpleImmutableEntry<AMQPType, AMQPType>(entry.getKey(), entry.getValue()));
        }
    }
    
    public static MapEntries create(Map<AMQPType, AMQPType> c) {
        if( c == null ) {
            return null;
        } else {
            return new MapEntries(c);
        }
    }
    
    public HashMap<AMQPType, AMQPType> toHashMap() {
        HashMap<AMQPType, AMQPType> rc = new HashMap<AMQPType, AMQPType>();
        fill(rc);
        return rc;
    }

    public LinkedHashMap<AMQPType, AMQPType> toLinkedHashMap() {
        LinkedHashMap<AMQPType, AMQPType> rc = new LinkedHashMap<AMQPType, AMQPType>();
        fill(rc);
        return rc;
    }

    public void fill(Map<AMQPType, AMQPType> map) {
        for (SimpleImmutableEntry<AMQPType, AMQPType> entry : this) {
            map.put(entry.getKey(), entry.getValue());
        }
    }

    public boolean add(AMQPType key, AMQPType value) {
        return add(new SimpleImmutableEntry<AMQPType, AMQPType>(key, value));
    }

    public AMQPType get(AMQPType key) {
        for (SimpleImmutableEntry<AMQPType, AMQPType> entry : this) {
            if( entry.equals(key) ) {
                return entry.getValue();
            }
        }
        return null;
    }
}
