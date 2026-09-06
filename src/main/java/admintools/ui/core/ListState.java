package admintools.ui.core;

import arc.Core;
import arc.func.Cons;
import arc.struct.Seq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reactive list state supporting immutable snapshots and change listeners.
 */
public class ListState<T> implements ReadState<List<T>> {
    private final List<T> items = new ArrayList<>();
    private final Seq<Cons<? super List<T>>> listeners = new Seq<>();

    public ListState() {
    }

    public ListState(List<T> initial) {
        if (initial != null) items.addAll(initial);
    }

    public static <T> ListState<T> of() {
        return new ListState<>();
    }

    public static <T> ListState<T> of(List<T> initial) {
        return new ListState<>(initial);
    }

    @Override
    public List<T> get() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void set(List<T> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        dispatch();
    }

    public void set(Seq<T> newItems) {
        items.clear();
        if (newItems != null) {
            for (int i = 0; i < newItems.size; i++) {
                items.add(newItems.get(i));
            }
        }
        dispatch();
    }

    public void add(T item) {
        items.add(item);
        dispatch();
    }

    public void remove(T item) {
        if (items.remove(item)) {
            dispatch();
        }
    }

    public void clear() {
        if (!items.isEmpty()) {
            items.clear();
            dispatch();
        }
    }

    private void dispatch() {
        List<T> snapshot = get();
        if (Core.app != null && !Core.app.isOnMainThread()) {
            Core.app.post(() -> notifyListeners(snapshot));
        } else {
            notifyListeners(snapshot);
        }
    }

    private void notifyListeners(List<T> snapshot) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).get(snapshot);
        }
    }

    @Override
    public Subscription subscribe(Cons<? super List<T>> listener) {
        listeners.add(listener);
        listener.get(get());
        return () -> listeners.remove(listener, true);
    }
}
