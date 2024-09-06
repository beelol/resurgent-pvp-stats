package com.mcresurgence.leaderboard;

import com.mcresurgence.scorekeeping.KillScoreLoadManager;
import com.mcresurgence.scorekeeping.PlayerKillScoreEntry;

import java.util.*;

public class LeaderboardManager {

    private static final int PLAYERS_PER_PAGE = 10;
    private final List<LeaderboardEntry> leaderboardEntries;

    public LeaderboardManager() {
        KillScoreLoadManager.loadKills(); // Ensure data is loaded
        this.leaderboardEntries = transformPlayerKillsToLeaderboardEntries();
    }

    public List<LeaderboardEntry> getAllEntries() {
        return leaderboardEntries;
    }

    // Method to get players for a specific page
    public List<LeaderboardEntry> getPlayersForPage(int page) {
        int start = page * PLAYERS_PER_PAGE;
        int end = Math.min(start + PLAYERS_PER_PAGE, leaderboardEntries.size());

        if (start >= leaderboardEntries.size()) {
            return Collections.emptyList();
        }

        return leaderboardEntries.subList(start, end);
    }

    // Method to get total pages
    public int getTotalPages() {
        return (int) Math.ceil((double) leaderboardEntries.size() / PLAYERS_PER_PAGE);
    }

    // Function to transform the map into a sorted list of LeaderboardEntry objects
    private List<LeaderboardEntry> transformPlayerKillsToLeaderboardEntries() {
        List<LeaderboardEntry> entries = new ArrayList<>();

        // Access the playerKills map from KillScoreLoadManager
        Map<UUID, PlayerKillScoreEntry> playerKills = KillScoreLoadManager.getPlayerKills();

        // Convert map entries to LeaderboardEntry objects
        for (Map.Entry<UUID, PlayerKillScoreEntry> entry : playerKills.entrySet()) {
            UUID playerUUID = entry.getKey();
            String playerName = entry.getValue().getKillerName();

            int totalKills = entry.getValue().getKillCount();

            entries.add(new LeaderboardEntry(playerUUID, playerName, totalKills));
        }

        // Sort the list by total kills in descending order
        entries.sort(Comparator.comparingInt(LeaderboardEntry::getTotalKills).reversed());

        return entries;
    }
}
