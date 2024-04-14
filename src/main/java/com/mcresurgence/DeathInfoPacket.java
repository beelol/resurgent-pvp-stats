package com.mcresurgence;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

public class DeathInfoPacket implements IMessage {
    private UUID killerId;          // UUID for the player who is the killer
    private UUID killedId;          // UUID of any killed entity
    private String killerName;      // Name of the killer
    private String weaponRegistryName;
    private String killedName;      // Name of the killed entity

    public DeathInfoPacket() {}

    public DeathInfoPacket(UUID killer, String killerName, UUID killed, String killedName, String weapon) {
        this.killerId = killer;
        this.killerName = killerName; // Store the name of the killer
        this.killedId = killed;
        this.weaponRegistryName = weapon;
        this.killedName = killedName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        long killerMost = buf.readLong();
        long killerLeast = buf.readLong();
        long killedMost = buf.readLong();
        long killedLeast = buf.readLong();
        killerId = new UUID(killerMost, killerLeast);
        killedId = new UUID(killedMost, killedLeast);
        killerName = ByteBufUtils.readUTF8String(buf);    // Read the name of the killer
        weaponRegistryName = ByteBufUtils.readUTF8String(buf);
        killedName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(killerId.getMostSignificantBits());
        buf.writeLong(killerId.getLeastSignificantBits());
        buf.writeLong(killedId.getMostSignificantBits());
        buf.writeLong(killedId.getLeastSignificantBits());
        ByteBufUtils.writeUTF8String(buf, killerName);    // Write the name of the killer
        ByteBufUtils.writeUTF8String(buf, weaponRegistryName);
        ByteBufUtils.writeUTF8String(buf, killedName);
    }

    public static class Handler implements IMessageHandler<DeathInfoPacket, IMessage> {
        @Override
        public IMessage onMessage(DeathInfoPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ModLogger logger = ResurgentPVPStats.modLogger;

                // Assuming we are on the client and can access client-only classes
                EntityPlayer killer = Minecraft.getMinecraft().world.getPlayerEntityByName(message.killerName);
                Entity killed = ClientEntityFinder.findEntityByUUID(message.killedId); // Generic entities

                // Retrieve the Item from its registry name
                Item weaponItem = Item.getByNameOrId(message.weaponRegistryName);
                ItemStack weapon = weaponItem != null ? new ItemStack(weaponItem) : new ItemStack(Items.WOODEN_SWORD); // Default item example

                if (killer != null && killed != null) {
                    KillDisplayOverlay.displayKillInfo(killer.getName(), weapon, killed.getName(), killer, killed);

                    logger.info("Player " + killer.getName() + " killed " + killed.getName() + " using " + weapon.getDisplayName());
                } else {
                    logger.info("Error retrieving player data or weapon for kill display.");
                }
            });
            return null;
        }
    }
}
