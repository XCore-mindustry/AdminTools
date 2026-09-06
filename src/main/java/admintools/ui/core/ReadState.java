package admintools.ui.core;

import arc.func.Cons;
import arc.func.Func;

/**
 * Read-only view of a reactive state value.
 */
public interface ReadState<T> {
    T get();

    Subscription subscribe(Cons<? super T> listener);

    default <R> ReadState<R> map(Func<T, R> mapper) {
        State<R> mapped = State.of(mapper.get(get()));
        subscribe(val -> mapped.set(mapper.get(val)));
        return mapped;
    }
}
