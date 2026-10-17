package com.fende.obesecat.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuideBookItem extends Item {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuideBookItem.class);
    public static final ResourceLocation BOOK_ID = ResourceLocation.fromNamespaceAndPath("obesecat", "guide_book");

    public GuideBookItem(Properties properties) {
        super(properties.component(DataComponents.WRITTEN_BOOK_CONTENT, GuideBookContents.createContent()));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (ModList.get().isLoaded("patchouli")) {
            boolean opened = false;
            if (level.isClientSide()) {
                opened = tryOpenPatchouliClient();
            } else if (player instanceof ServerPlayer serverPlayer) {
                opened = tryOpenPatchouliServer(serverPlayer);
            }
            if (opened) {
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            }
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundOpenBookPacket(hand));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    private static boolean tryOpenPatchouliClient() {
        try {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            Class<?> interfaceClass = Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            java.lang.reflect.Method openMethod = interfaceClass.getMethod("openBookGUI", ResourceLocation.class);
            openMethod.invoke(api, BOOK_ID);
            return true;
        } catch (Throwable t) {
            LOGGER.error("Failed to open Patchouli client GUI", t);
            return false;
        }
    }

    private static boolean tryOpenPatchouliServer(ServerPlayer player) {
        try {
            Class<?> apiClass = Class.forName("vazkii.patchouli.api.PatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            Class<?> interfaceClass = Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            java.lang.reflect.Method openMethod = interfaceClass.getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class);
            openMethod.invoke(api, player, BOOK_ID);
            return true;
        } catch (Throwable t) {
            LOGGER.error("Failed to open Patchouli server GUI", t);
            return false;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Classified Field Manual")
                .withStyle(ChatFormatting.GOLD));
        tooltipComponents.add(Component.literal("Progression guide: Bunkers, Fat Man, Trinitite, & Transmutation.")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("Right-click to read.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
