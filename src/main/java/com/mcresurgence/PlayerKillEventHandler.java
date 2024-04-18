package com.mcresurgence;

import com.mcresurgence.scorekeeping.KillScoreLoadManager;
import com.mcresurgence.scorekeeping.PlayerKillInfo;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class PlayerKillEventHandler {
    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {

        ModLogger logger = ResurgentPVPStats.modLogger;

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {
            EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
            ItemStack weapon = killer.getHeldItemMainhand();

            Entity killedEntity = event.getEntity();
            logger.info(String.format("Player %s killed %s using %s", killer.getName(), killedEntity.getName(), weapon.getDisplayName()));

            if (killedEntity instanceof EntityPlayer) {

                EntityPlayer killedPlayer = (EntityPlayer) killedEntity;


                PlayerKillInfo killInfo = new PlayerKillInfo(killedEntity.getUniqueID(), killedPlayer.getName(), weapon.getItem().getRegistryName().toString());

                KillScoreLoadManager.recordKill(killer.getUniqueID(), killInfo);

                logger.info(String.format("Killed entity %s was a player, sending info.", killer.getName(), killedEntity.getName(), weapon.getDisplayName()));

                try {
                    NetworkHandler.INSTANCE.sendToAll(new DeathInfoPacket(
                            killer.getUniqueID(),
                            killer.getName(),
                            killedPlayer.getUniqueID(),
                            killedPlayer.getName(),
                            weapon.getItem().getRegistryName().toString()));
                } catch (Exception e) {
                    logger.error(String.format("Failed to trigger kill feed entry for the following: Player %s killed %s using %s", killer.getName(), killedPlayer.getName(), weapon.getDisplayName()), e);
                }
            }
        }
    }
}

//KillDisplayOverlay.displayKillInfo(killer.getName(), weapon, killed.getName(), killer, killed);
