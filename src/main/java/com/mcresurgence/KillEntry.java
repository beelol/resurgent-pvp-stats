package com.mcresurgence;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class KillEntry {
    private final String killer;
    private final ItemStack weapon;
    private final String killed;
    private final EntityPlayer killerEntity;
    private final Entity killedEntity;

    public KillEntry(String killer, ItemStack weapon, String killed, EntityPlayer killerEntity, Entity killedEntity) {
        this.killer = killer;
        this.weapon = weapon;
        this.killed = killed;
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

    public EntityPlayer getKillerEntity() {
        return killerEntity;
    }

    public Entity getKilledEntity() {
        return killedEntity;
    }
}
