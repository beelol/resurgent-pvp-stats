package com.mcresurgence;

import com.mcresurgence.config.ConfigSyncPacket;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {
    public static SimpleNetworkWrapper INSTANCE;
    private static int packetId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("resurgent_pvp");

        // Register packets
        INSTANCE.registerMessage(DeathInfoPacket.Handler.class, DeathInfoPacket.class, packetId++, Side.CLIENT);

        // Register new packet for configuration sync
        INSTANCE.registerMessage(ConfigSyncPacket.Handler.class, ConfigSyncPacket.class, packetId++, Side.CLIENT);
    }
}
