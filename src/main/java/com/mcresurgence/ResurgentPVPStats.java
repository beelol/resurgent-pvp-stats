package com.mcresurgence;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ResurgentPVPStats.MODID, name = ResurgentPVPStats.NAME, version = ResurgentPVPStats.VERSION)
public class ResurgentPVPStats {
    public static final String MODID = "resurgent-pvp-stats";
    public static final String NAME = "Resurgent PVP Stats";
    public static final String VERSION = "0.1.0";

    public static Logger LOGGER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        LOGGER.info("Pre Initialization Stage.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        LOGGER.info("Initialization Stage.");

        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new KillDisplayOverlay(Minecraft.getMinecraft()));
        }
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        LOGGER.info("Server starting.");
    }
}