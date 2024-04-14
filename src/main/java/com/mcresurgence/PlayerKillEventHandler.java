package com.mcresurgence;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

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

//            if (event.getEntity() instanceof EntityPlayer) {
                Entity killed = event.getEntity();

                // Send packet to all clients
                NetworkHandler.INSTANCE.sendToAll(new DeathInfoPacket(killer.getUniqueID(), killer.getName(), killed.getUniqueID(), killed.getName(), weapon.getDisplayName()));
//            }
        }
    }
}

//KillDisplayOverlay.displayKillInfo(killer.getName(), weapon, killed.getName(), killer, killed);
