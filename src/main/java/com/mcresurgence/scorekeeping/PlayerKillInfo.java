package com.mcresurgence.scorekeeping;

import java.util.UUID;

public class PlayerKillInfo {
    private UUID killedUUID;
    private String killedName;
    private String weaponRegistryName;

    private UUID killerUUID;  // New field for killer's UUID
    private String killerName;  // New field for killer's name

    public PlayerKillInfo(UUID killedUUID, String killedName, String weaponRegistryName, UUID killerUUID, String killerName) {
        this.killedUUID = killedUUID;
        this.killedName = killedName;
        this.weaponRegistryName = weaponRegistryName;

        this.killerUUID = killerUUID;
        this.killerName = killerName;
    }

    public UUID getKilledUUID() {
        return killedUUID;
    }

    public void setKilledUUID(UUID killedUUID) {
        this.killedUUID = killedUUID;
    }

    public String getKilledName() {
        return killedName;
    }

    public void setKilledName(String killedName) {
        this.killedName = killedName;
    }

    public String getWeaponRegistryName() {
        return weaponRegistryName;
    }

    public void setWeaponRegistryName(String weaponRegistryName) {
        this.weaponRegistryName = weaponRegistryName;
    }

    public UUID getKillerUUID() {
        return killerUUID;
    }

    public void setKillerUUID(UUID killerUUID) {
        this.killerUUID = killerUUID;
    }

    public String getKillerName() {
        return killerName;
    }

    public void setKillerName(String killerName) {
        this.killerName = killerName;
    }
}
