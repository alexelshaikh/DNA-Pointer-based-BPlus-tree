package datastructures.container;

import utils.FuncUtils;
import utils.Pair;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class BlockingContainer<K, V> implements Container<K, V> {
    private final Container<K, V> container;
    private final Map<K, CountDownLatch> latchMap;

    public BlockingContainer(Container<K, V> container) {
        this.container = container;
        this.latchMap = new HashMap<>();
    }

    @Override
    public V get(K key) {
        V value = container.get(key);
        if (value != null)
            return value;

        CountDownLatch latch;
        synchronized (latchMap) {
            latch = latchMap.get(key);

            if (latch == null) {
                latch = new CountDownLatch(1);
                latchMap.put(key, latch);
            }
        }

        CountDownLatch finalLatch = latch;
        FuncUtils.safeRun(finalLatch::await);
        return container.get(key);
    }

    @Override
    public long size() {
        return container.size();
    }

    @Override
    public Collection<V> values() {
        return container.values();
    }

    @Override
    public Set<K> keys() {
        return container.keys();
    }

    public void put(K key, V value) {
        container.put(key, value);
        synchronized (latchMap) {
            CountDownLatch latch = latchMap.remove(key);
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    @Override
    public boolean isPersistent() {
        return container.isPersistent();
    }

    @Override
    public boolean remove(K key) {
        synchronized (latchMap) {
            CountDownLatch latch = latchMap.remove(key);
            if (latch != null) {
                if(latch.getCount() > 0L)
                    return false;
            }
            return container.remove(key);
        }
    }

    @Override
    public Iterator<Pair<K, V>> iterator() {
        return container.iterator();
    }
}
