package com.github.chengyuxing.plugin.rabbit.sql.util;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Value Set, different with HashSet.<br>
 * HashSet: ignore the same value by hashcode,<br>
 * ValueSet: replace the same value by hashcode<br>
 *
 * @param <E>
 */
public class ArrayListValueSet<E> extends AbstractSet<E> {
    private final List<E> list = new ArrayList<>();

    @Override
    public @NotNull Iterator<E> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean add(E e) {
        int idx = list.indexOf(e);
        if (idx != -1) {
            list.set(idx, e);
        } else {
            list.add(e);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return list.remove(o);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    public E get(int index) {
        return list.get(index);
    }

    public E get(E e) {
        int idx = list.indexOf(e);
        return list.get(idx);
    }
}
