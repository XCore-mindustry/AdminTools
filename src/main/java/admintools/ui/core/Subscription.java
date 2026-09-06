package admintools.ui.core;

/**
 * Represents an active subscription/listener that can be canceled.
 */
@FunctionalInterface
public interface Subscription extends AutoCloseable {
    @Override
    void close();

    static Subscription empty() {
        return () -> {};
    }
}
