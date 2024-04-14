package com.mcresurgence;

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

import java.util.*;

public class KillDisplayOverlay extends Gui {
    private final Minecraft minecraft;
    private static final List<KillEntry> killEntries = new ArrayList<>();
    private static final long FADE_DELAY = 5000; // 5 seconds delay before starting to fade

    // For fade effect
    private long lastTickTime = 0;
    private static Map<KillEntry, Float> alphaMap = new HashMap<>();
    private static Map<KillEntry, Long> startTimeMap = new HashMap<>();

    public KillDisplayOverlay(Minecraft mc) {
        this.minecraft = mc;
    }

    private int getTextColorFromAlpha(float alpha) {
        int alphaInt = (int) (alpha * 255);
        return (alphaInt << 24) | 0xffffff;  // White with variable alpha
    }

    public static void displayKillInfo(String killer, ItemStack weapon, String killed, EntityPlayer killerEntity, Entity killedEntity) {
        KillEntry entry = new KillEntry(killer, weapon, killed, System.currentTimeMillis(), killerEntity, killedEntity);

        killEntries.add(entry);

        alphaMap.put(entry, 1.0f);

        startTimeMap.put(entry, System.currentTimeMillis());  // Capture the start time
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            drawOverlay();
        }
    }

    private void drawOverlay() {
        long currentTime = System.currentTimeMillis();
        long deltaTime = (lastTickTime > 0) ? currentTime - lastTickTime : 0;
        lastTickTime = currentTime;

        float alphaFadePerSecond = 0.5F;
        float fadeRate = alphaFadePerSecond / 1000;  // Fade rate per millisecond

        int baseYPos = 15; // Base vertical position for the first entry
        int paddingY = minecraft.fontRenderer.FONT_HEIGHT + 15; // Vertical spacing between entries

        Iterator<KillEntry> iterator = killEntries.iterator();
        int index = 0; // Initialize an index counter for spacing calculation

        while (iterator.hasNext()) {
            KillEntry entry = iterator.next();
            Long startTime = startTimeMap.getOrDefault(entry, currentTime);
            float alpha = alphaMap.getOrDefault(entry, 1.0f);

            if (currentTime - startTime > FADE_DELAY) {
                alpha = Math.max(0, alpha - fadeRate * (currentTime - lastTickTime));
                alphaMap.put(entry, alpha);

                if (alpha <= 0) {
                    iterator.remove();
                    alphaMap.remove(entry);
                    startTimeMap.remove(entry);
                    continue;
                }
            }

            int yPos = baseYPos + paddingY * index; // Calculate yPos based on the current index

            drawEntry(entry, yPos, alpha);
            index++; // Increment the index only if the entry is not removed
        }
    }

    private void drawEntry(KillEntry entry, int yPos, float alpha) {
        int rectX = 8;
        int rectY = yPos - 3;
        int entryWidth = 250; // Estimate the width to cover icon + text
        int entryHeight = 20; // Estimate the height to cover the text and icon

        float maxBGAlpha = 255.0F / 2.0F;
        int backgroundColor = ((int) (alpha * maxBGAlpha) << 24) | 0x000000;  // Apply alpha to background color

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        drawRect(rectX, rectY, rectX + entryWidth, rectY + entryHeight, backgroundColor);

        int xPos = 16;
        renderPlayerHead(xPos, yPos, entry.getKillerEntity());
        xPos += 20;

        int textYOffset = yPos + 8 - minecraft.fontRenderer.FONT_HEIGHT / 2;

        displayText(entry.getKiller(), xPos, textYOffset, alpha);

        xPos += 90;
        GlStateManager.pushMatrix();
        GlStateManager.translate(xPos, yPos, 0);
        minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.getWeapon(), 0, 0);
        GlStateManager.popMatrix();

        xPos += 30;
        if (entry.getKilledEntity() instanceof EntityPlayer) {
            EntityPlayer killedPlayer = (EntityPlayer) entry.getKilledEntity();
            renderPlayerHead(xPos, yPos, killedPlayer);
        }

        xPos += 12;
        displayText(entry.getKilled(), xPos, textYOffset, alpha);

        GlStateManager.disableBlend();
    }

    private void displayText(String entry, int xPos, int textYOffset, float alpha) {
        minecraft.fontRenderer.drawStringWithShadow(entry, xPos, textYOffset, getTextColorFromAlpha(alpha));
    }

    private void renderPlayerHead(int x, int y, EntityPlayer player) {
        int headSize = 8;
        ModLogger logger = ResurgentPVPStats.modLogger;

        logger.info("Attempting to renderPlayerHead");

        if (player == null) {
            return;
        }

        logger.info("Player is not null.");


        if (player.getGameProfile() == null) {
            logger.info("player.getGameProfile() is null");

            return;
        }

        logger.info("player.getGameProfile() is not null");


        Collection<com.mojang.authlib.properties.Property> properties =
                player.getGameProfile().getProperties().get("textures");

        if (properties == null || properties.isEmpty()) {
            return;  // Exit if there are no texture properties
        }

        logger.info( String.format("Player [%s] has texture properties.", player.getDisplayName()));

//        Property property = properties.iterator().next(); // Safely access the first property
//
//        if (property == null) {
//            return;  // Exit if the property is null
//        }

        ResourceLocation skinLocation = null;

        if (Minecraft.getMinecraft().player.getGameProfile().getId() == player.getGameProfile().getId()) {
            logger.info( String.format("This player head is the same as the minecraft gameprofile ID, using SP player's skin"));

            skinLocation = Minecraft.getMinecraft().player.getLocationSkin();
        }

//        ResourceLocation skinLocation = Minecraft.getMinecraft().getSkinManager().loadSkin(new MinecraftProfileTexture(property.getValue(), null), MinecraftProfileTexture.Type.SKIN);

        if (skinLocation == null) {
            // If there's no custom skin, use the default skin based on player UUID
            skinLocation = DefaultPlayerSkin.getDefaultSkin(player.getUniqueID());
            logger.info("Player skinLocation is null.");
        } else {
            logger.info("Found player's skin location.");
        }
//
//        if (player.getGameProfile() != null) {
//            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(player.getGameProfile());
//
//            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
//                skinLocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
//
//                logger.info("Skin loaded: " + skinLocation.toString());
//            } else {
//                logger.info("Default skin used.");
//            }
//        }




        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
        GlStateManager.translate(x, y + headSize / 2 , 0);

        GlStateManager.scale(1.0F, 1.0F, 1.0F);

        drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);

        GlStateManager.popMatrix();
    }
}
