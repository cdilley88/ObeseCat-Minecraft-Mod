package com.fende.obesecat.item;

import com.fende.obesecat.ObeseCatMod;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class StarterHomeItem extends Item {
    private static final int BASEMENT_DEPTH = 6;
    private static final int CHUNK_WIDTH = 16;
    private final ResourceLocation templateId;
    private final String captionKey;
    private final boolean foil;
    private final int verticalOffset;

    public StarterHomeItem(Properties properties, String templateName, String captionKey, boolean foil, int verticalOffset) {
        super(properties);
        this.templateId = ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, templateName);
        this.captionKey = captionKey;
        this.foil = foil;
        this.verticalOffset = verticalOffset;
    }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        var player = context.getPlayer();
        StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
        if (template == null) return fail(player, "message.obesecat.starter_home.missing");
        var size = template.getSize();
        if (size.getX() > CHUNK_WIDTH || size.getZ() > CHUNK_WIDTH) return fail(player, "message.obesecat.starter_home.too_large");
        ChunkPos chunk = new ChunkPos(context.getClickedPos());
        int originY = context.getClickedPos().getY() + 1 - BASEMENT_DEPTH + verticalOffset;
        int topY = originY + size.getY() - 1;
        BlockPos origin = new BlockPos(chunk.getMinBlockX(), originY, chunk.getMinBlockZ());
        if (originY < level.getMinBuildHeight() || topY >= level.getMaxBuildHeight()) return fail(player, "message.obesecat.starter_home.bad_height");
        if (!level.hasChunksAt(origin, new BlockPos(chunk.getMaxBlockX(), topY, chunk.getMaxBlockZ()))) return fail(player, "message.obesecat.starter_home.not_loaded");
        clearDeploymentVolume(level, chunk, originY, topY);
        if (!template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), 2)) return fail(player, "message.obesecat.starter_home.failed");
        if (player == null || !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        if (player != null) player.displayClientMessage(Component.translatable("message.obesecat.starter_home.placed"), true);
        return InteractionResult.CONSUME;
    }
    private static InteractionResult fail(net.minecraft.world.entity.player.Player player, String key) {
        if (player != null) player.displayClientMessage(Component.translatable(key), true);
        return InteractionResult.FAIL;
    }
    private static void clearDeploymentVolume(ServerLevel level, ChunkPos chunk, int minY, int maxY) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++)
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++)
                for (int y = minY; y <= maxY; y++)
                    level.setBlock(cursor.set(x, y, z), Blocks.AIR.defaultBlockState(), 2);
    }
    @Override public boolean isFoil(ItemStack stack) { return foil; }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(captionKey).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("item.obesecat.starter_home.instructions").withStyle(ChatFormatting.GRAY));
    }
}
