package net.vanillacrafters.bridgeyourlaunchers.connect;

public abstract class ConnectStrategy {
    private int attempt = -1;

    // package-private constructor
    ConnectStrategy() { }

    public abstract void reconnect();

    public abstract String getName();

    public final int nextAttempt() {
        return ++attempt;
    }

    public final boolean isAttempting() {
        return attempt >= 0;
    }

    public final void resetAttempts() {
        attempt = -1;
    }
}
