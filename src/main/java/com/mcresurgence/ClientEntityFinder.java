package com.mcresurgence;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import java.util.UUID;

public class ClientEntityFinder {
    public static Entity findEntityByUUID(UUID uuid) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) {
            return null; // Early exit if the world isn't loaded
        }

        // Iterate over all entities in the world
        for (Entity entity : minecraft.world.loadedEntityList) {
            if (uuid.equals(entity.getUniqueID())) {
                return entity;
            }
        }

        return null; // Return null if no entity matches the UUID
    }
}