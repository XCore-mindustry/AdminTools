package admintools.ui.core;

import arc.struct.Seq;

/**
 * Manages the lifecycle of UI subscriptions, timers, and listeners.
 * Call {@link #close()} when an element or window is permanently disposed to prevent memory leaks.
 */
public final class UiScope implements AutoCloseable {
    private final Seq<Subscription> subscriptions = new Seq<>();
    private boolean closed = false;

    public Subscription own(Subscription subscription) {
        if (subscription == null) return Subscription.empty();
        if (closed) {
            subscription.close();
            return Subscription.empty();
        }
        subscriptions.add(subscription);
        return () -> {
            subscriptions.remove(subscription, true);
            subscription.close();
        };
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        for (int i = 0; i < subscriptions.size; i++) {
            try {
                subscriptions.get(i).close();
            } catch (Throwable ignored) {
            }
        }
        subscriptions.clear();
    }
}
