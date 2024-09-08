package net.vanillacrafters.bridgeyourlaunchers.connect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

public class MultiplayerConnectStrategy extends ConnectStrategy {
    private final ServerInfo serverInfo;

    public MultiplayerConnectStrategy(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String getName() {
        return serverInfo.name;
    }

    @Override
    public void reconnect() {
        ConnectScreen.connect(
            new MultiplayerScreen(new TitleScreen()),
            MinecraftClient.getInstance(),
            ServerAddress.parse(serverInfo.address),
            serverInfo,
            false);
    }
}
