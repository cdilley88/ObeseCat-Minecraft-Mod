package com.fende.obesecat.world;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class StasisSwordManager {
    public static final double RANGE = 25.0D;
    public static final int CAST_DELAY_TICKS = 5;
    public static final int SHATTER_DELAY_TICKS = 40;
    public static final int COOLDOWN_TICKS = 40;

    private static final ResourceLocation STASIS_TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "stasisice"
    );
    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingFormation> PENDING_FORMATIONS = new ArrayList<>();

    private StasisSwordManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS));
    }

    public static Optional<FrozenFormation> place(ServerLevel level, BlockPos origin) {
        BlockPos placementOrigin = origin.offset(-1, 1, -1);
        Optional<StructureTemplate> template = level.getStructureManager().get(STASIS_TEMPLATE_ID);
        if (template.isEmpty()) {
            return Optional.empty();
        }

        StructureTemplate loadedTemplate = template.get();
        List<StructureTemplate.StructureBlockInfo> blocks = loadedTemplate.filterBlocks(placementOrigin, new StructurePlaceSettings(), Blocks.ICE, true);
        if (blocks.isEmpty()) {
            return Optional.empty();
        }

        Map<BlockPos, BlockState> templateStates = new HashMap<>();
        for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
            templateStates.put(blockInfo.pos().immutable(), blockInfo.state());
        }

        BlockPos pocketOffset = findCapturePocket(templateStates).orElse(new BlockPos(1, 1, 1));
        BlockPos pocketWorldPos = placementOrigin.offset(pocketOffset.getX(), pocketOffset.getY(), pocketOffset.getZ());
        Vec3 pocketCenter = Vec3.atCenterOf(pocketWorldPos);
        AABB pullBox = new AABB(
                pocketCenter.x - 2.0D,
                pocketCenter.y - 2.0D,
                pocketCenter.z - 2.0D,
                pocketCenter.x + 2.0D,
                pocketCenter.y + 2.0D,
                pocketCenter.z + 2.0D
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, pullBox, entity -> entity.isAlive() && !(entity instanceof Player))) {
            entity.teleportTo(pocketCenter.x, pocketCenter.y, pocketCenter.z);
            entity.setDeltaMovement(Vec3.ZERO);
            entity.hasImpulse = true;
            entity.hurt(level.damageSources().inWall(), 8.0F);
        }

        List<BlockPos> placedBlocks = new ArrayList<>();
        for (StructureTemplate.StructureBlockInfo blockInfo : blocks) {
            BlockPos pos = blockInfo.pos();
            BlockState existingState = level.getBlockState(pos);
            if (!canOverwriteWithIce(existingState)) {
                continue;
            }

            if (level.setBlock(pos, blockInfo.state(), 3)) {
                placedBlocks.add(pos.immutable());
            }
        }

        if (placedBlocks.isEmpty()) {
            return Optional.empty();
        }

        FrozenFormation formation = new FrozenFormation(placementOrigin.immutable(), List.copyOf(placedBlocks));
        PENDING_FORMATIONS.add(new PendingFormation(level, formation, SHATTER_DELAY_TICKS));
        return Optional.of(formation);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || (PENDING_CASTS.isEmpty() && PENDING_FORMATIONS.isEmpty())) {
            return;
        }

        List<PendingCast> pendingCasts = List.copyOf(PENDING_CASTS);
        for (PendingCast pendingCast : pendingCasts) {
            if (pendingCast.level != level) {
                continue;
            }

            pendingCast.delayTicks--;
            if (pendingCast.delayTicks <= 0) {
                level.playSound(null, pendingCast.origin.getX() + 0.5D, pendingCast.origin.getY() + 0.5D, pendingCast.origin.getZ() + 0.5D, ModSounds.STASIS_ICE.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                place(level, pendingCast.origin);
                PENDING_CASTS.remove(pendingCast);
            }
        }

        if (PENDING_FORMATIONS.isEmpty()) {
            return;
        }

        List<PendingFormation> pendingFormations = List.copyOf(PENDING_FORMATIONS);
        for (PendingFormation pending : pendingFormations) {
            if (pending.level != level) {
                continue;
            }

            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                shatter(level, pending.formation);
                PENDING_FORMATIONS.remove(pending);
            }
        }
    }

    private static void shatter(ServerLevel level, FrozenFormation formation) {
        for (BlockPos pos : formation.placedBlocks()) {
            if (!level.getBlockState(pos).isAir()) {
                level.levelEvent(2001, pos, Block.getId(Blocks.ICE.defaultBlockState()));
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static boolean canOverwriteWithIce(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof DoublePlantBlock
                || state.is(Blocks.SHORT_GRASS)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.LARGE_FERN);
    }

    private static Optional<BlockPos> findCapturePocket(Map<BlockPos, BlockState> templateStates) {
        for (Map.Entry<BlockPos, BlockState> entry : templateStates.entrySet()) {
            if (!entry.getValue().isAir()) {
                continue;
            }

            BlockPos pos = entry.getKey();
            if (isIce(templateStates, pos.north())
                    && isIce(templateStates, pos.south())
                    && isIce(templateStates, pos.east())
                    && isIce(templateStates, pos.west())
                    && isIce(templateStates, pos.above())
                    && isIce(templateStates, pos.below())) {
                return Optional.of(pos);
            }
        }

        return Optional.empty();
    }

    private static boolean isIce(Map<BlockPos, BlockState> templateStates, BlockPos pos) {
        BlockState state = templateStates.get(pos);
        return state != null && state.is(Blocks.ICE);
    }

    private static final class PendingCast {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;

        private PendingCast(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }

    public record FrozenFormation(BlockPos origin, List<BlockPos> placedBlocks) {
    }

    private static final class PendingFormation {
        private final ServerLevel level;
        private final FrozenFormation formation;
        private int delayTicks;

        private PendingFormation(ServerLevel level, FrozenFormation formation, int delayTicks) {
            this.level = level;
            this.formation = formation;
            this.delayTicks = delayTicks;
        }
    }
}
