package org.example.service;

// ================================================
// GENERIC CLASS WITH TWO TYPE PARAMETERS
// ================================================

public class Pair<A, B> {

    private final A first;
    private final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A first()  { return first; }
    public B second() { return second; }

    // ================================================
    // GENERIC STATIC FACTORY METHOD
    // ================================================
    // The <A, B> before the return type declares the method's own type parameters
    // These are INDEPENDENT of any class type parameters

    public static <A, B> Pair<A, B> of(A first, B second) {
        return new Pair<>(first, second);
    }

    // ================================================
    // GENERIC METHOD — transform one or both elements
    // ================================================

    public <R> Pair<R, B> mapFirst(java.util.function.Function<A, R> mapper) {
        return new Pair<>(mapper.apply(first), second);
    }

    public <R> Pair<A, R> mapSecond(java.util.function.Function<B, R> mapper) {
        return new Pair<>(first, mapper.apply(second));
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return java.util.Objects.equals(first, pair.first)
                && java.util.Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(first, second);
    }
}