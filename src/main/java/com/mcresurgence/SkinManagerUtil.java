package com.mcresurgence;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import javax.imageio.ImageIO;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class SkinManagerUtil {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Map<UUID, ResourceLocation> skinCache = new ConcurrentHashMap<>();
    private static final Map<UUID, CompletableFuture<ResourceLocation>> loadingFutures = new ConcurrentHashMap<>();

    public static void registerEventListeners() {
        MinecraftForge.EVENT_BUS.register(new SkinManagerUtil());
    }

    public static ResourceLocation getSkin(UUID playerUUID) {
        if (!skinCache.containsKey(playerUUID)) {
            return getDefaultSkin(playerUUID);
        }

        return skinCache.get(playerUUID);
    }

    /**
     * Retrieves the default skin for a given UUID. This could be enhanced to differentiate based on player UUID for Alex vs. Steve models.
     *
     * @param uuid UUID of the player
     * @return ResourceLocation pointing to the default skin
     */
    private static ResourceLocation getDefaultSkin(UUID uuid) {
        // Determine if we should use Alex or Steve based on UUID
        return DefaultPlayerSkin.getDefaultSkin(uuid);
    }

    public static CompletableFuture<ResourceLocation> getOrLoadSkin(UUID playerUUID, String playerName) {
        ResourceLocation cachedSkin = skinCache.get(playerUUID);
        if (cachedSkin != null) {
            // If skin is already cached, return it immediately wrapped in a completed future
            return CompletableFuture.completedFuture(cachedSkin);
        } else {
            // If skin is not in the cache, load it asynchronously
            return loadSkin(playerUUID, playerName).thenApply(loadedSkin -> {
                // Once loaded, return the skin from the cache to ensure consistency in what is returned
                return loadedSkin;
            }).exceptionally(ex -> {
                // Log the error and return default skin if there was an exception during skin loading
                ResurgentPVPStats.modLogger.error("Failed to load skin for " + playerName + ", using default skin.");
                return getDefaultSkin(playerUUID);
            });
        }
    }
//
//    public static CompletableFuture<ResourceLocation> loadSkin(UUID playerUUID, String playerName) {
//        return loadingFutures.computeIfAbsent(playerUUID, uuid -> {
//            CompletableFuture<ResourceLocation> skinLoadFuture = new CompletableFuture<>();
//
//            // Assuming we have a method fetchSkinUrl that returns the URL as a String
//            CompletableFuture.runAsync(() -> {
//                        try {
//                            String skinUrl = fetchSkinUrl(playerUUID); // This should already be async or made async
//
//                            // Download and create a DynamicTexture
////                            InputStream in = new URL(skinUrl).openStream();
////                            BufferedImage image = ImageIO.read(in);
//
//                            if (skinUrl != null) {
//                                BufferedImage image = ImageIO.read(new URL(skinUrl));
//                                Minecraft.getMinecraft().addScheduledTask(() -> {
//                                    // Ensure texture is loaded in the Minecraft client thread
//                                    DynamicTexture texture = new DynamicTexture(image);
//                                    ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("skins/" + uuid, texture);
//                                    skinCache.put(uuid, resourceLocation);
//                                    skinLoadFuture.complete(resourceLocation);
//                                });
//                            } else {
//                                throw new RuntimeException("Skin URL fetch returned null");
//                            }
//
////                            BufferedImage image = ImageIO.read(new URL(skinUrl));
//
//
////                            DynamicTexture texture = new DynamicTexture(image);
////                            ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("skins/" + uuid, texture);
////                            skinCache.put(uuid, resourceLocation);
////                            skinLoadFuture.complete(resourceLocation);
//                        } catch (Exception e) {
//                            ResurgentPVPStats.modLogger.error("Failed to load skin for " + playerName, e);
//                            skinLoadFuture.completeExceptionally(e);
//                        }
//                    }).whenComplete((location, ex) -> {
//                        loadingFutures.remove(uuid);
//                    });
//
//            scheduler.schedule(() -> {
//                if (!skinLoadFuture.isDone()) {
//                    skinLoadFuture.completeExceptionally(new TimeoutException("Failed to load skin within timeout period"));
//                }
//            }, 5, TimeUnit.SECONDS);
//
//            loadingFutures.put(uuid, skinLoadFuture);
//
//            return skinLoadFuture;
//        });
//    }


    public static CompletableFuture<ResourceLocation> loadSkin(UUID playerUUID, String playerName) {
        return loadingFutures.computeIfAbsent(playerUUID, uuid -> {
            // Create a new CompletableFuture that manages the entire loading process.
            CompletableFuture<ResourceLocation> skinLoadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    String skinUrl = fetchSkinUrl(uuid);
                    if (skinUrl == null) {
                        throw new RuntimeException("Failed to fetch skin URL.");
                    }
                    BufferedImage image = ImageIO.read(new URL(skinUrl));


                    ResurgentPVPStats.modLogger.info(String.format("Is Image null?: %b", image == null));
                    ResurgentPVPStats.modLogger.info("ImageIO.read(new URL(skinUrl)) result: ");
                    ResurgentPVPStats.modLogger.info(image.toString());

                    return image;
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }).thenComposeAsync(image -> {
                // Create a nested CompletableFuture to handle the addition of the texture to Minecraft in the game thread
                return CompletableFuture.supplyAsync(() -> {
                    DynamicTexture texture = new DynamicTexture(image);
                    ResourceLocation resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("skins/" + uuid, texture);
                    skinCache.put(uuid, resourceLocation);
                    return resourceLocation;
                }, Minecraft.getMinecraft()::addScheduledTask);
            });

            // Schedule a timeout for the skin loading future
            scheduler.schedule(() -> {
                if (!skinLoadFuture.isDone()) {
                    skinLoadFuture.completeExceptionally(new TimeoutException("Failed to load skin within timeout period"));
                }
            }, 5, TimeUnit.SECONDS);

            // Ensure to remove the future from the map once it is completed
            skinLoadFuture.whenComplete((res, ex) -> {
                if (ex != null) {
                    ResurgentPVPStats.modLogger.error("Failed to load skin for " + playerName + ": " + ex.getMessage());
                }
                loadingFutures.remove(uuid);
            });

            return skinLoadFuture;
        });
    }


    private static String fetchSkinUrl(UUID uuid) {
        ModLogger logger = ResurgentPVPStats.modLogger;
        String profileUrl = String.format("https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false", uuid.toString().replace("-", ""));
        try {
            logger.info(String.format("URL to download skin: %s", profileUrl));
            URL url = new URL(profileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                logger.info("Request OK");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                logger.info("Made it past appending response");

                // Parse JSON response
//                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
//                String base64Value = jsonResponse.getAsJsonArray("properties")
//                        .get(0).getAsJsonObject()
//                        .get("value").getAsString();

                JsonParser parser = new JsonParser(); // Create a JsonParser instance
                JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
                String base64Value = jsonResponse.getAsJsonArray("properties")
                        .get(0).getAsJsonObject()
                        .get("value").getAsString();


                logger.info(String.format("base64Value: %s", base64Value));

                // Decode Base64 Value
//                String decodedJson = new String(Base64.getDecoder().decode(base64Value));
//                JsonObject decodedObject = JsonParser.parseString(decodedJson).getAsJsonObject();
//                String skinUrl = decodedObject.getAsJsonObject("textures")
//                        .getAsJsonObject("SKIN")
//                        .get("url").getAsString();



                String decodedJson = new String(Base64.getDecoder().decode(base64Value));
                JsonObject decodedObject = parser.parse(decodedJson).getAsJsonObject();
                String skinUrl = decodedObject.getAsJsonObject("textures")
                        .getAsJsonObject("SKIN")
                        .get("url").getAsString();


                logger.info(String.format("Found a skinUrl: %s", skinUrl));

                return skinUrl;

            } else {
                logger.error(String.format("Failed to fetch profile: HTTP error code : %d", responseCode));
                return null; // or handle error accordingly
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to fetchSkinUrl: %s", e.getMessage()), e);
            e.printStackTrace();
            return null; // or handle error accordingly
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        // Triggered when a player logs out from a server or leaves a single-player world
        clearCache();

        ResurgentPVPStats.modLogger.info("Cleared skin cache onClientDisconnect.");
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
//        Triggered when the client unloads a world, including disconnecting from a server
//        if (!event.getWorld().isRemote) {
//            return; // Ensure this is client side
//        }

        ResurgentPVPStats.modLogger.info("Cleared skin cache onWorldUnload.");

        clearCache();
    }


    private static void clearCache() {
        skinCache.clear();
        loadingFutures.clear();
        ResurgentPVPStats.modLogger.info("Cleared skin cache and futures.");
    }
}
