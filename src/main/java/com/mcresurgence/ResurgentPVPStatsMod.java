package com.mcresurgence;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ResurgentPVPStatsMod.MODID, name = ResurgentPVPStatsMod.NAME, version = ResurgentPVPStatsMod.VERSION)
@Mod.EventBusSubscriber
public class ResurgentPVPStatsMod {
    public static final String MODID = "resurgent-pvp-stats-mod";
    public static final String NAME = "Resurgent PVP Stats";
    public static final String VERSION = "0.1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("Pre Initialization Stage.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Initialization Stage.");
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        logger.info("Server starting.");
    }
}
