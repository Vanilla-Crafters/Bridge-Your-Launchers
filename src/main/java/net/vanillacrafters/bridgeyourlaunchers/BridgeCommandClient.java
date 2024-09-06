package net.vanillacrafters.bridgeyourlaunchers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class BridgeCommandClient implements ClientModInitializer {

    private static final Logger LOGGER = Logger.getLogger("BridgeCommandClient");
    private static final String configFolderName = "bridgeyourlaunchers";
    private static final String profilesFolderName = "profiles";

    @Override
    public void onInitializeClient() {
        BridgeConfigHelper.createConfigFile("config/bridgeyourlaunchers/config.json", profilesFolderName);

        ClientPlayNetworking.registerGlobalReceiver(new Identifier("bridgeyourlaunchers", "bridge_player"), (client, handler, buf, responseSender) -> {
            String profile = buf.readString();

            client.execute(() -> {
                try {
                    LOGGER.info("Bridge command received for profile: " + profile);

                    String minecraftDir = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
                    File profileDir = new File(minecraftDir + File.separator + "config" + File.separator + configFolderName + File.separator + profilesFolderName + File.separator + profile);

                    Optional<File> urlFile = BridgeConfigHelper.findUrlFile(profileDir);
                    PacketByteBuf responseBuf = PacketByteBufs.create();
                    responseBuf.writeBoolean(urlFile.isPresent());

                    ClientPlayNetworking.send(new Identifier("bridgeyourlaunchers", "file_check_response"), responseBuf);

                    if (urlFile.isPresent()) {
                        try {
                            Runtime.getRuntime().exec("cmd /c start \"\" \"" + urlFile.get().getAbsolutePath() + "\"");
                            sendChatMessage(client, "URL file opened successfully: " + urlFile.get().getAbsolutePath());
                        } catch (IOException e) {
                            LOGGER.severe("Failed to open URL file: " + e.getMessage());
                            sendChatMessage(client, "Failed to open URL file: " + e.getMessage());
                        }
                        MinecraftClient.getInstance().scheduleStop();
                        LOGGER.info("Minecraft client scheduled to stop.");
                    } else {
                        sendChatMessage(client, "No URL file found in profile: " + profile);
                    }
                } catch (Exception e) {
                    LOGGER.severe("Error handling packet: " + e.getMessage());
                    sendChatMessage(client, "Error handling packet: " + e.getMessage());
                }
            });
        });
    }

    private void sendChatMessage(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }
}
