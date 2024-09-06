package com.mcresurgence.leaderboard;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;

@SideOnly(Side.CLIENT)
public class KeyInputHandler {
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        // Check if the key binding is pressed
        if (ClientEventHandler.keyToggleLeaderboard.isPressed()) {
            // Get the current Minecraft instance
            Minecraft mc = Minecraft.getMinecraft();

            // Check if the leaderboard GUI is currently open
            if (mc.currentScreen instanceof LeaderboardGUI) {
                // If it's open, close it by setting current screen to null
                mc.displayGuiScreen(null);
            } else {
                // If it's not open, open the leaderboard GUI
                mc.displayGuiScreen(new LeaderboardGUI());
            }
        }
    }
}
