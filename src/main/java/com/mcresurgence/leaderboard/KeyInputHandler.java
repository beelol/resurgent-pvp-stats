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
            // Open the leaderboard GUI
            Minecraft.getMinecraft().displayGuiScreen(new LeaderboardGUI());
        }
    }
}
