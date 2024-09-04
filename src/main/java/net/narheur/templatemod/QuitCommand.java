package net.narheur.templatemod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import static net.narheur.templatemod.TemplateMod.LOGGER;

public class QuitCommand implements ModInitializer {

    @Override
    public void onInitialize() {
        // Register server command
        CommandRegistrationCallback.EVENT.register(QuitCommand::register);
    }

    // Define the server command
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("quit")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("player", EntityArgumentType.player())
            .executes(context -> {

                ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");

                // Send packet to the client
                PacketByteBuf buf = PacketByteBufs.create();
                ServerPlayNetworking.send(targetPlayer, new Identifier("template-mod", "quit_player"), buf);

                // Register packet receiver and handle the response
                ServerPlayNetworking.registerGlobalReceiver(new Identifier("template-mod", "sa_file_check"), (server, player, handler, receivedBuf, responseSender) -> {
                    boolean hasSaFile = receivedBuf.readBoolean();
                    if (!hasSaFile) {
                        // Inform server console about the missing file
                        server.execute(() -> {
                            LOGGER.info(player.getName().getString() + "'nın sa adlı dosyası yok.");
                            // Execute the command
                            try {
                                server.getCommandManager().getDispatcher().execute("tag " + player.getName().getString() + " add no", server.getCommandSource());
                            } catch (CommandSyntaxException e) {
                                LOGGER.error("Failed to execute command", e);
                            }
                        });
                    }
                    else {
                        try {
                            server.getCommandManager().getDispatcher().execute("tag " + player.getName().getString() + " add yes", server.getCommandSource());
                        } catch (CommandSyntaxException e) {
                            LOGGER.error("Failed to execute command", e);
                        }
                    }
                });
           return 1;
            })));
    }
}
