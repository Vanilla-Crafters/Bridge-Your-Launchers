package net.vanillacrafters.bridgeyourlaunchers.connect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.MessageScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

public class SingleplayerConnectStrategy extends ConnectStrategy {
    private final String worldName;

    public SingleplayerConnectStrategy(String worldName) {
        this.worldName = worldName;
    }

    @Override
    public String getName() {
        return worldName;
    }

    @Override
    public void reconnect() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.getLevelStorage().levelExists(getName())) return;
        client.setScreenAndRender(new MessageScreen(Text.translatable("selectWorld.data_read")));
        client.createIntegratedServerLoader().start(new TitleScreen(), getName());
    }
}
