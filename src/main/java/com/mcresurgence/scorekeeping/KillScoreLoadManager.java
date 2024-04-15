package com.mcresurgence.scorekeeping;

import java.io.*;
import java.nio.file.Files;
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
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

public class KillScoreLoadManager {
    private static Map<UUID, PlayerKillScoreEntry> playerKills = new HashMap<>();
    private static File dataFile;
    private static Logger logger;
    private static Gson gson = new Gson();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void init(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        dataFile = new File(event.getModConfigurationDirectory(), "kills.json");
        loadKills();
    }

    public static void recordKill(UUID killerUUID, PlayerKillInfo killInfo) {
        playerKills.computeIfAbsent(killerUUID, k -> new PlayerKillScoreEntry()).addKill(killInfo);
        saveKillsAsync();
    }

    public static void saveKillsAsync() {
        CompletableFuture.runAsync(() -> {
            try (Writer writer = Files.newBufferedWriter(Paths.get(dataFile.getPath()))) {
                gson.toJson(playerKills, writer);
            } catch (IOException e) {
                logger.error("Failed to save kills data asynchronously", e);
            }
        }, executor);
    }

    public static void loadKills() {
        if (!dataFile.exists()) {
            logger.info("No existing kills data found, starting fresh.");
            return;
        }

        try (Reader reader = Files.newBufferedReader(Paths.get(dataFile.getPath()))) {
            Type type = new TypeToken<Map<UUID, PlayerKillScoreEntry>>(){}.getType();
            playerKills = gson.fromJson(reader, type);
        } catch (IOException e) {
            logger.error("Failed to load kills data", e);
        }
    }
}
