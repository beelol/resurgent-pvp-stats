package com.mcresurgence.leaderboard;

import java.util.UUID;

public class LeaderboardEntry {
    private final UUID playerUUID;
    private final String playerName;
    private final int totalKills;

    public LeaderboardEntry(UUID playerUUID, String playerName, int totalKills) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;

        this.totalKills = totalKills;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getTotalKills() {
        return totalKills;
    }
}
