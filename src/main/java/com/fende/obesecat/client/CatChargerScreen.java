package com.fende.obesecat.client;

import com.fende.obesecat.inventory.CatChargerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CatChargerScreen extends AbstractContainerScreen<CatChargerMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int ORANGE = 0xFFFF8A18;
    private static final int CYAN = 0xFF38E8F2;
    public CatChargerScreen(CatChargerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 176;
        inventoryLabelY = 82;
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) { super.render(g, mouseX, mouseY, partialTick); renderTooltip(g, mouseX, mouseY); }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, ORANGE);
        g.fill(leftPos + 2, topPos + 2, leftPos + 174, topPos + 174, 0xFF1E1510);
        g.fill(leftPos + 8, topPos + 22, leftPos + 168, topPos + 85, 0xFF0D1113);
        g.blitSprite(SLOT, leftPos + 43, topPos + 43, 18, 18);
        g.fill(leftPos + 78, topPos + 29, leftPos + 98, topPos + 78, 0xFF061719);
        int energy = menu.getEnergyBarHeight(45);
        if (energy > 0) g.fill(leftPos + 80, topPos + 76 - energy, leftPos + 96, topPos + 76, CYAN);
        g.drawString(font, Component.translatable("container.obesecat.cat_charger.slot"), leftPos + 20, topPos + 29, 0xFFFFC54A, false);
        g.drawString(font, menu.getEnergy() + " / " + menu.getCapacity() + " FE", leftPos + 105, topPos + 47, CYAN, false);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) g.blitSprite(SLOT, leftPos + 7 + col * 18, topPos + 93 + row * 18, 18, 18);
        for (int col = 0; col < 9; col++) g.blitSprite(SLOT, leftPos + 7 + col * 18, topPos + 151, 18, 18);
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, ORANGE, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFFFE4C7, false);
    }
}
