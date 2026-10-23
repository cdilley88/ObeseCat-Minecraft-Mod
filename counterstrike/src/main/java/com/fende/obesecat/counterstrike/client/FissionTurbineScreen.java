package com.fende.obesecat.counterstrike.client;

import com.fende.obesecat.counterstrike.menu.FissionTurbineMenu;
import com.fende.obesecat.counterstrike.network.RotateStructurePayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class FissionTurbineScreen extends AbstractContainerScreen<FissionTurbineMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int CHASSIS_DARK = 0xFF0E1410;
    private static final int CHASSIS_PANEL = 0xFF141F18;
    private static final int CHASSIS_BORDER = 0xFF244830;
    private static final int TACTICAL_GREEN = 0xFF38FF50;
    private static final int EVA_AMBER = 0xFFFFAA00;
    private static final int EVA_RED = 0xFFFF3333;
    private static final int GAUGE_BG = 0xFF050B07;

    public FissionTurbineScreen(FissionTurbineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = -9999;
        titleLabelY = -9999;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Suppress default vanilla labels
    }

    @Override
    protected void init() {
        super.init();

        // 90-degree structure rotation button
        addRenderableWidget(Button.builder(Component.literal("⟳ Rotate"), button -> {
            PacketDistributor.sendToServer(new RotateStructurePayload(menu.getCorePos()));
        }).bounds(leftPos + 106, topPos + 50, 62, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Outer military chassis
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, CHASSIS_BORDER);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, CHASSIS_DARK);

        // Header panel
        g.fill(leftPos + 8, topPos + 8, leftPos + imageWidth - 8, topPos + 44, CHASSIS_PANEL);

        // Header title
        g.drawString(font, Component.literal("⚡ TABBYRIUM TURBINE"), leftPos + 12, topPos + 12, TACTICAL_GREEN, false);

        // Power Telemetry
        boolean active = menu.getBurnTime() > 0;
        int outputColor = active ? TACTICAL_GREEN : 0xFF888888;
        String outputText = active ? "Output: +200 GW [ACTIVE]" : "Output: 0 GW [OFFLINE]";
        g.drawString(font, Component.literal(outputText), leftPos + 12, topPos + 23, outputColor, false);

        int gen = menu.getPowerGenerated();
        int con = menu.getPowerConsumed();
        int gridColor = menu.isLowPower() ? EVA_RED : EVA_AMBER;
        String gridText = "Grid: " + con + " / " + gen + " GW" + (menu.isLowPower() ? " [LOW POWER!]" : "");
        g.drawString(font, Component.literal(gridText), leftPos + 12, topPos + 33, gridColor, false);

        // Fuel Slot background
        g.blitSprite(SLOT, leftPos + 79, topPos + 51, 18, 18);

        // Fuel Label
        g.drawString(font, Component.literal("Fuel"), leftPos + 52, topPos + 56, EVA_AMBER, false);

        // Burn Progress Gauge (vertical bar next to fuel slot)
        int gaugeX = leftPos + 12;
        int gaugeY = topPos + 50;
        int gaugeW = 34;
        int gaugeH = 20;
        g.fill(gaugeX, gaugeY, gaugeX + gaugeW, gaugeY + gaugeH, GAUGE_BG);
        g.fill(gaugeX + 1, gaugeY + 1, gaugeX + gaugeW - 1, gaugeY + gaugeH - 1, 0xFF1A261C);

        float progress = menu.getBurnProgress();
        if (progress > 0) {
            int fillW = (int) ((gaugeW - 2) * progress);
            g.fill(gaugeX + 1, gaugeY + 1, gaugeX + 1 + fillW, gaugeY + gaugeH - 1, TACTICAL_GREEN);
        }
        g.drawCenteredString(font, Component.literal(active ? "BURNING" : "EMPTY"), gaugeX + gaugeW / 2, gaugeY + 6, active ? 0xFF003300 : 0xFF666666);

        // Player inventory slots background
        int startX = leftPos + 8;
        int startY = topPos + 84;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                g.blitSprite(SLOT, startX + col * 18 - 1, startY + row * 18 - 1, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            g.blitSprite(SLOT, startX + col * 18 - 1, topPos + 142 - 1, 18, 18);
        }
    }
}
