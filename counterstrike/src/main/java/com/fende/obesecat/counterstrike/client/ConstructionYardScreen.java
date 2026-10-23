package com.fende.obesecat.counterstrike.client;

import com.fende.obesecat.counterstrike.menu.ConstructionYardMenu;
import com.fende.obesecat.counterstrike.network.DepositResourcesPayload;
import com.fende.obesecat.counterstrike.network.RotateStructurePayload;
import com.fende.obesecat.counterstrike.network.StartConstructionPayload;
import com.fende.obesecat.counterstrike.resource.ResourceValues;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ConstructionYardScreen extends AbstractContainerScreen<ConstructionYardMenu> {
    private static final ResourceLocation SLOT = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int CHASSIS_DARK = 0xFF0E1410;
    private static final int CHASSIS_PANEL = 0xFF141F18;
    private static final int CHASSIS_BORDER = 0xFF244830;
    private static final int TACTICAL_GREEN = 0xFF38FF50;
    private static final int EVA_AMBER = 0xFFFFAA00;
    private static final int EVA_GOLD = 0xFFFFD700;
    private static final int EVA_RED = 0xFFFF3333;
    private static final int GAUGE_BG = 0xFF050B07;

    private int activeTab = 0; // 0 = Schematics / Production, 1 = Resource Silos

    private Button tab0Button;
    private Button tab1Button;
    private Button turbineButton;
    private Button barracksButton;
    private Button rotateButton;
    private Button depositButton;

    public ConstructionYardScreen(ConstructionYardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 204;
        imageHeight = 222;
        inventoryLabelY = -9999;
        titleLabelY = -9999;
    }

    @Override
    protected void init() {
        super.init();

        // Top Navigation Tabs
        tab0Button = addRenderableWidget(Button.builder(Component.literal("🏗️ Production"), button -> {
            setActiveTab(0);
        }).bounds(leftPos + 8, topPos + 44, 92, 16).build());

        tab1Button = addRenderableWidget(Button.builder(Component.literal("📊 Resource Silos"), button -> {
            setActiveTab(1);
        }).bounds(leftPos + 104, topPos + 44, 92, 16).build());

        // Tab 0: Production / Schematics Widgets
        turbineButton = addRenderableWidget(Button.builder(Component.literal("⚡ Build Turbine"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "fission_turbine"));
        }).bounds(leftPos + 10, topPos + 64, 88, 16)
          .tooltip(Tooltip.create(Component.literal("§eTabbyrium Fission Turbine§r\n§7Base Silo Cost:\n§7• 36 Iron Units\n§7• 16 Tabbyrium Units\n§aPower Output: +200 GW\n§fHigh-efficiency atomic base generator.")))
          .build());

        barracksButton = addRenderableWidget(Button.builder(Component.literal("🎖️ Barracks"), button -> {
            PacketDistributor.sendToServer(new StartConstructionPayload(menu.getCorePos(), "barracks"));
        }).bounds(leftPos + 106, topPos + 64, 88, 16)
          .tooltip(Tooltip.create(Component.literal("§eInfantry Barracks§r\n§7Base Silo Cost:\n§7• 72 Iron Units\n§7• 24 Tabbyrium Units\n§7• 18 Gold Units\n§cPower Drain: -20 GW\n§fUnlocks infantry weapon fabrication.")))
          .build());

        rotateButton = addRenderableWidget(Button.builder(Component.literal("⟳ Rotate (90°)"), button -> {
            PacketDistributor.sendToServer(new RotateStructurePayload(menu.getCorePos()));
        }).bounds(leftPos + 10, topPos + 84, 88, 15)
          .tooltip(Tooltip.create(Component.literal("Rotate structure 90° clockwise")))
          .build());

        // Tab 1: Resource Silos Widgets
        depositButton = addRenderableWidget(Button.builder(Component.literal("📥 Deposit Stash"), button -> {
            PacketDistributor.sendToServer(new DepositResourcesPayload(menu.getCorePos()));
        }).bounds(leftPos + 116, topPos + 64, 78, 14)
          .tooltip(Tooltip.create(Component.literal("§aQuick-Deposit All§r\n§7Scans player inventory and stores all eligible Iron, Gold, Diamond, Trinitite, and Tabbyrium into base silos automatically.")))
          .build());

        updateWidgetVisibility();
        menu.setTab(activeTab);
    }

    private void setActiveTab(int tab) {
        this.activeTab = tab;
        menu.setTab(tab);
        updateWidgetVisibility();
    }

    private void updateWidgetVisibility() {
        boolean isTab0 = (activeTab == 0);
        boolean isTab1 = (activeTab == 1);

        turbineButton.visible = isTab0;
        barracksButton.visible = isTab0;
        rotateButton.visible = isTab0;

        depositButton.visible = isTab1;

        tab0Button.setMessage(Component.literal(isTab0 ? "§a▶ 🏗️ Production" : "🏗️ Production"));
        tab1Button.setMessage(Component.literal(isTab1 ? "§a▶ 📊 Silos" : "📊 Resource Silos"));
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        // Suppress default vanilla labels to prevent text overlap with custom EVA HUD
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);

        // Tooltip for Silo Columns in Tab 1
        if (activeTab == 1) {
            ResourceValues.ResourceType[] types = ResourceValues.ResourceType.values();
            int[] currentValues = {
                    menu.getIronTank(),
                    menu.getGoldTank(),
                    menu.getDiamondTank(),
                    menu.getTrinititeTank(),
                    menu.getTabbyriumTank()
            };

            for (int i = 0; i < 5; i++) {
                int colX = leftPos + 22 + i * 36;
                if (mouseX >= colX && mouseX <= colX + 18 && mouseY >= topPos + 76 && mouseY <= topPos + 122) {
                    ResourceValues.ResourceType type = types[i];
                    int current = currentValues[i];
                    int max = type.getMaxCapacity();

                    List<Component> tooltip = switch (type) {
                        case IRON -> List.of(
                                Component.literal("§b" + type.getDisplayName() + " Silo (" + type.getSymbol() + ")"),
                                Component.literal("§fStored: §a" + current + " §7/ " + max + " Units"),
                                Component.literal("§7• Nugget: 1 Unit"),
                                Component.literal("§7• Ingot / Raw: 9 Units"),
                                Component.literal("§7• Block: 81 Units"),
                                Component.literal("§aInsert below to dissolve into tank.")
                        );
                        case GOLD -> List.of(
                                Component.literal("§6" + type.getDisplayName() + " Silo (" + type.getSymbol() + ")"),
                                Component.literal("§fStored: §a" + current + " §7/ " + max + " Units"),
                                Component.literal("§7• Nugget: 1 Unit"),
                                Component.literal("§7• Ingot / Raw: 9 Units"),
                                Component.literal("§7• Block: 81 Units"),
                                Component.literal("§aInsert below to dissolve into tank.")
                        );
                        case DIAMOND -> List.of(
                                Component.literal("§b" + type.getDisplayName() + " Silo (" + type.getSymbol() + ")"),
                                Component.literal("§fStored: §a" + current + " §7/ " + max + " Units"),
                                Component.literal("§7• Diamond Gem: 9 Units"),
                                Component.literal("§7• Diamond Block: 81 Units"),
                                Component.literal("§aInsert below to dissolve into tank.")
                        );
                        case TRINITITE -> List.of(
                                Component.literal("§a" + type.getDisplayName() + " Silo (" + type.getSymbol() + ")"),
                                Component.literal("§fStored: §a" + current + " §7/ " + max + " Units"),
                                Component.literal("§7• Atomic Glass Block: 81 Units"),
                                Component.literal("§aInsert below to dissolve into tank.")
                        );
                        case TABBYRIUM -> List.of(
                                Component.literal("§e" + type.getDisplayName() + " Silo (" + type.getSymbol() + ")"),
                                Component.literal("§fStored: §a" + current + " §7/ " + max + " Units"),
                                Component.literal("§7• Shard: 1 Unit | Crystal: 4 Units"),
                                Component.literal("§7• Crust/Stalk/Planks: 9 Units"),
                                Component.literal("§7• Cluster: 16 Units | Silo: 81 Units"),
                                Component.literal("§aInsert below to dissolve into tank.")
                        );
                    };

                    g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        // Outer military chassis
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, CHASSIS_BORDER);
        g.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, CHASSIS_DARK);

        // Telemetry header panel
        g.fill(leftPos + 8, topPos + 6, leftPos + imageWidth - 8, topPos + 41, CHASSIS_PANEL);

        // Title
        g.drawString(font, Component.literal("EVA BATTLEFIELD CONTROL"), leftPos + 12, topPos + 8, TACTICAL_GREEN, false);

        // Power Telemetry
        int gen = menu.getPowerGenerated();
        int con = menu.getPowerConsumed();
        boolean lowPower = menu.isLowPower();
        int powerColor = lowPower ? EVA_RED : EVA_AMBER;
        String powerText = "Grid: " + con + " / " + gen + " GW" + (lowPower ? " [LOW POWER!]" : " [NORMAL]");
        g.drawString(font, Component.literal(powerText), leftPos + 12, topPos + 18, powerColor, false);

        // Facing & Drain
        String facingName = menu.getFacing().getName().toUpperCase();
        g.drawString(font, Component.literal("Facing: " + facingName + " | Drain: -25 GW"), leftPos + 12, topPos + 28, EVA_GOLD, false);

        // Tab indicator underline
        if (activeTab == 0) {
            g.fill(leftPos + 8, topPos + 60, leftPos + 100, topPos + 62, TACTICAL_GREEN);
        } else {
            g.fill(leftPos + 104, topPos + 60, leftPos + 196, topPos + 62, TACTICAL_GREEN);
        }

        // Inner panel for current tab
        g.fill(leftPos + 8, topPos + 62, leftPos + imageWidth - 8, topPos + 134, CHASSIS_PANEL);

        if (activeTab == 0) {
            renderProductionTab(g);
        } else {
            renderResourceSilosTab(g);
        }

        // Player Inventory slots background (centered at startX = 21)
        int startX = leftPos + 21;
        int startY = topPos + 140;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                g.blitSprite(SLOT, startX + col * 18 - 1, startY + row * 18 - 1, 18, 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            g.blitSprite(SLOT, startX + col * 18 - 1, topPos + 198 - 1, 18, 18);
        }
    }

    private void renderProductionTab(GuiGraphics g) {
        // Mini Tank Levels Indicator
        String tankSummary = "Fe: " + menu.getIronTank() + " | Tb: " + menu.getTabbyriumTank() + " | Au: " + menu.getGoldTank();
        g.drawString(font, Component.literal(tankSummary), leftPos + 104, topPos + 88, 0xFF88CC88, false);

        // Progress Bar
        int barX = leftPos + 10;
        int barY = topPos + 104;
        int barW = 126;
        int barH = 18;
        g.fill(barX, barY, barX + barW, barY + barH, GAUGE_BG);
        g.fill(barX + 1, barY + 1, barX + barW - 1, barY + barH - 1, 0xFF142018);

        float progress = menu.getProgressRatio();
        boolean lowPower = menu.isLowPower();
        if (menu.isConstructing()) {
            int fillW = (int) ((barW - 2) * progress);
            int barColor = lowPower ? 0xFFFF9900 : TACTICAL_GREEN;
            g.fill(barX + 1, barY + 1, barX + 1 + fillW, barY + barH - 1, barColor);

            int pct = (int) (progress * 100);
            String status = lowPower ? "FABRICATING (" + pct + "%) [50%]" : "FABRICATING (" + pct + "%)";
            g.drawCenteredString(font, Component.literal(status), barX + barW / 2, barY + 5, 0xFFFFFFFF);
        } else {
            g.drawCenteredString(font, Component.literal("SYSTEM IDLE - SELECT SCHEMATIC"), barX + barW / 2, barY + 5, 0xFF667766);
        }

        // Arrow indicator
        g.drawString(font, Component.literal("➔"), leftPos + 140, topPos + 108, 0xFF88AA88, false);

        // Output Bay Label
        g.drawString(font, Component.literal("Bay"), leftPos + 154, topPos + 92, EVA_AMBER, false);

        // Output Bay Slot at (154, 104)
        g.blitSprite(SLOT, leftPos + 154 - 1, topPos + 104 - 1, 18, 18);
    }

    private void renderResourceSilosTab(GuiGraphics g) {
        g.drawString(font, Component.literal("BASE STORAGE SILOS"), leftPos + 12, topPos + 67, TACTICAL_GREEN, false);

        // 5 Dedicated Silo Columns: Fe, Au, Dm, Tr, Tb
        ResourceValues.ResourceType[] types = ResourceValues.ResourceType.values();
        int[] currentValues = {
                menu.getIronTank(),
                menu.getGoldTank(),
                menu.getDiamondTank(),
                menu.getTrinititeTank(),
                menu.getTabbyriumTank()
        };

        for (int i = 0; i < 5; i++) {
            int colX = leftPos + 22 + i * 36;
            ResourceValues.ResourceType type = types[i];
            int current = currentValues[i];
            int max = type.getMaxCapacity();

            // Resource symbol
            g.drawString(font, Component.literal("[" + type.getSymbol() + "]"), colX + 1, topPos + 77, type.getColor(), false);

            // Level Gauge (topPos + 88 to 100)
            int gaugeY = topPos + 88;
            int gaugeH = 12;
            g.fill(colX, gaugeY, colX + 18, gaugeY + gaugeH, GAUGE_BG);
            g.fill(colX + 1, gaugeY + 1, colX + 17, gaugeY + gaugeH - 1, 0xFF142018);

            float ratio = Math.min(1.0F, (float) current / (float) max);
            int fillW = (int) (16 * ratio);
            if (fillW > 0) {
                g.fill(colX + 1, gaugeY + 1, colX + 1 + fillW, gaugeY + gaugeH - 1, type.getColor());
            }

            // Hopper Slot sprite at (colX, 104)
            g.blitSprite(SLOT, colX - 1, topPos + 104 - 1, 18, 18);
        }

        // Hopper Inputs Label (satisfies FissionTurbineTest)
        g.drawString(font, Component.literal("Hopper Inputs (Nugget: 1 | Ingot: 9 | Block: 81)"), leftPos + 12, topPos + 124, EVA_AMBER, false);
    }
}
