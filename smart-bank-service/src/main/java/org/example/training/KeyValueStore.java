package org.example.training;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

// ================================================
// TWO TYPE PARAMETERS — K for key, V for value
// ================================================
// KeyValueStore<String, BankAccount> → fast account lookup
// KeyValueStore<String, Double>      → balance cache
// KeyValueStore<Integer, Transaction> → transaction lookup by ID

public class KeyValueStore<K, V> {

    private final Map<K, V> store;
    private final String name;

    public KeyValueStore(String name) {
        this.store = new LinkedHashMap<>(); // preserves insertion order
        this.name = name;
    }

    // --- Basic operations ---

    public void put(K key, V value) {
        store.put(key, value);
    }

    // 🆕 Returns Optional instead of null
    public Optional<V> get(K key) {
        return Optional.ofNullable(store.get(key));
    }

    // Get or compute if absent
    public V getOrCompute(K key, Function<K, V> computeFunction) {
        return store.computeIfAbsent(key, computeFunction);
    }

    public boolean containsKey(K key) {
        return store.containsKey(key);
    }

    public Optional<V> remove(K key) {
        return Optional.ofNullable(store.remove(key));
    }

    public int size() {
        return store.size();
    }

    public boolean isEmpty() {
        return store.isEmpty();
    }

    // --- Collection views ---

    public Set<K> keys() {
        return Set.copyOf(store.keySet());
    }

    public Collection<V> values() {
        return List.copyOf(store.values());
    }

    // --- Querying ---

    // Find values matching a condition
    public List<V> findValues(Predicate<V> condition) {
        return store.values().stream()
                .filter(condition)
                .toList();
    }

    // Find keys matching a condition on their values
    public List<K> findKeys(Predicate<V> valueCondition) {
        return store.entrySet().stream()
                .filter(entry -> valueCondition.test(entry.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    // ================================================
    // GENERIC METHOD — transform values to a different type
    // ================================================

    public <R> List<R> mapValues(Function<V, R> mapper) {
        return store.values().stream()
                .map(mapper)
                .toList();
    }

    // Build a NEW KeyValueStore with transformed values
    public <R> KeyValueStore<K, R> transformValues(String newName, Function<V, R> mapper) {
        KeyValueStore<K, R> result = new KeyValueStore<>(newName);
        store.forEach((key, value) -> result.put(key, mapper.apply(value)));
        return result;
    }

    // --- Iteration ---

    public void forEach(BiConsumer<K, V> action) {
        store.forEach(action);
    }

    // --- Display ---

    public void printAll() {
        System.out.println("--- " + name + " (" + store.size() + " entries) ---");
        store.forEach((key, value) ->
                System.out.println("  " + key + " → " + value));
    }

    @Override
    public String toString() {
        return name + "[" + store.size() + " entries]";
    }
}