package com.mcresurgence.leaderboard.network;

import com.mcresurgence.leaderboard.LeaderboardGUI;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraft.client.Minecraft;

public class LeaderboardDataHandler implements IMessageHandler<LeaderboardDataPacket, IMessage> {

    @Override
    public IMessage onMessage(LeaderboardDataPacket message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (Minecraft.getMinecraft().currentScreen instanceof LeaderboardGUI) {
                LeaderboardGUI gui = (LeaderboardGUI) Minecraft.getMinecraft().currentScreen;
                gui.updateLeaderboardData(message.getLeaderboardEntries());
            }
        });
        return null;
    }
}
