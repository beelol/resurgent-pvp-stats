package com.mcresurgence.leaderboard.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class LeaderboardRequestPacket implements IMessage {

    public LeaderboardRequestPacket() {}

    @Override
    public void fromBytes(ByteBuf buf) {
        // No data needs to be sent for a request
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // No data needs to be sent for a request
    }
}
