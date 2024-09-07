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

<<<<<<< HEAD
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;

import static net.vanillacrafters.bridgeyourlaunchers.BridgeYourLaunchers.LOGGER;
=======
import java.io.File;
>>>>>>> f8a00a3 (Added command sending to client.)


public class BridgeCommand implements ModInitializer {

    private static final String configFolderName = "bridgeyourlaunchers";
    private static final String profilesFolderName = "profiles";
    private static final String commandConfigFileName = "command_configs.json"; // Command config file
    private static final String readmeFileName = "readme.txt";
    private static final String configFileName = "command_configs.json";

    @Override
    public void onInitialize() {
        // Register server command
        CommandRegistrationCallback.EVENT.register(BridgeCommand::register);
        // Ensure config folder and files are created on initialization
        createConfigFolderAndFiles();
    }

    private void createConfigFolderAndFiles() {
        File serverDir = new File(".");  // Use server's run directory
        File configDir = new File(serverDir, "config" + File.separator + configFolderName);
        File profilesDir = new File(configDir, profilesFolderName);
        File readmeFile = new File(configDir, readmeFileName);
        File configFileJson = new File(configDir, configFileName);

        // Create the folders if they don't exist
        if (!configDir.exists()) {
            configDir.mkdirs();
            LOGGER.info("Created config folder: " + configDir.getAbsolutePath());
        }

        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
            LOGGER.info("Created profiles folder: " + profilesDir.getAbsolutePath());
        }

        if (!configFileJson.exists()) {
            try (FileWriter writer = new FileWriter(configFileJson)) {
                writer.write("{\n" +
                        "  \"ifFileFound\": \"execute as <player> at @s run say File found. Example command executed.\",\n" +
                        "  \"ifFileNotFound\": \"execute as <player> at @s run say File not found. Example command executed.\"\n" +
                        "}");
                LOGGER.info("Created config file: " + configFileJson.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.error("Failed to create config file: " + e.getMessage());
            }
        }

        // Create the readme.txt file
        if (!readmeFile.exists()) {
            try (InputStream inputStream = BridgeCommand.class.getClassLoader().getResourceAsStream("readmes/readme.txt")) {
                assert inputStream != null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                     FileWriter fileWriter = new FileWriter(readmeFile);
                     BufferedWriter writer = new BufferedWriter(fileWriter)) {

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }

                    LOGGER.info("Created readme file: " + readmeFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create readme file: " + e.getMessage());
            }
        }
    }



    private static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("bridge")
                .requires(source -> source.hasPermissionLevel(2))  // Command requires permission level 2
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("profile", StringArgumentType.greedyString())
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

                                    // Remove quotes from the profile name if present
                                    if (profile.startsWith("\"") && profile.endsWith("\"")) {
                                        profile = profile.substring(1, profile.length() - 1);
                                    }

                                    // Send the profile name to the client
                                    PacketByteBuf buf = PacketByteBufs.create();
                                    buf.writeString(profile);  // Write profile name into the buffer
                                    ServerPlayNetworking.send(targetPlayer, new Identifier("bridgeyourlaunchers", "bridge_player"), buf);

<<<<<<< HEAD
                                    // Register packet receiver to handle client's response
                                    ServerPlayNetworking.registerGlobalReceiver(new Identifier("bridgeyourlaunchers", "file_check_response"), (server, player, handler, receivedBuf, responseSender) -> {
                                        boolean fileExists = receivedBuf.readBoolean();

                                        // Load the command configuration from the JSON file
                                        JsonObject commandConfig = loadCommandConfig(server.getRunDirectory());
                                        String ifFileFoundCommand = commandConfig.get("ifFileFound").getAsString();
                                        String ifFileNotFoundCommand = commandConfig.get("ifFileNotFound").getAsString();

                                        // Execute the appropriate command based on file existence
                                        server.execute(() -> {
                                            try {
                                                if (fileExists) {
                                                    server.getCommandManager().getDispatcher().execute(
                                                            ifFileFoundCommand.replace("<player>", player.getName().getString()),
                                                            server.getCommandSource()
                                                    );
                                                } else {
                                                    server.getCommandManager().getDispatcher().execute(
                                                            ifFileNotFoundCommand.replace("<player>", player.getName().getString()),
                                                            server.getCommandSource()
                                                    );
                                                }
                                            } catch (Exception e) {
                                                LOGGER.error("Failed to execute command", e);
                                            }
                                        });
                                    });

=======
>>>>>>> f8a00a3 (Added command sending to client.)
                                    return 1;
                                })))
                .executes(context -> {
                    context.getSource().sendError(Text.of("Please specify a profile."));
                    return 0;
                })
        );
    }

<<<<<<< HEAD
    // Load the command configurations from JSON file
    private static JsonObject loadCommandConfig(File serverDir) {
        File configFile = new File(serverDir, "config/" + configFolderName + "/" + commandConfigFileName);
        try (FileReader reader = new FileReader(configFile)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            LOGGER.error("Failed to load command config file", e);
            return new JsonObject();  // Return empty object if file read fails
        }
    }
=======
>>>>>>> f8a00a3 (Added command sending to client.)
}
