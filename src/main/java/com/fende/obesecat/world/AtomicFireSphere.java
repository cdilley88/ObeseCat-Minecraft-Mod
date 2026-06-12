package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class AtomicFireSphere {
    public static final int RADIUS = 100;
    public static final int SHELL_THICKNESS = 2;
    public static final int LIFETIME_TICKS = 300;

    private static final List<ActiveSphere> ACTIVE_SPHERES = new ArrayList<>();
    private static final List<PendingSphere> PENDING_SPHERES = new ArrayList<>();

    private AtomicFireSphere() {
    }

    public static void create(ServerLevel level, BlockPos origin) {
        List<BlockPos> placedBlocks = placeShell(level, origin, RADIUS);
        ACTIVE_SPHERES.add(new ActiveSphere(level, placedBlocks, LIFETIME_TICKS));
    }

    public static void createDelayed(ServerLevel level, BlockPos origin, int delayTicks) {
        PENDING_SPHERES.add(new PendingSphere(level, origin.immutable(), Math.max(delayTicks, 0)));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Iterator<PendingSphere> pendingIterator = PENDING_SPHERES.iterator();
        while (pendingIterator.hasNext()) {
            PendingSphere pending = pendingIterator.next();
            if (pending.level != level) {
                continue;
            }

            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                create(level, pending.origin);
                pendingIterator.remove();
            }
        }

        Iterator<ActiveSphere> iterator = ACTIVE_SPHERES.iterator();
        while (iterator.hasNext()) {
            ActiveSphere sphere = iterator.next();
            if (sphere.level != level) {
                continue;
            }

            sphere.lifetimeTicks--;
            if (sphere.lifetimeTicks <= 0) {
                removeSphere(sphere);
                iterator.remove();
            }
        }
    }

    private static List<BlockPos> placeShell(ServerLevel level, BlockPos origin, int radius) {
        List<BlockPos> placedBlocks = new ArrayList<>();
        BlockState atomicFire = ModBlocks.ATOMIC_FIRE.get().defaultBlockState();
        int radiusSquared = radius * radius;
        int innerRadius = radius - SHELL_THICKNESS;
        int innerRadiusSquared = innerRadius * innerRadius;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int dx = -radius; dx <= radius; dx++) {
            int dxSquared = dx * dx;
            for (int dy = -radius; dy <= radius; dy++) {
                int xySquared = dxSquared + dy * dy;
                if (xySquared > radiusSquared) {
                    continue;
                }

                int maxZ = (int) Math.floor(Math.sqrt(radiusSquared - xySquared));
                int minShellZ = innerRadiusSquared > xySquared
                        ? (int) Math.ceil(Math.sqrt(innerRadiusSquared - xySquared))
                        : 0;

                for (int absZ = minShellZ; absZ <= maxZ; absZ++) {
                    placeAtomicFire(level, origin, mutablePos, placedBlocks, atomicFire, dx, dy, absZ);
                    if (absZ != 0) {
                        placeAtomicFire(level, origin, mutablePos, placedBlocks, atomicFire, dx, dy, -absZ);
                    }
                }
            }
        }

        return placedBlocks;
    }

    private static void placeAtomicFire(
            ServerLevel level,
            BlockPos origin,
            BlockPos.MutableBlockPos mutablePos,
            List<BlockPos> placedBlocks,
            BlockState atomicFire,
            int dx,
            int dy,
            int dz
    ) {
        mutablePos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
        if (!level.getBlockState(mutablePos).isAir()) {
            return;
        }

        level.setBlock(mutablePos, atomicFire, 3);
        placedBlocks.add(mutablePos.immutable());
    }

    private static void removeSphere(ActiveSphere sphere) {
        for (BlockPos pos : sphere.placedBlocks) {
            if (sphere.level.getBlockState(pos).is(ModBlocks.ATOMIC_FIRE.get())) {
                sphere.level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static final class ActiveSphere {
        private final ServerLevel level;
        private final List<BlockPos> placedBlocks;
        private int lifetimeTicks;

        private ActiveSphere(ServerLevel level, List<BlockPos> placedBlocks, int lifetimeTicks) {
            this.level = level;
            this.placedBlocks = placedBlocks;
            this.lifetimeTicks = lifetimeTicks;
        }
    }

    private static final class PendingSphere {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;

        private PendingSphere(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }
}
