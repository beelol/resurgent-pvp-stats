package com.mcresurgence.leaderboard;

import com.mcresurgence.*;
import com.mcresurgence.leaderboard.network.LeaderboardRequestPacket;
import com.mcresurgence.leaderboard.LeaderboardEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardGUI extends GuiScreen {
    private int currentPage = 0;
    private int totalPages = 1;
    private List<LeaderboardEntry> leaderboardEntries;
    private final int playersPerPage = 10;
    private final int headSize = 16;

    private final Map<UUID, ResourceLocation> skinCache = new ConcurrentHashMap<>();

    int headColumnWidth = 20;  // Width for the player head column
    int nameColumnWidth = 150; // Fixed width for player names
    int killsColumnWidth = 50; // Fixed width for kill counts

    public LeaderboardGUI() {
        requestLeaderboardData();
    }

    @Override
    public void initGui() {
        // Clear any existing buttons
        this.buttonList.clear();

        // Only add buttons if there are more than one page
        if (totalPages > 1) {
            int centerX = this.width / 2;
            int buttonWidth = 80;
            int buttonY = this.height - 30;

            // Add buttons for page navigation
            this.buttonList.add(new GuiButton(1, centerX - buttonWidth - 5, buttonY, buttonWidth, 20, "Previous"));
            this.buttonList.add(new GuiButton(2, centerX + 5, buttonY, buttonWidth, 20, "Next"));

            updateButtonStates();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1: // Previous page
                if (currentPage > 0) {
                    currentPage--;

                    if (leaderboardEntries != null && !leaderboardEntries.isEmpty()) {
                        loadSkinsForCurrentPage();
                    }

                    drawLeaderboard();
                }
                break;
            case 2: // Next page
                if (currentPage < totalPages - 1) {
                    currentPage++;

                    if (leaderboardEntries != null && !leaderboardEntries.isEmpty()) {
                        loadSkinsForCurrentPage();
                    }

                    drawLeaderboard();
                }
                break;
        }
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.mc.currentScreen == this) {
            drawDefaultBackground();
            drawLeaderboard();
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    private void drawLeaderboard() {
        if (leaderboardEntries == null || leaderboardEntries.isEmpty()) {
            drawCenteredString(fontRenderer, "No leaderboard data available", this.width / 2, this.height / 2, 0xFFFFFF);
            return;
        }

        int yStart = 50;
        int entryHeight = 20;
        int headerPadding = 5;  // Padding below the header

        int start = currentPage * playersPerPage;
        int end = Math.min(start + playersPerPage, leaderboardEntries.size());

        int startX = (this.width - (headColumnWidth + nameColumnWidth + killsColumnWidth)) / 2;


        drawString(fontRenderer, "Player", startX, yStart - 10, 0xFFFFFF);

        drawString(fontRenderer, "Kills", startX + headColumnWidth + nameColumnWidth, yStart - 10, 0xFFFFFF);

        yStart += headerPadding;

        for (int i = start; i < end; i++) {
            LeaderboardEntry entry = leaderboardEntries.get(i);
            UUID playerUUID = entry.getPlayerUUID();
            String playerName = entry.getPlayerName(); // Use player name instead of UUID

            int yPosition = yStart + (i - start) * entryHeight;
            int textYOffset = yPosition + (entryHeight - fontRenderer.FONT_HEIGHT) / 2;
//            int headYOffset = yPosition + (entryHeight - fontRenderer.FONT_HEIGHT) / 2;

            // Draw background for each entry with a slightly darker color
            int backgroundColor = (i % 2 == 0) ? 0x20000000 : 0x10000000; // Alternating color for rows
            drawRect(startX, yPosition - 2, startX + headColumnWidth + nameColumnWidth + killsColumnWidth, yPosition + entryHeight - 2, backgroundColor);

            // Draw player head
            ResourceLocation playerHead = skinCache.get(entry.getPlayerUUID());

            if (playerHead == null) {
                drawString(fontRenderer, "...", startX + 4, textYOffset, 0xFFFFFF);  // Display dots as loading indicator
            } else {
                drawPlayerHead(playerHead, startX, textYOffset, playerUUID);
            }

            // Draw player name
            drawString(fontRenderer, playerName, startX + headColumnWidth, textYOffset, 0xFFFFFF);

            // Draw kill count
            String killsText = String.valueOf(entry.getTotalKills());
            drawString(fontRenderer, killsText, startX + headColumnWidth + nameColumnWidth, textYOffset, 0xFFFFFF);

            // Draw separator line between rows
            if (i < end - 1) {
                drawHorizontalLine(startX, startX + headColumnWidth + nameColumnWidth + killsColumnWidth, yPosition + entryHeight - 1, 0xFF666666);
            }
        }

        drawCenteredString(fontRenderer, "Page: " + (currentPage + 1) + " / " + totalPages, this.width / 2, this.height - 50, 0xFFFFFF);
    }

    public void updateLeaderboardData(List<LeaderboardEntry> newData) {
        this.leaderboardEntries = newData;

        ResurgentPVPStats.modLogger.info("Received new entries");

        if (leaderboardEntries != null && !leaderboardEntries.isEmpty()) {
            this.totalPages = (int) Math.ceil((double) leaderboardEntries.size() / playersPerPage);
        } else {
            this.totalPages = 1; // No data or empty file, so set totalPages to 1
        }

        initGui(); // Re-initialize the GUI to update buttons
        loadSkinsForCurrentPage();
        drawLeaderboard();
    }

    private void loadSkinsForCurrentPage() {
        ResurgentPVPStats.modLogger.info("Entered loadSkinsForCurrentPage");
        if (leaderboardEntries == null || leaderboardEntries.isEmpty()) {

            ResurgentPVPStats.modLogger.info("No leaderboard entries found. Canceling skin load");
            return;
        }


        int start = currentPage * playersPerPage;
        int end = Math.min(start + playersPerPage, leaderboardEntries.size());

        // Load skins asynchronously for the current page
        for (int i = start; i < end; i++) {
            LeaderboardEntry entry = leaderboardEntries.get(i);
            UUID playerUUID = entry.getPlayerUUID();

            if (playerUUID != null) { // Ensure playerUUID is not null
                // Fetch skin asynchronously
                fetchSkinAsync(playerUUID, entry.getPlayerName());
                ResurgentPVPStats.modLogger.info("Fetching skin for player " + entry.getPlayerName() + " with UUID " + playerUUID);
            } else {
                ResurgentPVPStats.modLogger.error("Player UUID is null for entry: " + entry.getPlayerName());
            }
        }
    }

    private void requestLeaderboardData() {
        NetworkHandler.INSTANCE.sendToServer(new LeaderboardRequestPacket());
    }

    private void updateButtonStates() {
        // Enable or disable buttons based on the current page

        GuiButton previousButton = buttonList.get(0);
        GuiButton nextButton = buttonList.get(1);

        if (previousButton != null) {
            previousButton.enabled = (currentPage > 0);
        }

        if (nextButton != null) {
            nextButton.enabled = (currentPage < totalPages - 1);
        }
    }


//    private void fetchAndDrawPlayerHead(UUID playerUUID, String playerName, int x, int y) {
//        CompletableFuture<ResourceLocation> skinLoadFuture = SkinManagerUtil.getOrLoadSkin(playerUUID, playerName);
//
//        skinLoadFuture.exceptionally(ex -> {
//            ResurgentPVPStats.modLogger.error("Failed to load skin: " + ex.getMessage());
//            return DefaultPlayerSkin.getDefaultSkin(playerUUID); // Fallback to default skin
//        }).thenAccept(skin -> {
//            Minecraft.getMinecraft().addScheduledTask(() -> drawPlayerHead(skin, x, y));
//        });
//    }

    private void fetchSkinAsync(UUID playerUUID, String playerName) {
        CompletableFuture<ResourceLocation> skinLoadFuture = SkinManagerUtil.getOrLoadSkin(playerUUID, playerName);

        skinLoadFuture.exceptionally(ex -> {
            ResurgentPVPStats.modLogger.error("Failed to load skin: " + ex.getMessage());

            skinCache.put(playerUUID, DefaultPlayerSkin.getDefaultSkin(playerUUID));
            Minecraft.getMinecraft().addScheduledTask(this::drawLeaderboard);

            return DefaultPlayerSkin.getDefaultSkin(playerUUID); // Fallback to default skin
        }).thenAccept(skin -> {
            // Store the loaded skin in cache and schedule a re-render
            skinCache.put(playerUUID, skin);
            Minecraft.getMinecraft().addScheduledTask(this::drawLeaderboard);
        });
    }

    private void drawPlayerHead(ResourceLocation playerSkin, int x, int y, UUID playerId) {
        if (playerSkin == null) {
            // Display loading indicator or placeholder if the player head texture is not yet available
            drawString(fontRenderer, "...", x + 4, y, 0xFFFFFF);
            return; // Exit to avoid binding a null texture
        }

        SkinDimensions dimensions = SkinManagerUtil.skinDimensionsByUUID.get(playerId);

        int width = 64;
        int height = 64;

        if (dimensions != null) {
            width = dimensions.getWidth();
            height = dimensions.getHeight();
        }

        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Bind the texture and draw the player head
        Minecraft.getMinecraft().getTextureManager().bindTexture(playerSkin);

        GlStateManager.translate(x, y - headSize / 2, 0);
        GlStateManager.scale(1.0F, 1.0F, 1.0F);

//        drawModalRectWithCustomSizedTexture(x, y, 8, 8, headSize, headSize, width, height);

        drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, width, height);

        GlStateManager.popMatrix();
    }

//    private void renderPlayerHead(ResourceLocation skinLocation, int x, int y, UUID playerId) {
////        SkinDimensions dimensions = SkinManagerUtil.skinDimensionsByUUID.get(playerId);
////
////        int width = 64;
////        int height = 64;
////
////        if (dimensions != null) {
////            width = dimensions.getWidth();
////            height = dimensions.getHeight();
////        }
//
////        GlStateManager.pushMatrix();
////        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//
//        Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
//        GlStateManager.translate(x, y + headSize / 2, 0);
//
//        GlStateManager.scale(1.0F, 1.0F, 1.0F);
//
//        drawModalRectWithCustomSizedTexture(0, 0, 8, 8, 8, 8, width, height);
//
//        GlStateManager.popMatrix();
//    }
}
