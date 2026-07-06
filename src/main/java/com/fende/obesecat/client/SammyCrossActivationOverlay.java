package com.fende.obesecat.client;

import com.fende.obesecat.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public final class SammyCrossActivationOverlay {
    private SammyCrossActivationOverlay() {
    }

    public static void trigger() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.displayItemActivation(new ItemStack(ModItems.SAMMYS_CROSS.get()));
    }
}
