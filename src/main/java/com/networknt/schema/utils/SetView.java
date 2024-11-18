package com.networknt.schema.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * View of a list of sets.
 * <p>
 * This is used for performance to reduce copies but breaks the semantics of the
 * set where the elements must all be unique.
 * 
 * @param <E> the type contains in the set
 */
public class SetView<E> implements Set<E> {
    private final List<Set<E>> sets = new ArrayList<>();

    /**
     * Adds a set to the view.
     * 
     * @param set to add to the view
     * @return the view
     */
    public SetView<E> union(Set<E> set) {
        if (set != null && !set.isEmpty()) {
            this.sets.add(set);
        }
        return this;
    }

    @Override
    public int size() {
        int size = 0;
        for (Set<E> set : sets) {
            size += set.size();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return sets.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        for (Set<E> set : sets) {
            if (set.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new SetViewIterator<>(this);
    }

    @Override
    public Object[] toArray() {
        int size = size();
        Object[] result = new Object[size];
        Iterator<?> iterator = iterator();
        for (int x = 0; x < size; x++) {
            result[x] = iterator.hasNext() ? iterator.next() : null;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        int size = size();
        T[] result = size <= a.length ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), size);
        Iterator<?> iterator = iterator();
        for (int x = 0; x < size; x++) {
            result[x] = iterator.hasNext() ? (T) iterator.next() : null;
        }
        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c) {
            if (!contains(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> coll) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Iterator.
     * 
     * @param <E> the type contains in the set
     */
    public static class SetViewIterator<E> implements Iterator<E> {
        private Iterator<Set<E>> sets = null;
        private Iterator<E> current = null;

        public SetViewIterator(SetView<E> view) {
            this.sets = view.sets.iterator();
            if (this.sets.hasNext()) {
                this.current = this.sets.next().iterator();
            }
        }

        @Override
        public boolean hasNext() {
            if (this.current.hasNext()) {
                return true;
            }
            while (this.sets.hasNext()) {
                this.current = this.sets.next().iterator();
                if (this.current.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public E next() {
            return this.current.next();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(sets);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Set)) {
            return false;
        }
        Collection<?> collection = (Collection<?>) obj;
        if (collection.size() != size()) {
            return false;
        }
        try {
            return containsAll(collection);
        } catch (ClassCastException ignore) {
            return false;
        } catch (NullPointerException ignore) {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        Iterator<E> iterator = iterator();
        if (iterator.hasNext()) {
            builder.append(iterator.next().toString());
        }
        while (iterator.hasNext()) {
            builder.append(", ");
            builder.append(iterator.next().toString());
        }
        builder.append(']');
        return builder.toString();
    }
}
