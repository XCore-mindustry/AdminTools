package admintools.ui.core;

import arc.Core;
import arc.func.Cons;
import arc.func.Func;
import arc.struct.Seq;

import java.util.Objects;

/**
 * A reactive state container that dispatches change notifications on Arc's main UI thread.
 */
public class State<T> implements ReadState<T> {
    protected T value;
    protected final Seq<Cons<? super T>> listeners = new Seq<>();
    private boolean notifying = false;
    private final Seq<Cons<? super T>> toAdd = new Seq<>();
    private final Seq<Cons<? super T>> toRemove = new Seq<>();

    public State(T initialValue) {
        this.value = initialValue;
    }

    public static <T> State<T> of(T initialValue) {
        return new State<>(initialValue);
    }

    @Override
    public T get() {
        return value;
    }

    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) return;
        this.value = newValue;
        dispatch(newValue);
    }

    public void update(Func<T, T> updater) {
        set(updater.get(this.value));
    }

    private void dispatch(T val) {
        if (Core.app != null && !Core.app.isOnMainThread()) {
            Core.app.post(() -> notifyListeners(val));
        } else {
            notifyListeners(val);
        }
    }

    private void notifyListeners(T val) {
        notifying = true;
        try {
            for (int i = 0; i < listeners.size; i++) {
                Cons<? super T> l = listeners.get(i);
                if (!toRemove.contains(l, true)) {
                    l.get(val);
                }
            }
        } finally {
            notifying = false;
            for (int i = 0; i < toAdd.size; i++) listeners.add(toAdd.get(i));
            for (int i = 0; i < toRemove.size; i++) listeners.remove(toRemove.get(i), true);
            toAdd.clear();
            toRemove.clear();
        }
    }

    @Override
    public Subscription subscribe(Cons<? super T> listener) {
        if (notifying) {
            toAdd.add(listener);
        } else {
            listeners.add(listener);
        }

        listener.get(value);

        return () -> {
            if (notifying) {
                toRemove.add(listener);
            } else {
                listeners.remove(listener, true);
            }
        };
    }
}
