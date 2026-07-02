package com.fende.obesecat.client;

import com.fende.obesecat.network.SniperPacoFirePayload;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SniperPacoInputHandler {
    private SniperPacoInputHandler() {
    }

    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !shouldInterceptMouseButton(event.getButton(), event.getAction(), player.isUsingItem() && player.getUseItem().is(ModItems.SNIPER_PACO.get()))) {
            return;
        }

        PacketDistributor.sendToServer(new SniperPacoFirePayload(1));
        event.setCanceled(true);
    }

    public static boolean shouldInterceptMouseButton(int button, int action, boolean isScopedSniper) {
        return button == 0 && action == 1 && isScopedSniper;
    }
}
