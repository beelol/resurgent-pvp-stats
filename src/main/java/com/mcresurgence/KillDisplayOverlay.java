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
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            drawOverlay();
        }
    }

    private void drawOverlay() {
        int yPos = 10;
        Iterator<KillEntry> iterator = killEntries.iterator();
        while (iterator.hasNext()) {
            KillEntry entry = iterator.next();
            if (System.currentTimeMillis() - entry.getStartTime() > 5000) { // 5 seconds
                iterator.remove();
            } else {
                int xPos = 10;
                // Render killer's face
                renderPlayerHead(xPos, yPos, entry.getKillerEntity());
                xPos += 24;
                minecraft.fontRenderer.drawStringWithShadow("Killer: " + entry.getKiller(), xPos, yPos, 0xffffff);

                // Render item icon
                GlStateManager.pushMatrix();
                GlStateManager.translate(xPos + 100, yPos, 0); // Adjust X, Y positioning to not overlap text
                GlStateManager.scale(1.0F, 1.0F, 1.0F);
                minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.getWeapon(), 0, 0);
                GlStateManager.popMatrix();

                // Render killed player's face
                xPos += 150;
                renderPlayerHead(xPos, yPos, entry.getKilledEntity());
                xPos += 24;
                minecraft.fontRenderer.drawStringWithShadow("killed: " + entry.getKilled(), xPos, yPos, 0xffffff);

                yPos += 24 + minecraft.fontRenderer.FONT_HEIGHT + 10;
            }
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
