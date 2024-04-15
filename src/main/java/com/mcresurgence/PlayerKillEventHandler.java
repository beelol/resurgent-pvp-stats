package com.mcresurgence;

import com.mcresurgence.scorekeeping.KillScoreLoadManager;
import com.mcresurgence.scorekeeping.PlayerKillInfo;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

//@Mod.EventBusSubscriber(modid = ResurgentPVPStats.MODID)
public class PlayerKillEventHandler {
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {

        ModLogger logger = ResurgentPVPStats.modLogger;

        logger.info("LivingDeathEvent fired.");

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
            ItemStack weapon = killer.getHeldItemMainhand();

            Entity killedEntity = event.getEntity();
            logger.info("Player " + killer.getName() + " killed " + killedEntity.getName() + " using " + weapon.getDisplayName());

            if (killedEntity instanceof EntityPlayer) {
                Entity killed = event.getEntity();

                PlayerKillInfo killInfo = new PlayerKillInfo(killed.getUniqueID(), killed.getName(), weapon.getItem().getRegistryName().toString());

                // Record the kill in the KillScoreLoadManager
                KillScoreLoadManager.recordKill(killer.getUniqueID(), killInfo);

                try {
                    NetworkHandler.INSTANCE.sendToAll(new DeathInfoPacket(killer.getUniqueID(), killer.getName(), killed.getUniqueID(), killed.getName(), weapon.getItem().getRegistryName().toString()));
                } catch (Exception e) {
//                throw new RuntimeException(e);

                    logger.warn("Failed to trigger kill feed entry for the following: Player " + killer.getName() + " killed " + killedEntity.getName() + " using " + weapon.getDisplayName());
                }
            }
        }
    }
}

//KillDisplayOverlay.displayKillInfo(killer.getName(), weapon, killed.getName(), killer, killed);
