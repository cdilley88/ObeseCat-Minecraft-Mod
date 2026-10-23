package com.fende.obesecat.client;

import com.fende.obesecat.inventory.TabbyriumSiloMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TabbyriumSiloScreen extends AbstractContainerScreen<TabbyriumSiloMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int TIBERIUM_DARK = 0xFF0E1A12;
    private static final int TIBERIUM_PANEL = 0xFF14241A;
    private static final int TIBERIUM_BORDER = 0xFF2A5935;
    private static final int TIBERIUM_GREEN = 0xFF38FF50;
    private static final int TIBERIUM_DIM = 0xFF175924;
    private static final int EVA_AMBER = 0xFFFFAA00;
    private static final int EVA_RED = 0xFFFF3333;

    public TabbyriumSiloScreen(TabbyriumSiloMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 176;
        inventoryLabelY = 82;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Outer military chassis
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, TIBERIUM_BORDER);
        g.fill(leftPos + 2, topPos + 2, leftPos + 174, topPos + 174, TIBERIUM_DARK);
        g.fill(leftPos + 8, topPos + 20, leftPos + 168, topPos + 80, TIBERIUM_PANEL);

        // Charging cradle slot
        g.fill(leftPos + 41, topPos + 41, leftPos + 61, topPos + 61, TIBERIUM_BORDER);
        g.blitSprite(SLOT, leftPos + 42, topPos + 42, 18, 18);
        g.drawString(font, Component.literal("CHARGING"), leftPos + 28, topPos + 26, EVA_AMBER, false);

        // Energy column bar (Silo gauge)
        g.fill(leftPos + 72, topPos + 25, leftPos + 90, topPos + 75, 0xFF06140A);
        g.fill(leftPos + 73, topPos + 26, leftPos + 89, topPos + 74, TIBERIUM_DIM);
        int barHeight = menu.getEnergyBarHeight(48);
        if (barHeight > 0) {
            g.fill(leftPos + 73, topPos + 74 - barHeight, leftPos + 89, topPos + 74, TIBERIUM_GREEN);
        }

        // Tactical EVA Status Console
        int stored = menu.getEnergy();
        int capacity = menu.getCapacity();
        boolean isFull = menu.isFull();
        boolean flash = (System.currentTimeMillis() / 450) % 2 == 0;

        // EVA Box
        int boxBg = isFull && flash ? 0xFF3D0909 : 0xFF08120B;
        int boxBorder = isFull ? (flash ? EVA_RED : 0xFF991111) : TIBERIUM_BORDER;
        g.fill(leftPos + 96, topPos + 28, leftPos + 164, topPos + 72, boxBorder);
        g.fill(leftPos + 97, topPos + 29, leftPos + 163, topPos + 71, boxBg);

        if (isFull) {
            int textColor = flash ? EVA_RED : 0xFFFFAA00;
            g.drawString(font, "§l[EVA] ALERT:", leftPos + 100, topPos + 34, textColor, false);
            g.drawString(font, "§lSILOS NEEDED!", leftPos + 100, topPos + 46, textColor, false);
            g.drawString(font, "GRID FULL", leftPos + 100, topPos + 58, 0xFF888888, false);
        } else if (stored <= 0) {
            g.drawString(font, "[EVA] STANDBY", leftPos + 100, topPos + 34, 0xFF888888, false);
            g.drawString(font, "GRID EMPTY", leftPos + 100, topPos + 46, 0xFF666666, false);
            g.drawString(font, "REFINERY REQ.", leftPos + 100, topPos + 58, 0xFF555555, false);
        } else {
            int pct = capacity > 0 ? (int) ((long) stored * 100 / capacity) : 0;
            g.drawString(font, "[EVA] ONLINE", leftPos + 100, topPos + 34, TIBERIUM_GREEN, false);
            g.drawString(font, pct + "% CAPACITY", leftPos + 100, topPos + 46, EVA_AMBER, false);
            g.drawString(font, "10K FE/T MAX", leftPos + 100, topPos + 58, 0xFF77AA88, false);
        }

        // Inventory slots
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                g.blitSprite(SLOT, leftPos + 7 + col * 18, topPos + 93 + row * 18, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            g.blitSprite(SLOT, leftPos + 7 + col * 18, topPos + 151, 18, 18);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, TIBERIUM_GREEN, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFFB4C8BA, false);
    }
}
