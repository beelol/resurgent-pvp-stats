package com.mcresurgence;

import com.mcresurgence.config.PlayerJoinHandler;
import com.mcresurgence.config.ResurgentPVPStatsConfiguration;
import com.mcresurgence.leaderboard.ClientEventHandler;
import com.mcresurgence.leaderboard.KeyInputHandler;
import com.mcresurgence.leaderboard.LeaderboardGUI;
import com.mcresurgence.playername.PlayerNameEventHandler;
import com.mcresurgence.scorekeeping.KillScoreLoadManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = ResurgentPVPStats.MODID, name = ResurgentPVPStats.NAME, version = ResurgentPVPStats.VERSION)
public class ResurgentPVPStats {
    public static final String MODID = "resurgent-pvp-stats";
    public static final String NAME = "Resurgent PVP Stats";
    public static final String VERSION = "0.5.0";

    public static Logger LOGGER;
    public static ModLogger modLogger;

    private static KeyBinding keyToggleLeaderboard;


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

            keyToggleLeaderboard = new KeyBinding("Toggle Leaderboard", Keyboard.KEY_L, "My Mod");

            MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
            ClientEventHandler.registerKeyBindings();

            ClientRegistry.registerKeyBinding(keyToggleLeaderboard);
        } else {
            MinecraftForge.EVENT_BUS.register(new PlayerJoinHandler());
            MinecraftForge.EVENT_BUS.register(new PlayerKillEventHandler());
        }
    }


}
