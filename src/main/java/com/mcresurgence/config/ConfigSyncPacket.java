package com.mcresurgence.config;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ConfigSyncPacket implements IMessage {
    private boolean showNametags;

    public ConfigSyncPacket() { }

    public ConfigSyncPacket(boolean showNametags) {
        this.showNametags = showNametags;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        showNametags = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(showNametags);
    }

    public static class Handler implements IMessageHandler<ConfigSyncPacket, IMessage> {
        @Override
        public IMessage onMessage(ConfigSyncPacket message, MessageContext ctx) {
            // Handle received data on the client
            net.minecraftforge.fml.common.FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> {
                ResurgentPVPStatsConfiguration.setShowNametagsEnabled(message.showNametags);
            });
            return null;
        }
    }
}
