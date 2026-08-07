package com.fende.obesecat.item;

import com.fende.obesecat.ObeseCatMod;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class StarterHomeItem extends Item {
    private static final int BASEMENT_DEPTH = 6;
    private static final int CHUNK_WIDTH = 16;
    private static final long PREVIEW_LIFETIME_TICKS = 20L * 60L;
    private static final float EDGE_THICKNESS = 0.075F;
    private static final Map<UUID, DeploymentPreview> PREVIEWS = new HashMap<>();
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

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.FAIL;

        StructureTemplate template = level.getStructureManager().get(templateId).orElse(null);
        if (template == null) return fail(player, "message.obesecat.starter_home.missing");
        Vec3i size = template.getSize();
        if (size.getX() > CHUNK_WIDTH || size.getZ() > CHUNK_WIDTH) return fail(player, "message.obesecat.starter_home.too_large");

        ChunkPos chunk = new ChunkPos(context.getClickedPos());
        int originY = context.getClickedPos().getY() + 1 - BASEMENT_DEPTH + verticalOffset;
        int topY = originY + size.getY() - 1;
        BlockPos origin = new BlockPos(chunk.getMinBlockX(), originY, chunk.getMinBlockZ());
        if (originY < level.getMinBuildHeight() || topY >= level.getMaxBuildHeight()) return fail(player, "message.obesecat.starter_home.bad_height");
        if (!level.hasChunksAt(origin, new BlockPos(chunk.getMaxBlockX(), topY, chunk.getMaxBlockZ()))) return fail(player, "message.obesecat.starter_home.not_loaded");

        DeploymentTarget target = new DeploymentTarget(level.dimension(), templateId, chunk.toLong(), originY, topY);
        DeploymentPreview preview = PREVIEWS.get(player.getUUID());
        if (preview == null || !preview.target.equals(target) || preview.isExpired(level.getGameTime())) {
            discardPreview(preview);
            PREVIEWS.put(player.getUUID(), new DeploymentPreview(target, 1, level.getGameTime() + PREVIEW_LIFETIME_TICKS,
                    spawnOutline(level, chunk, originY, topY, false)));
            player.displayClientMessage(Component.translatable("message.obesecat.starter_home.preview", chunk.x, chunk.z, originY, topY), true);
            return InteractionResult.CONSUME;
        }

        if (preview.stage == 1) {
            discardPreview(preview);
            PREVIEWS.put(player.getUUID(), new DeploymentPreview(target, 2, level.getGameTime() + PREVIEW_LIFETIME_TICKS,
                    spawnOutline(level, chunk, originY, topY, true)));
            player.displayClientMessage(Component.translatable("message.obesecat.starter_home.confirm"), true);
            return InteractionResult.CONSUME;
        }

        discardPreview(PREVIEWS.remove(player.getUUID()));
        clearDeploymentVolume(level, chunk, originY, topY);
        if (!template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), 2)) {
            return fail(player, "message.obesecat.starter_home.failed");
        }
        if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        player.displayClientMessage(Component.translatable("message.obesecat.starter_home.placed"), true);
        return InteractionResult.CONSUME;
    }

    private static List<Display.BlockDisplay> spawnOutline(ServerLevel level, ChunkPos chunk, int minY, int maxY, boolean confirmed) {
        List<Display.BlockDisplay> indicators = new ArrayList<>(12);
        float minX = chunk.getMinBlockX();
        float minZ = chunk.getMinBlockZ();
        float width = CHUNK_WIDTH;
        float height = maxY - minY + 1;
        float topY = maxY + 1;

        for (float y : new float[] {minY, topY}) {
            indicators.add(spawnEdge(level, minX, y, minZ, width, EDGE_THICKNESS, EDGE_THICKNESS, confirmed));
            indicators.add(spawnEdge(level, minX, y, minZ + width - EDGE_THICKNESS, width, EDGE_THICKNESS, EDGE_THICKNESS, confirmed));
            indicators.add(spawnEdge(level, minX, y, minZ, EDGE_THICKNESS, EDGE_THICKNESS, width, confirmed));
            indicators.add(spawnEdge(level, minX + width - EDGE_THICKNESS, y, minZ, EDGE_THICKNESS, EDGE_THICKNESS, width, confirmed));
        }
        for (float x : new float[] {minX, minX + width - EDGE_THICKNESS}) {
            for (float z : new float[] {minZ, minZ + width - EDGE_THICKNESS}) {
                indicators.add(spawnEdge(level, x, minY, z, EDGE_THICKNESS, height, EDGE_THICKNESS, confirmed));
            }
        }
        return indicators;
    }

    private static Display.BlockDisplay spawnEdge(ServerLevel level, float x, float y, float z,
                                                   float scaleX, float scaleY, float scaleZ, boolean confirmed) {
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(scaleX, scaleY, scaleZ), new Quaternionf()))
                .result().orElse(null);
        long expiresAt = level.getGameTime() + PREVIEW_LIFETIME_TICKS;
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level) {
            {
                CompoundTag config = new CompoundTag();
                config.put("block_state", NbtUtils.writeBlockState((confirmed ? Blocks.ORANGE_STAINED_GLASS : Blocks.LIME_STAINED_GLASS).defaultBlockState()));
                config.putFloat("view_range", 64.0F);
                config.putBoolean("Glowing", true);
                config.putInt("glow_color_override", confirmed ? 0xFF9D21 : 0x55FF55);
                if (transform != null) config.put("transformation", transform);
                readAdditionalSaveData(config);
            }
            @Override public void tick() {
                super.tick();
                if (!level().isClientSide && level().getGameTime() >= expiresAt) discard();
            }
            @Override public boolean shouldBeSaved() { return false; }
        };
        display.setPos(x, y, z);
        level.addFreshEntity(display);
        return display;
    }

    private static void discardPreview(DeploymentPreview preview) {
        if (preview != null) preview.indicators.forEach(Display.BlockDisplay::discard);
    }

    private static InteractionResult fail(Player player, String key) {
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

    private record DeploymentTarget(ResourceKey<Level> dimension, ResourceLocation templateId, long chunkPos, int originY, int topY) {}
    private record DeploymentPreview(DeploymentTarget target, int stage, long expiresAt, List<Display.BlockDisplay> indicators) {
        private boolean isExpired(long gameTime) { return gameTime >= expiresAt; }
    }
}
