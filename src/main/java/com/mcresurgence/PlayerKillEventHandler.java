package com.mcresurgence;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ResurgentPVPStats.MODID)
public class PlayerKillEventHandler {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Logger logger = ResurgentPVPStats.LOGGER;

        if (event.getSource().getTrueSource() instanceof EntityPlayer) {

            EntityPlayer killer = (EntityPlayer) event.getSource().getTrueSource();
            if (event.getEntity() instanceof EntityPlayer) {
                EntityPlayer killed = (EntityPlayer) event.getEntity();
                ItemStack weapon = killer.getHeldItemMainhand();
                logger.info("Player " + killer.getName() + " killed " + killed.getName() + " using " + weapon.getDisplayName());

                KillDisplayOverlay.displayKillInfo(killer.getName(), weapon, killed.getName(), killer, killed);
            }
        }
    }
}
