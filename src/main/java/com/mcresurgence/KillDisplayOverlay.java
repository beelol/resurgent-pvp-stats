package com.mcresurgence;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KillDisplayOverlay extends Gui {
    private final Minecraft minecraft;
    private static final List<KillEntry> killEntries = new ArrayList<>();
    private static final long FADE_DELAY = 5000; // 5 seconds delay before starting to fade
    private static final int headSize = 8; // 5 seconds delay before starting to fade
    private static final int itemWidth = 16;

    // For fade effect
    private long lastTickTime = 0;
    private static Map<KillEntry, Float> alphaMap = new HashMap<>();
    private static Map<KillEntry, Long> startTimeMap = new HashMap<>();
    private static final Map<KillEntry, Float> yPosMap = new HashMap<>();
    private static final Map<UUID, ResourceLocation> uuidToSkinMap = new HashMap<>();

    private static final Map<UUID, CompletableFuture<ResourceLocation>> loadingFutures = new HashMap<>();


    public KillDisplayOverlay(Minecraft mc) {
        this.minecraft = mc;
    }

    private int getTextColorFromAlpha(float alpha) {
        int alphaInt = (int) (alpha * 255);
        return (alphaInt << 24) | 0xffffff;  // White with variable alpha
    }

    private static CompletableFuture<ResourceLocation> loadSkin(UUID playerUUID) {
        return loadingFutures.computeIfAbsent(playerUUID, uuid -> {
            CompletableFuture<ResourceLocation> skinLoadFuture = new CompletableFuture<>();
            SkinManager skinManager = Minecraft.getMinecraft().getSkinManager();
            GameProfile profile = new GameProfile(playerUUID, null);

            skinManager.loadProfileTextures(profile, (type, resourceLocation, profileTexture) -> {
                if (type == MinecraftProfileTexture.Type.SKIN) {
                    ResurgentPVPStats.modLogger.info(String.format("Successfully loaded skin for player %s. Inserting into map.", playerUUID));

                    skinLoadFuture.complete(resourceLocation);
                } else {
                    ResurgentPVPStats.modLogger.info(String.format("Failed to load skin for player %s. Using default skin.", playerUUID));

                    skinLoadFuture.complete(DefaultPlayerSkin.getDefaultSkin(uuid));
                }
            }, true);

            
            return skinLoadFuture.handle((loadedSkin, exception) -> {
                if (exception != null) {
                    ResurgentPVPStats.modLogger.error(String.format("Error loading skin for player %s: %s", playerUUID, exception.getMessage()));
                    loadedSkin = DefaultPlayerSkin.getDefaultSkin(uuid); // Use default skin on error
                }

                uuidToSkinMap.put(uuid, loadedSkin);
                loadingFutures.remove(uuid); // Ensure cleanup from map whether successful or failed
                return loadedSkin;
            });
        });
    }

    public static void displayKillInfo(String killer, ItemStack weapon, String killed, UUID killerUUID, UUID killedUUID) {
        CompletableFuture<ResourceLocation> killerSkinLoaded = loadSkin(killerUUID);
        CompletableFuture<ResourceLocation> killedSkinLoaded = loadSkin(killedUUID);

        CompletableFuture.allOf(killerSkinLoaded, killedSkinLoaded).thenRun(() -> {
            addKillEntry(killer, weapon, killed, killerUUID, killedUUID);
        });
    }

    private static void addKillEntry(String killer, ItemStack weapon, String killed, UUID killerUUID, UUID killedUUID) {
        KillEntry entry = new KillEntry(killer, weapon, killed, killerUUID, killedUUID);

        killEntries.add(entry);
        alphaMap.put(entry, 1.0f);
        startTimeMap.put(entry, System.currentTimeMillis());
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

        float moveRatePerSecond = 10;
        float moveDistancePerTick = moveRatePerSecond * (deltaTime / 1000.0f);

        float alphaFadePerSecond = 0.5F;
        float fadeRate = alphaFadePerSecond / 1000;

        int baseYPos = 15;
        int paddingY = minecraft.fontRenderer.FONT_HEIGHT + 15;

        Iterator<KillEntry> iterator = killEntries.iterator();
        int index = 0;

        while (iterator.hasNext()) {
            KillEntry entry = iterator.next();

            float targetYPos = baseYPos + paddingY * index;

            float currentYPos = yPosMap.getOrDefault(entry, targetYPos);

            if (currentYPos > targetYPos) {
                currentYPos = Math.max(targetYPos, currentYPos - moveDistancePerTick);
                yPosMap.put(entry, currentYPos);
            }

            Long startTime = startTimeMap.getOrDefault(entry, 0L);
            float alpha = alphaMap.getOrDefault(entry, 1.0f);

            if (startTimeMap.containsKey(entry) && currentTime - startTime > FADE_DELAY) {
                alpha = Math.max(0, alpha - fadeRate * deltaTime);
                alphaMap.put(entry, alpha);

                if (alpha <= 0) {
                    alphaMap.remove(entry);
                    startTimeMap.remove(entry);
                    yPosMap.remove(entry);
                    iterator.remove();
                    continue;
                }
            }

            drawEntry(entry, (int) currentYPos, alpha);
            index++; // Increment the index only if the entry is not removed
        }
    }

    private void drawEntry(KillEntry entry, int yPos, float alpha) {
        int entryPosStart = 8;
        int rectY = yPos - 3;
        int entryWidth = 250; // Estimate the width to cover icon + text
        int entryHeight = 20; // Estimate the height to cover the text and icon
        int paddingHeadToText = 4;

        int entryStartX = entryPosStart;
        int entryEndX = entryPosStart + entryWidth;

        int centerX = (entryStartX + entryEndX) / 2;
        int itemPositionX = centerX - itemWidth / 2;

        int entryPaddingX = 16;

        float maxBGAlpha = 255.0F / 2.0F;
        int backgroundColor = ((int) (alpha * maxBGAlpha) << 24) | 0x000000;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        drawRect(entryPosStart, rectY, entryPosStart + entryWidth, rectY + entryHeight, backgroundColor);

        int xPos = entryPaddingX;
        int textYOffset = yPos + 8 - minecraft.fontRenderer.FONT_HEIGHT / 2;

        renderPlayerHead(xPos, yPos, entry.getKillerUUID());
        xPos += headSize + paddingHeadToText;
        displayText(entry.getKiller(), xPos, textYOffset, alpha);

        int endX = entryWidth + entryPosStart - entryPaddingX;

        GlStateManager.pushMatrix();
        GlStateManager.translate(itemPositionX, yPos, 0);

        minecraft.getRenderItem().renderItemAndEffectIntoGUI(entry.getWeapon(), 0, 0);

        GlStateManager.popMatrix();

        int killedTextWidth = minecraft.fontRenderer.getStringWidth(entry.getKilled());

        int endTextX = endX - killedTextWidth;
        int endHeadX = endTextX - headSize - paddingHeadToText;

        renderPlayerHead(endHeadX, yPos, entry.getKilledUUID());

        displayText(entry.getKilled(), endTextX, textYOffset, alpha);

        GlStateManager.disableBlend();
    }

    private void displayText(String entry, int xPos, int textYOffset, float alpha) {
        minecraft.fontRenderer.drawStringWithShadow(entry, xPos, textYOffset, getTextColorFromAlpha(alpha));
    }

    private void renderPlayerHead(int x, int y, UUID playerId) {
        ModLogger logger = ResurgentPVPStats.modLogger;

        logger.info("Attempting to renderPlayerHead");

        if (playerId == null) {
            return;
        }

        logger.info("Player is not null.");

//        Collection<com.mojang.authlib.properties.Property> properties =
//                playerId.getGameProfile().getProperties().get("textures");
//
//        if (properties == null || properties.isEmpty()) {
//            logger.info(String.format("Player [%s] has no texture properties. Continuing regardless.", playerId.getDisplayName()));
//
//        } else {
//            logger.info(String.format("Player [%s] has texture properties. We could use one.", playerId.getDisplayName()));
//        }

        ResourceLocation skinLocation = uuidToSkinMap.get(playerId);
//
//        if (Minecraft.getMinecraft().player.getGameProfile().getId() == playerId.getGameProfile().getId()) {
//            logger.info(String.format("Player %s is the same as the minecraft gameprofile ID, using SP player's skin", playerId.getDisplayName()));
//
//            skinLocation = Minecraft.getMinecraft().player.getLocationSkin();
//        } else {
//            logger.info(String.format("Player %s is not the user on this client, trying next method.", playerId.getDisplayName()));
//        }


//        ResourceLocation skinLocation =
//        Minecraft.getMinecraft().getSkinManager().loadSkin(new MinecraftProfileTexture(property.getValue(), null), MinecraftProfileTexture.Type.SKIN);
//

//        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(playerId.getGameProfile());
//
//        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
//            skinLocation = minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
//
//            logger.info(String.format("Skin %s loaded via skinManager for player %s", skinLocation.toString(), playerId.getDisplayName()));
//        } else {
//            logger.info(String.format("Failed to load skin for player %s.", playerId.getDisplayName()));
//        }
//
//

        if (skinLocation == null) {
            // If there's no custom skin, use the default skin based on player UUID
            skinLocation = DefaultPlayerSkin.getDefaultSkin(playerId);

            logger.info(String.format("Player %s skinLocation is null after all methods. Using default skin.", playerId));
        }

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
        GlStateManager.translate(x, y + headSize / 2, 0);

        GlStateManager.scale(1.0F, 1.0F, 1.0F);

        drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, 64, 64);

        GlStateManager.popMatrix();
    }

    private float calculateYPosition(int index) {

        int baseYPos = 15;
        int paddingY = minecraft.fontRenderer.FONT_HEIGHT + 15;

        return baseYPos + paddingY * index;
    }
}
