package com.github.chengyuxing.plugin.rabbit.sql.util;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ValueHashSet<E> extends AbstractSet<E> implements Set<E> {
    private final Map<E, E> map;

    public ValueHashSet() {
        map = new HashMap<>();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsValue(o);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return map.values().iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return map.values().toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return map.values().toArray(a);
    }

    @Override
    public boolean add(E e) {
        E r = map.put(e, e);
        return r != e;
    }

    @Override
    public boolean remove(Object o) {
        E r = map.remove(o);
        return r != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ValueHashSet<?> that)) return false;
        if (!super.equals(o)) return false;

        return map.values().equals(that.map.values());
    }

    @Override
    public int hashCode() {
        return map.values().hashCode();
    }
}
