package com.mcresurgence.scorekeeping;

import java.util.UUID;

public class PlayerKillInfo {
    private UUID killedUUID;
    private String killedName;
    private String weaponRegistryName;

    public PlayerKillInfo(UUID killedUUID, String killedName, String weaponRegistryName) {
        this.killedUUID = killedUUID;
        this.killedName = killedName;
        this.weaponRegistryName = weaponRegistryName;
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
}
