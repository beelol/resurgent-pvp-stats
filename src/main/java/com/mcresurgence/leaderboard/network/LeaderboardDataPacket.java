package com.mcresurgence.leaderboard.network;

import com.mcresurgence.leaderboard.LeaderboardEntry;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LeaderboardDataPacket implements IMessage {
    private List<LeaderboardEntry> leaderboardEntries;

    public LeaderboardDataPacket() {
        // Initialize the list to avoid NullPointerException
        this.leaderboardEntries = new ArrayList<>();
    }

    public LeaderboardDataPacket(List<LeaderboardEntry> leaderboardEntries) {
        this.leaderboardEntries = leaderboardEntries != null ? leaderboardEntries : new ArrayList<>();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Deserialize leaderboard data from bytes
        int size = buf.readInt();  // Read the size of the leaderboard entries

        // Ensure the list is initialized
        leaderboardEntries = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            UUID playerUUID = new UUID(buf.readLong(), buf.readLong());
            int totalKills = buf.readInt();

            // Read the player name length
            int nameLength = buf.readInt();

            // Read the player name bytes
            byte[] nameBytes = new byte[nameLength];
            buf.readBytes(nameBytes);

            String playerName = new String(nameBytes, StandardCharsets.UTF_8); // Convert bytes to string

            leaderboardEntries.add(new LeaderboardEntry(playerUUID, playerName, totalKills));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Serialize leaderboard data to bytes
        buf.writeInt(leaderboardEntries.size());
        for (LeaderboardEntry entry : leaderboardEntries) {
            buf.writeLong(entry.getPlayerUUID().getMostSignificantBits());
            buf.writeLong(entry.getPlayerUUID().getLeastSignificantBits());
            buf.writeInt(entry.getTotalKills());

            // Serialize the player name
            byte[] nameBytes = entry.getPlayerName().getBytes(StandardCharsets.UTF_8);
            buf.writeInt(nameBytes.length);  // Write the length of the player name
            buf.writeBytes(nameBytes);  // Write the player name bytes
        }
    }

    public List<LeaderboardEntry> getLeaderboardEntries() {
        return leaderboardEntries;
    }
}
