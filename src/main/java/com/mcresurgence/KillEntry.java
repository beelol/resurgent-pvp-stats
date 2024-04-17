package com.mcresurgence;

import net.minecraft.item.ItemStack;

import java.util.UUID;

public class KillEntry {
    private final String killer;
    private final ItemStack weapon;
    private final String killed;
    private final UUID killerUUID;
    private final UUID killedUUID;

    public KillEntry(String killer, ItemStack weapon, String killed, UUID killerUUID, UUID killedUUID) {
        this.killer = killer;
        this.weapon = weapon;
        this.killed = killed;
        this.killerUUID = killerUUID;
        this.killedUUID = killedUUID;
    }

    public String getKiller() {
        return killer;
    }

    public ItemStack getWeapon() {
        return weapon;
    }

    public String getKilled() {
        return killed;
    }

    public UUID getKillerUUID() {
        return killerUUID;
    }

    public UUID getKilledUUID() {
        return killedUUID;
    }
}
