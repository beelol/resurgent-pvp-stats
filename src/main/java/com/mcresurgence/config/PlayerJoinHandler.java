package com.mcresurgence.config;

import com.mcresurgence.NetworkHandler;
import com.mcresurgence.ResurgentPVPStats;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PlayerJoinHandler {
    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) { // Ensure the player is the correct instance
            EntityPlayerMP playerMP = (EntityPlayerMP) event.player;
            boolean showNametags = ResurgentPVPStatsConfiguration.isShowNametagsEnabled();

            ResurgentPVPStats.modLogger.info("Sending showNametags config to " + playerMP.getName() + ": " + showNametags);

            NetworkHandler.INSTANCE.sendTo(new ConfigSyncPacket(showNametags), playerMP);
        } else {
            ResurgentPVPStats.modLogger.warn("PlayerLoggedInEvent triggered by non-EntityPlayerMP instance");
        }
    }
}
