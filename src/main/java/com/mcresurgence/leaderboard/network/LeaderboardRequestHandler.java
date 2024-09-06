package com.mcresurgence.leaderboard.network;

import com.mcresurgence.leaderboard.LeaderboardEntry;
import com.mcresurgence.leaderboard.LeaderboardManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class LeaderboardRequestHandler implements IMessageHandler<LeaderboardRequestPacket, IMessage> {

    @Override
    public IMessage onMessage(LeaderboardRequestPacket message, MessageContext ctx) {
        if (ctx.getServerHandler().player != null) {
            LeaderboardManager manager = new LeaderboardManager();
            List<LeaderboardEntry> leaderboardData = manager.getAllEntries();

            return new LeaderboardDataPacket(leaderboardData);
        }
        return null;
    }
}
