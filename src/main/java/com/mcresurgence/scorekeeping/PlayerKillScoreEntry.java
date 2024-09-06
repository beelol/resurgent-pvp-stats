package com.mcresurgence.scorekeeping;

import java.util.ArrayList;
import java.util.List;

public class PlayerKillScoreEntry {
    private int killCount = 0;
    private List<PlayerKillInfo> kills;
    private String killerName;

    public PlayerKillScoreEntry(String killerName) {
        this.kills = new ArrayList<>();
        this.killerName = killerName;
    }

    public void addKill(PlayerKillInfo kill) {
        kills.add(kill);
        killCount++;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public List<PlayerKillInfo> getKills() {
        return kills;
    }

    public void setKills(List<PlayerKillInfo> kills) {
        this.kills = kills;
    }

    public String getKillerName() {
        return killerName;
    }

    public void setKillerName(String killerName) {
        this.killerName = killerName;
    }
}
