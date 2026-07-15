package com.fende.obesecat.client;

import com.fende.obesecat.inventory.EchoingBlastChamberMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EchoingBlastChamberScreen extends AbstractContainerScreen<EchoingBlastChamberMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    public EchoingBlastChamberScreen(EchoingBlastChamberMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }
    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF5B765B);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, 0xFF17211E);
        drawSlot(g, 44, 35); drawSlot(g, 44, 58); drawSlot(g, 116, 47);
        g.fill(leftPos + 76, topPos + 47, leftPos + 102, topPos + 57, 0xFF071111);
        int width = menu.progressWidth();
        if (width > 0) g.fill(leftPos + 77, topPos + 48, leftPos + 77 + width, topPos + 56, 0xFF9EEBFF);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawSlot(g, 8 + col * 18, 84 + row * 18);
        for (int col = 0; col < 9; col++) drawSlot(g, 8 + col * 18, 142);
    }
    private void drawSlot(GuiGraphics g, int x, int y) { g.blitSprite(SLOT, leftPos + x - 1, topPos + y - 1, 18, 18); }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, 0xFFD9FFF0, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFD9FFF0, false);
    }
}
