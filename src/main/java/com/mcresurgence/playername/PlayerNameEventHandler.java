package com.mcresurgence.playername;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mcresurgence.config.ResurgentPVPStatsConfiguration;

public class PlayerNameEventHandler {
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
        if (event.getEntity() instanceof EntityPlayer) {
//            EntityPlayer player = (EntityPlayer) event.getEntity();

            boolean shouldHideNametags = !ResurgentPVPStatsConfiguration.isShowNametagsEnabled();

            if (event.isCancelable() && shouldHideNametags) {
//                ResurgentPVPStats.modLogger.info("Cancelling nametag render for player: " + player.getName());
                event.setCanceled(true);
            } else if (!shouldHideNametags) {
//                ResurgentPVPStats.modLogger.info("Nametag render permitted for player: " + player.getName());
            }
        }
    }
}