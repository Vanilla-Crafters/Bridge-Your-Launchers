package net.vanillacrafters.bridgeyourlaunchers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Logger;
import com.mojang.brigadier.ParseResults;


public class BridgeConfigHelper {

    private static final Logger LOGGER = Logger.getLogger("BridgeConfigHelper");

    public static void createConfigFile(String configFileName, String profilesFolderName) {
        File configFile = new File(configFileName);
        File profilesFolder = new File("config/bridgeyourlaunchers/" + profilesFolderName);

        if (!profilesFolder.exists()) {
            profilesFolder.mkdirs();
            LOGGER.info("Profiles folder created at: " + profilesFolder.getAbsolutePath());
        }

        if (!configFile.exists()) {
            try {
                JsonObject defaultConfig = new JsonObject();
                defaultConfig.addProperty("yes", "say yes");
                defaultConfig.addProperty("no", "say no");

                try (FileWriter writer = new FileWriter(configFile)) {
                    writer.write(defaultConfig.toString());
                    LOGGER.info("Config file created at: " + configFile.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.severe("Failed to create config file: " + e.getMessage());
            }
        }
    }

    public static void executeCommandsFromJson(ServerCommandSource source, String key, String configFileName) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(configFileName)));
            JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();

            if (jsonObject.has(key)) {
                String command = jsonObject.get(key).getAsString();
                LOGGER.info("Executing command for key: " + key + " - " + command);

                // Check for empty or invalid command
                if (command == null || command.trim().isEmpty()) {
                    LOGGER.severe("Command is empty or null.");
                    return;
                }

                // Modify the command to execute as the player who used the /bridge command
                String playerSpecificCommand = "execute as @s at @s run " + command;

                try {
                    CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();

                    // Parse and execute the modified command
                    ParseResults<ServerCommandSource> parseResults = dispatcher.parse(playerSpecificCommand, source);
                    LOGGER.info("ParseResults: " + parseResults.getContext().toString());

                    if (parseResults.getContext().getNodes().isEmpty()) {
                        LOGGER.severe("Command syntax error: Invalid command format or empty command.");
                        return;
                    }

                    // Execute the command specifically as the player
                    int result = dispatcher.execute(parseResults);
                    LOGGER.info("Command executed with result: " + result);

                } catch (CommandSyntaxException e) {
                    LOGGER.severe("Command syntax error: " + e.getMessage());
                }

            } else {
                LOGGER.warning("No command found for key: " + key);
            }

        } catch (IOException e) {
            LOGGER.severe("Failed to read config file: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            LOGGER.severe("Invalid JSON syntax in config file: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error executing command from config file: " + e.getMessage());
        }
    }




    public static Optional<File> findUrlFile(File profileDir) {
        if (profileDir.exists() && profileDir.isDirectory()) {
            File[] files = profileDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".url")) {
                        return Optional.of(file);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
