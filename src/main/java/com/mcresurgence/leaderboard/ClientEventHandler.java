package com.mcresurgence.leaderboard;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class ClientEventHandler {
    public static KeyBinding keyToggleLeaderboard;

    public static void registerKeyBindings() {
        // Define and register the key binding
        keyToggleLeaderboard = new KeyBinding("Toggle Leaderboard", Keyboard.KEY_L, "Resurgent PVP Stats");
        ClientRegistry.registerKeyBinding(keyToggleLeaderboard);
    }
}
