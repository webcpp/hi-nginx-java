package hi;

import java.util.LinkedHashMap;
import java.util.Map;

public class lrucache<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    public lrucache(int capacity) {
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry entry) {
        return size() > capacity;
    }

}