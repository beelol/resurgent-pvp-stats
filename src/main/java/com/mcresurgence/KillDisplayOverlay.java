package com.mcresurgence;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

public class KillDisplayOverlay extends Gui {
    private final Minecraft minecraft;
    private static final List<KillEntry> killEntries = new ArrayList<>();

    public KillDisplayOverlay(Minecraft mc) {
        this.minecraft = mc;
    }

    public static void displayKillInfo(String killer, ItemStack weapon, String killed, EntityPlayer killerEntity, Entity killedEntity) {
        killEntries.add(new KillEntry(killer, weapon, killed, System.currentTimeMillis(), killerEntity, killedEntity));
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        ModLogger logger = ResurgentPVPStats.modLogger;

        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
//            logger.info("Rendering game overlay");
            drawOverlay();
        }
    }

    private void drawOverlay() {
        int baseYPos = 10; // Base vertical position for the first entry
        int paddingY = minecraft.fontRenderer.FONT_HEIGHT + 30; // Vertical spacing between entries

        Iterator<KillEntry> iterator = killEntries.iterator();
        int index = 0; // Initialize an index counter for spacing calculation

        while (iterator.hasNext()) {
            KillEntry entry = iterator.next();

            if (System.currentTimeMillis() - entry.getStartTime() > 5000) {
                iterator.remove(); // Remove the entry using Iterator's remove method
                continue; // Skip the rest of the loop for this iteration
            }

            int yPos = baseYPos + paddingY * index; // Calculate yPos based on the current index

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

            if (entry.getKilledEntity() instanceof EntityPlayer) {
                EntityPlayer killedPlayer = (EntityPlayer) entry.getKilledEntity();
                renderPlayerHead(xPos, yPos, killedPlayer);
            }

            xPos += 24;
            minecraft.fontRenderer.drawStringWithShadow(entry.getKilled(), xPos, yPos, 0xffffff);

            index++; // Increment the index only if the entry is not removed
        }
    }

    private void renderPlayerHead(int x, int y, EntityPlayer player) {
        if (player == null) {
            return;  // Early exit if the player object is null
        }

        if (player == null || player.getGameProfile() == null) {
            return;  // Early exit if the player object or profile is null
        }

        Collection<com.mojang.authlib.properties.Property> properties =
                player.getGameProfile().getProperties().get("textures");

        if (properties == null || properties.isEmpty()) {
            return;  // Exit if there are no texture properties
        }

        Property property = properties.iterator().next(); // Safely access the first property

        if (property == null) {
            return;  // Exit if the property is null
        }

        ResourceLocation skinLocation = Minecraft.getMinecraft().getSkinManager().loadSkin(new MinecraftProfileTexture(property.getValue(), null), MinecraftProfileTexture.Type.SKIN);

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
