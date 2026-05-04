package org.example.service;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

// ================================================
// GENERIC CLASS — T is the type parameter
// ================================================
// T is decided when you CREATE the repository:
//   Repository<BankAccount> → T becomes BankAccount everywhere
//   Repository<Transaction> → T becomes Transaction everywhere
//   Repository<String>      → T becomes String everywhere

public class Repository<T> {

    private final List<T> items;
    private final String name; // for display purposes

    public Repository(String name) {
        this.items = new ArrayList<>();
        this.name = name;
    }

    // ================================================
    // BASIC OPERATIONS — T adapts to whatever type you use
    // ================================================

    public void add(T item) {
        // T is BankAccount? → add(BankAccount item)
        // T is Transaction? → add(Transaction item)
        // Same code, different types — that's generics
        items.add(item);
    }

    public void addAll(Collection<T> newItems) {
        items.addAll(newItems);
    }

    public T getByIndex(int index) {
        if (index < 0 || index >= items.size()) {
            throw new IndexOutOfBoundsException(
                    name + ": index " + index + " out of bounds (size: " + items.size() + ")");
        }
        return items.get(index);
    }

    public boolean remove(T item) {
        return items.remove(item);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<T> getAll() {
        return List.copyOf(items);
    }

    // ================================================
    // GENERIC METHODS — using T from the class
    // ================================================

    // Find first item matching a condition
    public Optional<T> findFirst(Predicate<T> condition) {
        return items.stream()
                .filter(condition)
                .findFirst();
    }

    // Find all items matching a condition
    public List<T> findAll(Predicate<T> condition) {
        return items.stream()
                .filter(condition)
                .toList();
    }

    // Check if any item matches
    public boolean anyMatch(Predicate<T> condition) {
        return items.stream().anyMatch(condition);
    }

    // Check if all items match
    public boolean allMatch(Predicate<T> condition) {
        return items.stream().allMatch(condition);
    }

    // Count matching items
    public long count(Predicate<T> condition) {
        return items.stream().filter(condition).count();
    }

    // Remove all matching items
    public int removeAll(Predicate<T> condition) {
        int before = items.size();
        items.removeIf(condition);
        return before - items.size();
    }

    // ================================================
    // GENERIC METHOD WITH DIFFERENT RETURN TYPE — <R>
    // ================================================
    // The method introduces its OWN type parameter R
    // independent of the class's T

    // Transform each item → list of R
    public <R> List<R> map(Function<T, R> mapper) {
        // T = class type parameter (e.g., BankAccount)
        // R = method type parameter (e.g., String, Double, anything)
        return items.stream()
                .map(mapper)
                .toList();
    }

    // Transform and flatten
    public <R> List<R> flatMap(Function<T, List<R>> mapper) {
        return items.stream()
                .flatMap(item -> mapper.apply(item).stream())
                .toList();
    }

    // Group by a key
    public <K> Map<K, List<T>> groupBy(Function<T, K> keyExtractor) {
        // K = the key type (e.g., AccountStatus, TransactionType)
        return items.stream()
                .collect(Collectors.groupingBy(keyExtractor));
    }

    // Group by key and count
    public <K> Map<K, Long> countBy(Function<T, K> keyExtractor) {
        return items.stream()
                .collect(Collectors.groupingBy(keyExtractor, Collectors.counting()));
    }

    // Group by key and sum a numeric value
    public <K> Map<K, Double> sumBy(Function<T, K> keyExtractor,
                                    ToDoubleFunction<T> valueExtractor) {
        return items.stream()
                .collect(Collectors.groupingBy(
                        keyExtractor,
                        Collectors.summingDouble(valueExtractor)
                ));
    }

    // ================================================
    // SORTING — uses Comparator<T>
    // ================================================

    public List<T> sorted(Comparator<T> comparator) {
        return items.stream()
                .sorted(comparator)
                .toList();
    }

    public Optional<T> max(Comparator<T> comparator) {
        return items.stream().max(comparator);
    }

    public Optional<T> min(Comparator<T> comparator) {
        return items.stream().min(comparator);
    }

    // Top N items by comparator
    public List<T> topN(int n, Comparator<T> comparator) {
        return items.stream()
                .sorted(comparator)
                .limit(n)
                .toList();
    }

    // ================================================
    // REDUCE — combine all items into one
    // ================================================

    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return items.stream().reduce(accumulator);
    }

    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return items.stream().reduce(identity, accumulator);
    }

    public double sumDouble(ToDoubleFunction<T> extractor) {
        return items.stream().mapToDouble(extractor).sum();
    }

    public OptionalDouble average(ToDoubleFunction<T> extractor) {
        return items.stream().mapToDouble(extractor).average();
    }

    // ================================================
    // DISPLAY
    // ================================================

    public void forEach(Consumer<T> action) {
        items.forEach(action);
    }

    public void printAll() {
        System.out.println("--- " + name + " (" + items.size() + " items) ---");
        for (int i = 0; i < items.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + items.get(i));
        }
    }

    public void printMatching(Predicate<T> condition, String label) {
        List<T> matches = findAll(condition);
        System.out.println("--- " + label + " (" + matches.size() + " matches) ---");
        matches.forEach(item -> System.out.println("  " + item));
    }

    public String getName() { return name; }

    @Override
    public String toString() {
        return name + "[" + items.size() + " items]";
    }
}