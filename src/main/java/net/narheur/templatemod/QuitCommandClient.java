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

    private static final String fileName = "sa"; // Sadece dosya adı, uzantı olmadan
    private static final Logger LOGGER = Logger.getLogger("QuitCommandClient");

    @Override
    public void onInitializeClient() {
        // Sunucudan gelen quit_player paketini dinleme
        ClientPlayNetworking.registerGlobalReceiver(new Identifier("template-mod", "quit_player"), (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                LOGGER.info("Quit command received from server.");

                // Dosyayı kontrol etme
                String minecraftDir = MinecraftClient.getInstance().runDirectory.getAbsolutePath();
                File modsDir = new File(minecraftDir + File.separator + "mods");

                // Tam adı 'sa' olan dosyayı bulur (uzantısız)
                Optional<File> fileToRun = Arrays.stream(modsDir.listFiles())
                        .filter(file -> getFileNameWithoutExtension(file).equals(fileName))
                        .findFirst();

                // Sunucuya geri bildirim gönderme
                PacketByteBuf responseBuf = PacketByteBufs.create();
                boolean hasSaFile = fileToRun.isPresent();
                responseBuf.writeBoolean(hasSaFile);
                ClientPlayNetworking.send(new Identifier("template-mod", "sa_file_check"), responseBuf);

                if (!hasSaFile) {
                    // Dosya yoksa uyarı mesajı göster
                    MinecraftClient.getInstance().player.sendMessage(Text.of("Lütfen sa dosyasını yükleyiniz."), false);
                    LOGGER.warning("'sa' dosyası bulunamadı.");
                } else {
                    LOGGER.info("'sa' dosyası bulundu: " + fileToRun.get().getAbsolutePath());

                    // Dosya bulunursa çalıştır
                    try {
                        Runtime.getRuntime().exec("cmd /c start \"\" \"" + fileToRun.get().getAbsolutePath() + "\"");
                        sendChatMessage(client, "File executed successfully: " + fileToRun.get().getAbsolutePath());
                    } catch (IOException e) {
                        LOGGER.severe("Failed to execute file: " + e.getMessage());
                        sendChatMessage(client, "Failed to execute file: " + e.getMessage());
                    }

                    // Oyunu kapatma işlemi
                    MinecraftClient.getInstance().scheduleStop();
                    LOGGER.info("Minecraft client scheduled to stop.");
                }
            });
        });
    }

    // Dosya adını uzantısız şekilde döndüren yardımcı metot
    private static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    // Client'a mesaj göndermek için yardımcı metot
    private static void sendChatMessage(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.of(message), false);
        }
    }
}
