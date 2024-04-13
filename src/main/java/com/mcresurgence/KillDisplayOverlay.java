package com.mcresurgence;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class KillDisplayOverlay extends Gui {
    private final Minecraft minecraft;
    private static final List<KillEntry> killEntries = new ArrayList<>();

    public KillDisplayOverlay(Minecraft mc) {
        this.minecraft = mc;
    }

    public static void displayKillInfo(String killer, ItemStack weapon, String killed, EntityPlayer killerEntity, EntityPlayer killedEntity) {
        killEntries.add(new KillEntry(killer, weapon, killed, System.currentTimeMillis(), killerEntity, killedEntity));
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        Logger logger = ResurgentPVPStats.LOGGER;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            logger.info("Rendering game overlay");  // Add this for testing
            drawOverlay();
        }
    }

    private void drawOverlay() {
        int yPos = 10;
        Iterator<KillEntry> iterator = killEntries.iterator();
        while (iterator.hasNext()) {
            KillEntry entry = iterator.next();
            if (System.currentTimeMillis() - entry.getStartTime() > 5000) {
                iterator.remove();
                continue;
            }

            int xPos = 10;
            renderPlayerHead(xPos, yPos, entry.getKillerEntity());
            xPos += 24;
            minecraft.fontRenderer.drawStringWithShadow(entry.getKiller(), xPos, yPos, 0xffffff);

            xPos += 100;
            GlStateManager.pushMatrix();
            GlStateManager.translate(xPos, yPos, 0);
            minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.getWeapon(), 0, 0);
            GlStateManager.popMatrix();

            xPos += 50;
            renderPlayerHead(xPos, yPos, entry.getKilledEntity());
            xPos += 24;
            minecraft.fontRenderer.drawStringWithShadow(entry.getKilled(), xPos, yPos, 0xffffff);

            yPos += minecraft.fontRenderer.FONT_HEIGHT + 30;
        }
    }

    private void renderPlayerHead(int x, int y, EntityPlayer player) {
        if (player == null) {
            return;  // Early exit if the player object is null
        }

        ResourceLocation skinLocation = Minecraft.getMinecraft().getSkinManager().loadSkin(new MinecraftProfileTexture(player.getGameProfile().getProperties().get("textures").iterator().next().getValue(), null), MinecraftProfileTexture.Type.SKIN);

        if (skinLocation == null) {
            // If there's no custom skin, use the default skin based on player UUID
            skinLocation = DefaultPlayerSkin.getDefaultSkin(player.getUniqueID());
        }


        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(16.0F, 16.0F, 16.0F); // Scale up the texture to the appropriate size for display
        Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
        drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 8, 8);
        GlStateManager.popMatrix();
    }
}
