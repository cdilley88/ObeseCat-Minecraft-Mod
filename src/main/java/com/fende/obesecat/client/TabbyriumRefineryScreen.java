package com.fende.obesecat.client;

import com.fende.obesecat.inventory.TabbyriumRefineryMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TabbyriumRefineryScreen extends AbstractContainerScreen<TabbyriumRefineryMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int GREEN_ACCENT = 0xFF39FF14; // Neon Tiberium Green
    private static final int DARK_STEEL = 0xFF18221B;
    private static final int PANEL = 0xFF111713;
    private static final int INNER = 0xFF0A0F0C;
    private static final int TEXT = 0xFFE0FFE5;
    private static final int ITEM_ACCENT = 0xFFFFC438;
    private static final int ITEM_PANEL = 0xFF3A2D0B;
    private static final int POWER_PANEL = 0xFF0D3316;
    private static final String[] SIDE_NAMES = {"D", "U", "N", "S", "W", "E"};

    private boolean powerLayer;
    private final List<Button> sideButtons = new ArrayList<>();
    private Button itemLayerButton;
    private Button powerLayerButton;

    public TabbyriumRefineryScreen(TabbyriumRefineryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 200;
        imageHeight = 260;
        inventoryLabelX = 20;
        inventoryLabelY = 165;
        titleLabelX = 12;
        titleLabelY = 8;
    }

    @Override
    protected void init() {
        super.init();
        sideButtons.clear();
        itemLayerButton = addRenderableWidget(Button.builder(Component.literal("[ITEM]"), ignored -> selectLayer(false))
                .bounds(leftPos + 18, topPos + 101, 78, 18).build());
        powerLayerButton = addRenderableWidget(Button.builder(Component.literal("POWER"), ignored -> selectLayer(true))
                .bounds(leftPos + 104, topPos + 101, 78, 18).build());
        for (int i = 0; i < 6; i++) {
            final int side = i;
            Button button = Button.builder(sideLabel(side), ignored -> toggleSide(side))
                    .bounds(leftPos + 13 + (i % 3) * 56, topPos + 126 + (i / 3) * 20, 52, 18)
                    .build();
            sideButtons.add(addRenderableWidget(button));
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        itemLayerButton.setMessage(Component.literal(powerLayer ? "ITEM" : "[ITEM]"));
        powerLayerButton.setMessage(Component.literal(powerLayer ? "[POWER]" : "POWER"));
        for (int i = 0; i < sideButtons.size(); i++) sideButtons.get(i).setMessage(sideLabel(i));
    }

    private void selectLayer(boolean power) {
        powerLayer = power;
    }

    private Component sideLabel(int side) {
        boolean enabled = powerLayer ? menu.isPowerOutputEnabled(side) : menu.isItemInputEnabled(side);
        String state = enabled ? (powerLayer ? "OUT" : "IN") : "OFF";
        return Component.literal(SIDE_NAMES[side] + ": " + state);
    }

    private void toggleSide(int side) {
        if (minecraft != null && minecraft.gameMode != null) {
            int base = powerLayer ? TabbyriumRefineryMenu.POWER_SIDE_BUTTON_BASE : TabbyriumRefineryMenu.ITEM_SIDE_BUTTON_BASE;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, base + side);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (mouseX >= leftPos + 78 && mouseX < leftPos + 96 && mouseY >= topPos + 29 && mouseY < topPos + 86) {
            graphics.renderTooltip(font, List.of(
                    Component.literal(menu.getEnergy() + " FE / " + menu.getCapacity() + " FE")
            ), java.util.Optional.empty(), mouseX, mouseY);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, DARK_STEEL);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, PANEL);
        g.fill(leftPos + 8, topPos + 23, leftPos + 192, topPos + 87, INNER);

        int sideAccent = powerLayer ? GREEN_ACCENT : ITEM_ACCENT;
        int sidePanel = powerLayer ? POWER_PANEL : ITEM_PANEL;
        g.fill(leftPos + 8, topPos + 89, leftPos + 192, topPos + 169, sideAccent);
        g.fill(leftPos + 10, topPos + 91, leftPos + 190, topPos + 167, sidePanel);
        g.fill(leftPos + 8, topPos + 172, leftPos + 192, topPos + 257, INNER);

        drawSlot(g, TabbyriumRefineryMenu.INPUT_X, TabbyriumRefineryMenu.INPUT_Y);
        g.drawString(font, Component.translatable("container.obesecat.tabbyrium_refinery.input"), leftPos + 17, topPos + 30, TEXT, false);

        g.fill(leftPos + 77, topPos + 28, leftPos + 97, topPos + 87, 0xFF051408);
        g.fill(leftPos + 79, topPos + 30, leftPos + 95, topPos + 85, 0xFF0C2B12);
        int energyHeight = menu.getEnergyBarHeight(55);
        if (energyHeight > 0) g.fill(leftPos + 79, topPos + 85 - energyHeight, leftPos + 95, topPos + 85, GREEN_ACCENT);

        g.drawString(font, Component.translatable("container.obesecat.tabbyrium_refinery.buffer"), leftPos + 104, topPos + 32, TEXT, false);
        g.drawString(font, menu.getEnergy() + " / " + menu.getCapacity() + " FE", leftPos + 104, topPos + 48, GREEN_ACCENT, false);

        g.fill(leftPos + 17, topPos + 76, leftPos + 66, topPos + 82, 0xFF051408);
        int progress = menu.getProgressWidth(47);
        if (progress > 0) g.fill(leftPos + 18, topPos + 77, leftPos + 18 + progress, topPos + 81, 0xFF39FF14);
        g.drawString(font, Component.translatable("container.obesecat.tabbyrium_refinery.sides"), leftPos + 12, topPos + 91, sideAccent, false);

        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) drawSlot(g, TabbyriumRefineryMenu.PLAYER_X + col * 18, TabbyriumRefineryMenu.PLAYER_Y + row * 18);
        for (int col = 0; col < 9; col++) drawSlot(g, TabbyriumRefineryMenu.PLAYER_X + col * 18, TabbyriumRefineryMenu.HOTBAR_Y);
    }

    private void drawSlot(GuiGraphics g, int x, int y) {
        g.blitSprite(SLOT, leftPos + x - 1, topPos + y - 1, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, title, titleLabelX, titleLabelY, GREEN_ACCENT, false);
        g.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT, false);
    }
}
