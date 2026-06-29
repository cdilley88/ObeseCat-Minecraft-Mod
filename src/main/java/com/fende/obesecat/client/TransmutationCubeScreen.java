package com.fende.obesecat.client;

import com.fende.obesecat.inventory.TransmutationCubeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TransmutationCubeScreen extends AbstractContainerScreen<TransmutationCubeMenu> {
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final int PANEL = 0xFF24221D;
    private static final int INNER = 0xFF11100E;
    private static final int BRONZE = 0xFF8A754D;
    private static final int LABEL = 0xFFE0D6C2;

    public TransmutationCubeScreen(TransmutationCubeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 224;
        inventoryLabelY = 128;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                        Component.translatable("container.obesecat.transmutation_cube.transmute"),
                        button -> sendTransmuteRequest())
                .bounds(leftPos + 43, topPos + 100, 90, 20)
                .build());
    }

    private void sendTransmuteRequest() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, TransmutationCubeMenu.TRANSMUTE_BUTTON_ID);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, BRONZE);
        graphics.fill(leftPos + 2, topPos + 2, leftPos + imageWidth - 2, topPos + imageHeight - 2, PANEL);
        graphics.fill(leftPos + 56, topPos + 15, leftPos + 120, topPos + 92, INNER);
        graphics.fill(leftPos + 3, topPos + 135, leftPos + 173, topPos + 222, INNER);

        for (int row = 0; row < 4; row++) {
            for (int column = 0; column < 3; column++) {
                drawSlot(graphics,
                        TransmutationCubeMenu.CUBE_SLOT_X + column * 18,
                        TransmutationCubeMenu.CUBE_SLOT_Y + row * 18);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics,
                        TransmutationCubeMenu.PLAYER_SLOT_X + column * 18,
                        TransmutationCubeMenu.PLAYER_SLOT_Y + row * 18);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics,
                    TransmutationCubeMenu.PLAYER_SLOT_X + column * 18,
                    TransmutationCubeMenu.HOTBAR_SLOT_Y);
        }
    }

    private void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(SLOT_SPRITE, leftPos + x - 1, topPos + y - 1, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL, false);
    }
}
