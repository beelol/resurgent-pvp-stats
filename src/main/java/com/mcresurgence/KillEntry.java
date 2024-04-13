package com.mcresurgence;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class KillEntry {
    private final String killer;
    private final ItemStack weapon;
    private final String killed;
    private final long startTime;
    private final EntityPlayer killerEntity;
    private final EntityPlayer killedEntity;

    public KillEntry(String killer, ItemStack weapon, String killed, long startTime, EntityPlayer killerEntity, EntityPlayer killedEntity) {
        this.killer = killer;
        this.weapon = weapon;
        this.killed = killed;
        this.startTime = startTime;
        this.killerEntity = killerEntity;
        this.killedEntity = killedEntity;
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

    public long getStartTime() {
        return startTime;
    }

    public EntityPlayer getKillerEntity() {
        return killerEntity;
    }

    public EntityPlayer getKilledEntity() {
        return killedEntity;
    }
}
