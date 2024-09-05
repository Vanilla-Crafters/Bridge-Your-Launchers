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

public class BridgeCommand implements ModInitializer {

    private static final String configFolderName = "bridgeyourlaunchers";
    private static final String profilesFolderName = "profiles";

    @Override
    public void onInitialize() {
        // Register server command
        CommandRegistrationCallback.EVENT.register(BridgeCommand::register);
    }

    // Define the server command
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("bridge")
                .requires(source -> source.hasPermissionLevel(2))  // Command requires permission level 2
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("profile", StringArgumentType.greedyString())  // Changed to greedyString to handle spaces
                                .suggests((context, builder) -> {
                                    File profilesDir = new File(context.getSource().getServer().getRunDirectory(), "config/" + configFolderName + "/" + profilesFolderName);
                                    if (profilesDir.exists()) {
                                        File[] profileFolders = profilesDir.listFiles(File::isDirectory);
                                        if (profileFolders != null) {
                                            for (File profileFolder : profileFolders) {
                                                builder.suggest("\"" + profileFolder.getName() + "\"");  // Suggest with quotes
                                            }
                                        }
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "player");
                                    String profile = StringArgumentType.getString(context, "profile");

                                    // Remove quotes from the profile name if present
                                    if (profile.startsWith("\"") && profile.endsWith("\"")) {
                                        profile = profile.substring(1, profile.length() - 1);
                                    }

                                    // Send the profile name to the client
                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeString(profile);  // Write profile name into the buffer
                                    ServerPlayNetworking.send(targetPlayer, new Identifier("bridgeyourlaunchers", "bridge_player"), buf);

                                    return 1;
                                })))
                .executes(context -> {
                    context.getSource().sendError(Text.of("Please specify a profile."));
                    return 0; // Command fails if no profile is provided
                })
        );
    }
}
