package com.mcresurgence.config;

import com.mcresurgence.ResurgentPVPStats;
import net.minecraftforge.common.config.Configuration;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResurgentPVPStatsConfiguration {

    private static Configuration clientConfig;
    private static Configuration serverConfig;

    public static void init(File configDir) {
        String clientConfigFilename = "client.cfg";
        String serverConfigFilename = "server.cfg";

        Path clientConfigPath = Paths.get(configDir.toString(), ResurgentPVPStats.MODID, clientConfigFilename);

        Path serverConfigPath = Paths.get(configDir.toString(), ResurgentPVPStats.MODID, serverConfigFilename);

        // Load Client Configuration
        clientConfig = new Configuration(clientConfigPath.toFile());
        loadClientConfig();

        // Load Server Configuration
        serverConfig = new Configuration(serverConfigPath.toFile());
        loadServerConfig();
    }

    private static void loadClientConfig() {
        clientConfig.load();

        clientConfig.getBoolean("enableKillFeed", Configuration.CATEGORY_CLIENT, true, "Set to false to disable the kill feed on the client.");
        clientConfig.getInt("killFeedDuration", Configuration.CATEGORY_CLIENT, 5, 1, 20, "Duration in seconds that the kill feed should appear on screen.");

        if (clientConfig.hasChanged()) {
            clientConfig.save();
        }
    }

    private static void loadServerConfig() {
        serverConfig.load();

        serverConfig.getBoolean("showPlayerNametags", Configuration.CATEGORY_GENERAL, false, "Set to true to enable player nametags on the server.");

        if (serverConfig.hasChanged()) {
            serverConfig.save();
        }
    }

    public static boolean isKillFeedEnabled() {
        return clientConfig.getBoolean("enableKillFeed", Configuration.CATEGORY_CLIENT, true, "Check if the kill feed is enabled.");
    }

    public static int getKillFeedDurationMillis() {
        int seconds = clientConfig.getInt("killFeedDuration", Configuration.CATEGORY_CLIENT, 5, 1, 60, "Get the kill feed duration in seconds.");
        return seconds * 1000;  // Convert seconds to milliseconds
    }

    // Serverside, data is sent from the server when a player joins.
    public static boolean isShowNametagsEnabled() {
        return serverConfig.getBoolean("showPlayerNametags", Configuration.CATEGORY_GENERAL, false, "Check if nametags are shown.");
    }

    public static void setShowNametagsEnabled(boolean show) {
        serverConfig.get(Configuration.CATEGORY_GENERAL, "showPlayerNametags", false).set(show);
        if (serverConfig.hasChanged()) {
            serverConfig.save();
        }
    }
}
