package com.fende.obesecat.counterstrike.client;

import com.fende.obesecat.counterstrike.menu.BarracksMenu;
import com.fende.obesecat.counterstrike.network.RotateStructurePayload;
import com.fende.obesecat.counterstrike.network.StartConstructionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class BarracksScreen extends AbstractContainerScreen<BarracksMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int CHASSIS_DARK = 0xFF0E1410;
    private static final int CHASSIS_PANEL = 0xFF141F18;
    private static final int CHASSIS_BORDER = 0xFF244830;
    private static final int TACTICAL_GREEN = 0xFF38FF50;
    private static final int EVA_AMBER = 0xFFFFAA00;
    private static final int EVA_GOLD = 0xFFFFD700;
    private static final int EVA_RED = 0xFFFF3333;
    private static final int GAUGE_BG = 0xFF050B07;

    public BarracksScreen(BarracksMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 212;
        inventoryLabelY = -9999;
        titleLabelY = -9999;
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Suppress default vanilla labels that clip EVA telemetry
    }

    @Override
    protected void init() {
        super.init();

        // Row 1 Weapons
        addRenderableWidget(Button.builder(Component.literal("🔫 Rifle"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "automatic_rifle"));
        }).bounds(leftPos + 8, topPos + 46, 48, 16)
          .tooltip(Tooltip.create(Component.literal("§eAutomatic Rifle§r\n§7Cost: 4x Iron, 2x Tabbyrium, 1x Redstone\n§f24-Round Magazine with rapid fire.")))
          .build());

        addRenderableWidget(Button.builder(Component.literal("🚀 Rocket"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "rocket_launcher"));
        }).bounds(leftPos + 58, topPos + 46, 52, 16)
          .tooltip(Tooltip.create(Component.literal("§eRocket Launcher§r\n§7Cost: 6x Iron, 4x Tabbyrium, 2x Redstone, 2x Gunpowder\n§fSingle-tube high explosive projectile.")))
          .build());

        addRenderableWidget(Button.builder(Component.literal("💣 Grenade"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "frag_grenade"));
        }).bounds(leftPos + 112, topPos + 46, 56, 16)
          .tooltip(Tooltip.create(Component.literal("§eFrag Grenades (x4)§r\n§7Cost: 2x Iron, 2x Gunpowder, 1x Tabbyrium\n§fBouncing fragmentation explosives.")))
          .build());

        // Row 2 Weapons + Rotate
        addRenderableWidget(Button.builder(Component.literal("🔥 Flame"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "flamethrower"));
        }).bounds(leftPos + 8, topPos + 64, 48, 16)
          .tooltip(Tooltip.create(Component.literal("§eFlamethrower§r\n§7Cost: 4x Iron, 3x Tabbyrium, 2x Blaze/Gunpowder\n§fContinuous cone of searing flame.")))
          .build());

        addRenderableWidget(Button.builder(Component.literal("⭐ Commando"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "commando_rifle"));
        }).bounds(leftPos + 58, topPos + 64, 66, 16)
          .tooltip(Tooltip.create(Component.literal("§6Commando Assault Rifle§r\n§7Cost: 8x Iron, 6x Tabbyrium, 4x Redstone, 2x TNT\n§bHeavy 50-Cal Rifle + Shift-Click C-4 Detonation.")))
          .build());

        addRenderableWidget(Button.builder(Component.literal("⟳"), button -> {
            PacketDistributor.sendToServer(new RotateStructurePayload(menu.getCorePos()));
        }).bounds(leftPos + 126, topPos + 64, 42, 16)
          .tooltip(Tooltip.create(Component.literal("Rotate Barracks (90°)")))
          .build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Outer chassis
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, CHASSIS_BORDER);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, CHASSIS_DARK);

        // Header panel
        g.fill(leftPos + 8, topPos + 6, leftPos + imageWidth - 8, topPos + 42, CHASSIS_PANEL);

        // Header Text
        g.drawString(font, Component.literal("EVA INFANTRY PROCUREMENT"), leftPos + 12, topPos + 9, TACTICAL_GREEN, false);

        // Power Telemetry
        int gen = menu.getPowerGenerated();
        int con = menu.getPowerConsumed();
        boolean lowPower = menu.isLowPower();
        int powerColor = lowPower ? EVA_RED : EVA_AMBER;
        String powerText = "Grid: " + con + " / " + gen + " GW" + (lowPower ? " [LOW POWER!]" : " [NORMAL]");
        g.drawString(font, Component.literal(powerText), leftPos + 12, topPos + 20, powerColor, false);

        String facingName = menu.getFacing().getName().toUpperCase();
        g.drawString(font, Component.literal("Facing: " + facingName + " | Drain: -20 GW"), leftPos + 12, topPos + 30, EVA_GOLD, false);

        // Progress Bar (y = 82..91)
        int barX = leftPos + 8;
        int barY = topPos + 83;
        int barW = imageWidth - 16;
        int barH = 10;
        g.fill(barX, barY, barX + barW, barY + barH, GAUGE_BG);
        g.fill(barX + 1, barY + 1, barX + barW - 1, barY + barH - 1, 0xFF142018);

        float progress = menu.getProgressRatio();
        if (menu.isConstructing()) {
            int fillW = (int) ((barW - 2) * progress);
            int barColor = lowPower ? 0xFFFF9900 : TACTICAL_GREEN;
            g.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, barColor);

            int pct = (int) (progress * 100);
            String status = lowPower ? "FABRICATING (" + pct + "%) [50% SPEED]" : "FABRICATING (" + pct + "%)";
            g.drawCenteredString(font, Component.literal(status), barX + barW / 2, barY + 1, 0xFFFFFFFF);
        } else {
            g.drawCenteredString(font, Component.literal("ARMORY IDLE - SELECT INFANTRY WEAPON"), barX + barW / 2, barY + 1, 0xFF667766);
        }

        // Hopper Inputs Label
        g.drawString(font, Component.literal("Armory Hopper"), leftPos + 18, topPos + 94, EVA_AMBER, false);

        // 4 Input Hopper Slots at x = 18, 36, 54, 72, y = 104
        for (int i = 0; i < 4; i++) {
            g.blitSprite(SLOT, leftPos + 18 + i * 18 - 1, topPos + 104 - 1, 18, 18);
        }

        // Transfer Arrow
        g.drawString(font, Component.literal("➔"), leftPos + 104, topPos + 108, 0xFF88AA88, false);

        // Weapon Bay Label
        g.drawString(font, Component.literal("Arms"), leftPos + 138, topPos + 94, EVA_AMBER, false);

        // Output Slot at x = 138, y = 104
        g.blitSprite(SLOT, leftPos + 138 - 1, topPos + 104 - 1, 18, 18);

        // Player Inventory
        int startX = leftPos + 8;
        int startY = topPos + 130;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                g.blitSprite(SLOT, startX + col * 18 - 1, startY + row * 18 - 1, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            g.blitSprite(SLOT, startX + col * 18 - 1, topPos + 188 - 1, 18, 18);
        }
    }
}
