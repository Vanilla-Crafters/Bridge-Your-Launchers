package net.vanillacrafters.bridgeyourlaunchers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;

import static net.vanillacrafters.bridgeyourlaunchers.BridgeYourLaunchers.LOGGER;

public class BridgeCommand implements ModInitializer {

    private static final String configFolderName = "bridgeyourlaunchers";
    private static final String profilesFolderName = "profiles";
    private static final String configFileName = "config/bridgeyourlaunchers/config.json";

    @Override
    public void onInitialize() {
        // Register server command
        CommandRegistrationCallback.EVENT.register(BridgeCommand::register);

        // Create the config file on the server side
        BridgeConfigHelper.createConfigFile(configFileName, profilesFolderName);
    }

    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("bridge")
                .requires(source -> source.hasPermissionLevel(2))  // Command requires permission level 2
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("profile", StringArgumentType.greedyString())  // Handle spaces in profile names
                                .suggests((context, builder) -> {
                                    File profilesDir = new File(context.getSource().getServer().getRunDirectory(), "config/" + configFolderName + "/" + profilesFolderName);
                                    if (profilesDir.exists()) {
                                        File[] profileFolders = profilesDir.listFiles(File::isDirectory);
                                        if (profileFolders != null) {
                                            for (File profileFolder : profileFolders) {
                                                builder.suggest("\"" + profileFolder.getName() + "\"");
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                    String profile = StringArgumentType.getString(context, "profile");

                                    if (profile.startsWith("\"") && profile.endsWith("\"")) {
                                        profile = profile.substring(1, profile.length() - 1);
                                    }

                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeString(profile);
                                    ServerPlayNetworking.send(targetPlayer, new Identifier("bridgeyourlaunchers", "bridge_player"), buf);

                                    ServerPlayNetworking.registerGlobalReceiver(new Identifier("bridgeyourlaunchers", "file_check_response"), (server, player, handler, receivedBuf, responseSender) -> {
                                        boolean fileExists = receivedBuf.readBoolean();
                                        server.execute(() -> {
                                            try {
                                                String tagCommand = fileExists ? "tag " + player.getName().getString() + " add yes" : "tag " + player.getName().getString() + " add no";
                                                String removeNoTagCommand = "tag " + player.getName().getString() + " remove no"; // Command to remove "no" tag

                                                // Check for existing tags
                                                String checkTagCommand = "tag " + player.getName().getString() + " list";
                                                String existingTags = String.valueOf(server.getCommandManager().getDispatcher().execute(checkTagCommand, server.getCommandSource()));

                                                if (existingTags.contains("no") && fileExists) {
                                                    // Remove the "no" tag if the file exists
                                                    server.getCommandManager().getDispatcher().execute(removeNoTagCommand, server.getCommandSource());
                                                } else if (!existingTags.contains("no") && !fileExists) {
                                                    player.sendMessage(Text.of("Please add the profile URL."), false);
                                                }

                                                server.getCommandManager().getDispatcher().execute(tagCommand, server.getCommandSource());
                                                String configKey = fileExists ? "yes" : "no";
                                                BridgeConfigHelper.executeCommandsFromJson(server.getCommandSource(), configKey, configFileName);
                                            } catch (Exception e) {
                                                LOGGER.error("Failed to execute tag command", e);
                                            }
                                        });
                                    });

                                    return 1;
                                })))
                .executes(context -> {
                    context.getSource().sendError(Text.of("Please specify a profile."));
                    return 0;
                })
        );
    }
}
