package com.mcresurgence;

import com.mcresurgence.config.PlayerJoinHandler;
import com.mcresurgence.config.ResurgentPVPStatsConfiguration;
import com.mcresurgence.playername.PlayerNameEventHandler;
import com.mcresurgence.scorekeeping.KillScoreLoadManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import org.apache.logging.log4j.Logger;

@Mod(modid = ResurgentPVPStats.MODID, name = ResurgentPVPStats.NAME, version = ResurgentPVPStats.VERSION)
public class ResurgentPVPStats {
    public static final String MODID = "resurgent-pvp-stats";
    public static final String NAME = "Resurgent PVP Stats";
    public static final String VERSION = "0.4.0";

    public static Logger LOGGER;
    public static ModLogger modLogger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        modLogger = new ModLogger(event.getModLog(), "[Resurgent PVP Stats] ");

        NetworkHandler.init();
        ResurgentPVPStatsConfiguration.init(event.getModConfigurationDirectory());

        KillScoreLoadManager.preInit(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (event.getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new KillDisplayOverlay(Minecraft.getMinecraft()));
            MinecraftForge.EVENT_BUS.register(new PlayerNameEventHandler());
            SkinManagerUtil.registerEventListeners();
        } else {
            MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
            MinecraftForge.EVENT_BUS.register(new PlayerKillEventHandler());
        }
    }
}
