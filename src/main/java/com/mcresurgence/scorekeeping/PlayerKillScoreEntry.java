package com.mcresurgence.scorekeeping;

import java.util.ArrayList;
import java.util.List;

public class PlayerKillScoreEntry {
    private int killCount;
    private List<PlayerKillInfo> kills;

    public PlayerKillScoreEntry() {
        this.kills = new ArrayList<>();
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
}
