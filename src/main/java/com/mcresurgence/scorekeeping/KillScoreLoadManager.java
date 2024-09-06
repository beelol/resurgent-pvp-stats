package com.mcresurgence.scorekeeping;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import com.mcresurgence.ResurgentPVPStats;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class KillScoreLoadManager {
    private static Map<UUID, PlayerKillScoreEntry> playerKills = new HashMap<>();
    private static File dataFile;
    private static Gson gson = new Gson();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static Map<UUID, PlayerKillScoreEntry> getPlayerKills() {
        return playerKills;
    }

    public static void preInit(File configDir) {
        ResurgentPVPStats.modLogger.info("Initializing KillScoreLoadManager");
        String filename = "player-kills.json";

        Path path = Paths.get(configDir.toString(), ResurgentPVPStats.MODID, filename);

        // Ensure the directory exists
        if (Files.notExists(path.getParent())) {
            try {
                ResurgentPVPStats.modLogger.info("[KillScoreLoadManager] Parent directory exists.");
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                ResurgentPVPStats.modLogger.error("Failed to create directories: " + e.getMessage(), e);
                return;
            }
        }

        // Create the file if it does not exist
        if (Files.notExists(path)) {
            try {
                Files.createFile(path);

                ResurgentPVPStats.modLogger.info("[KillScoreLoadManager] Parent did not exist, successfully created it.");
            } catch (IOException e) {
                ResurgentPVPStats.modLogger.error("Failed to create the file: " + e.getMessage(), e);
                return;
            }
        }

        dataFile = path.toFile();

        loadKills();
    }

    public static void recordKill(PlayerKillInfo killInfo) {
        if (playerKills == null) {
            ResurgentPVPStats.modLogger.error("Attempted to record a kill before playerKills was initialized.");
            return;
        }

        String killerName = killInfo.getKillerName();
        UUID killerUUID = killInfo.getKillerUUID();

        ResurgentPVPStats.modLogger.info("Recording kill...");

        try {
            playerKills.computeIfAbsent(killerUUID, k -> new PlayerKillScoreEntry(killerName)).addKill(killInfo);
            saveKillsAsync();
        } catch (Exception e) {
            ResurgentPVPStats.modLogger.error(String.format("[KillScoreLoadManager] could not record kill. Error: %s", e.getMessage()), e);

            ResurgentPVPStats.modLogger.info(String.format("Killer UUID: %s", killerUUID.toString()));
            ResurgentPVPStats.modLogger.info(String.format("killInfo: %s", killInfo.toString()));
        }
    }

    public static void saveKillsAsync() {
        CompletableFuture.runAsync(() -> {
            try (Writer writer = Files.newBufferedWriter(Paths.get(dataFile.getPath()))) {
                gson.toJson(playerKills, writer);
            } catch (IOException e) {
                ResurgentPVPStats.modLogger.error("Failed to save kills data", e);
            }
        }, executor);
    }

    public static void loadKills() {
        if (!dataFile.exists()) {
            ResurgentPVPStats.modLogger.info("No existing kills data found, starting fresh.");
            return;
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(dataFile.getPath()))) {
            Type type = new TypeToken<Map<UUID, PlayerKillScoreEntry>>() {
            }.getType();

            Map<UUID, PlayerKillScoreEntry> loadedKills = gson.fromJson(reader, type);

            if (loadedKills != null) {
                playerKills = loadedKills;

                ResurgentPVPStats.modLogger.warn("Successfully loaded kills data.");
            } else {
                ResurgentPVPStats.modLogger.warn("Kills data file was empty or corrupt. Starting with an empty map.");
                playerKills = new HashMap<>();  // Initialize to an empty map to avoid null
            }
        } catch (IOException e) {
            ResurgentPVPStats.modLogger.error("Failed to load kills data", e);
        }
    }
}
