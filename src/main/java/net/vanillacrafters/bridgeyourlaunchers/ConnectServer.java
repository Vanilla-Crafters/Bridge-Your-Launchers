package net.vanillacrafters.bridgeyourlaunchers;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.vanillacrafters.bridgeyourlaunchers.config.AutoconnectConfig;
import net.vanillacrafters.bridgeyourlaunchers.connect.ConnectStrategy;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;

public class ConnectServer implements ClientModInitializer {
    private static final ScheduledThreadPoolExecutor EXECUTOR_SERVICE = new ScheduledThreadPoolExecutor(1);
    private static ConnectServer instance;
    private final AtomicReference<ScheduledFuture<?>> countdown = new AtomicReference<>(null);
    private ConnectStrategy connectStrategy = null;

    static {
        EXECUTOR_SERVICE.setRemoveOnCancelPolicy(true);
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        AutoconnectConfig.load();
    }

    public static ConnectServer getInstance() {
        return instance;
    }

    public static AutoconnectConfig getConfig() {
        return AutoconnectConfig.getInstance();
    }

    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit timeUnit) {
        return EXECUTOR_SERVICE.schedule(command, delay, timeUnit);
    }

    public void setConnectHandler(ConnectStrategy connectStrategy) {
        if (this.connectStrategy != null) {
            assert this.connectStrategy.getClass().equals(connectStrategy.getClass()) &&
                this.connectStrategy.getName().equals(connectStrategy.getName());
            return;
        }
        this.connectStrategy = connectStrategy;
    }

    public void connect() {
        if (connectStrategy == null) return;
        cancelCountdown();
        connectStrategy.reconnect();
    }

    public void startCountdown(final IntConsumer callback) {
        if (connectStrategy == null) {
            LogUtils.getLogger().error("Cannot connect because connectStrategy is null");
            callback.accept(-1);
        }

        int delay = getConfig().getDelayForAttempt(connectStrategy.nextAttempt());
        if (delay >= 0) {
            countdown(delay, callback);
        } else {
            callback.accept(-1);
        }
    }


    private void cancelCountdown() {
        synchronized (countdown) { // just to be sure
            if (countdown.get() == null) return;
            countdown.getAndSet(null).cancel(true); // should stop the timer
        }
    }

    private void countdown(int seconds, final IntConsumer callback) {
        if (connectStrategy == null) return;
        if (seconds == 0) {
            MinecraftClient.getInstance().execute(this::connect);
            return;
        }
        callback.accept(seconds);
        synchronized (countdown) { // just to be sure
            countdown.set(schedule(() -> countdown(seconds - 1, callback), 1, TimeUnit.SECONDS));
        }
    }

    public void cancelAutoReconnect() {
        if (connectStrategy == null) return; // should not happen
        connectStrategy.resetAttempts();
        cancelCountdown();
    }

    public void onScreenChanged(Screen current, Screen next) {
        if (sameType(current, next)) return;
        // TODO condition could use some improvement, shouldn't cause any issues tho
        if (!isMainScreen(current) && isMainScreen(next) || isReAuthenticating(current, next)) {
            cancelAutoReconnect();
            connectStrategy = null;
        }
    }

    private static boolean sameType(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a != null && b != null) return a.getClass().equals(b.getClass());
        return false;
    }

    private static boolean isMainScreen(Screen screen) {
        return screen instanceof TitleScreen || screen instanceof SelectWorldScreen ||
            screen instanceof MultiplayerScreen || screen instanceof RealmsMainScreen;
    }

    private static boolean isReAuthenticating(Screen from, Screen to) {
        return from instanceof DisconnectedScreen && to != null &&
            to.getClass().getName().startsWith("me.axieum.mcmod.authme");
    }


}
