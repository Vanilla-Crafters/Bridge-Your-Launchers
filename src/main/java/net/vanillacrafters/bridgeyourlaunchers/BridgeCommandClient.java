package net.vanillacrafters.bridgeyourlaunchers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;

public class BridgeCommandClient implements ClientModInitializer {

    private static final String fileName = "sa"; // File Name without extension
    private static final Logger LOGGER = Logger.getLogger("BridgeCommandClient");
    private static final String configFolderName = "bridgeyourlaunchers";
    private static final String profilesFolderName = "profiles";
    private static final String readmeFileName = "readme.txt";

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
        // Create config folder and files
        createConfigFolderAndFiles();

        // Check the Packet Sent by the Server
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("bridgeyourlaunchers", "bridge_player"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                LOGGER.info("Quit command received from server.");

                // Check the file
                String minecraftDir = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
                File profilesDir = new File(minecraftDir + File.separator + "config" + File.separator + configFolderName + File.separator + profilesFolderName);

                // Find the File Named "sa"
                Optional<File> fileToRun = Arrays.stream(profilesDir.listFiles())
                        .filter(file -> getFileNameWithoutExtension(file).equals(fileName))
                        .findFirst();

                // Send Feedback to Server
                PacketByteBuf responseBuf = PacketByteBufs.create();
                boolean hasSaFile = fileToRun.isPresent();
                responseBuf.writeBoolean(hasSaFile);
                ClientPlayNetworking.send(new Identifier("bridgeyourlaunchers", "sa_file_check"), responseBuf);

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

    private void createConfigFolderAndFiles() {
        String minecraftDir = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
        File configDir = new File(minecraftDir + File.separator + "config" + File.separator + configFolderName);
        File profilesDir = new File(configDir + File.separator + profilesFolderName);
        File readmeFile = new File(configDir, readmeFileName);

        // Create the folders if they don't exist
        if (!configDir.exists()) {
            configDir.mkdirs();
            LOGGER.info("Created config folder: " + configDir.getAbsolutePath());
        }

        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
            LOGGER.info("Created profiles folder: " + profilesDir.getAbsolutePath());
        }

        // Create the readme.txt file
        if (!readmeFile.exists()) {
            try (FileWriter writer = new FileWriter(readmeFile)) {
                writer.write("This is the readme file for Bridge Your Launchers mod.");
                LOGGER.info("Created readme file: " + readmeFile.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.severe("Failed to create readme file: " + e.getMessage());
            }
        }
    }
}
