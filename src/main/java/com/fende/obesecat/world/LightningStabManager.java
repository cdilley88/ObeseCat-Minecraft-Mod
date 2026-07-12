package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class LightningStabManager {
    public static final double RANGE = 25.0D;
    public static final double RADIUS = 5.0D;
    public static final float IMPACT_DAMAGE = 10.0F;
    public static final float PILLAR_DAMAGE = 2.0F;
    public static final int CAST_DELAY_TICKS = 12;
    public static final int PILLAR_DURATION_TICKS = 18;
    public static final int COOLDOWN_TICKS = 80;

    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingPillar> PENDING_PILLARS = new ArrayList<>();

    private LightningStabManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS, false));
    }

    public static void scheduleIonStormStrike(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), 0, true));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (PENDING_CASTS.isEmpty() && PENDING_PILLARS.isEmpty()) {
            return;
        }

        List<PendingCast> pendingCasts = List.copyOf(PENDING_CASTS);
        for (PendingCast pending : pendingCasts) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                PENDING_CASTS.remove(pending);
                executeCast(level, pending.origin, pending.damagePlayers);
            }
        }

        List<PendingPillar> pendingPillars = List.copyOf(PENDING_PILLARS);
        for (PendingPillar pending : pendingPillars) {
            if (pending.level != level) {
                continue;
            }
            if (pending.startDelayTicks > 0) {
                pending.startDelayTicks--;
                continue;
            }

            if (!pending.soundPlayed) {
                LocalSoundHelper.playLocalized(level, Vec3.atCenterOf(pending.base), ModSounds.LIGHTNING_STAB_FLAME.get(), 28.0D, 0.9F, 1.0F);
                pending.soundPlayed = true;
            }

            emitPillar(level, pending.base, pending.ticksRemaining);
            damageAroundPillar(level, pending.base, pending.damagePlayers);

            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                PENDING_PILLARS.remove(pending);
            }
        }
    }

    private static void executeCast(ServerLevel level, BlockPos origin, boolean damagePlayers) {
        Vec3 center = Vec3.atCenterOf(origin);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            // Keep the strike visual without allowing vanilla fire placement side effects.
            bolt.setVisualOnly(true);
            bolt.moveTo(center.x, center.y, center.z);
            level.addFreshEntity(bolt);
        }

        AABB impactBox = new AABB(
                center.x - RADIUS, center.y - 2.0D, center.z - RADIUS,
                center.x + RADIUS, center.y + 3.0D, center.z + RADIUS
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, impactBox,
                e -> e.isAlive() && (damagePlayers || !(e instanceof Player)))) {
            entity.hurt(level.damageSources().lightningBolt(), IMPACT_DAMAGE);
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 80));
        }

        int pillarCount = 10 + level.random.nextInt(5);
        for (int i = 0; i < pillarCount; i++) {
            double angle = level.random.nextDouble() * (Math.PI * 2.0D);
            double distance = level.random.nextDouble() * RADIUS;
            int x = (int) Math.round(center.x + Math.cos(angle) * distance);
            int z = (int) Math.round(center.z + Math.sin(angle) * distance);
            BlockPos base = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            int startDelay = level.random.nextInt(9);
            PENDING_PILLARS.add(new PendingPillar(level, base.immutable(), startDelay, PILLAR_DURATION_TICKS, damagePlayers));
        }
    }

    private static void emitPillar(ServerLevel level, BlockPos base, int ticksRemaining) {
        double pulse = 0.55D + (ticksRemaining / (double) PILLAR_DURATION_TICKS) * 0.45D;
        int height = 4 + level.random.nextInt(4);
        for (int y = 0; y < height; y++) {
            double yy = base.getY() + 0.15D + y * 0.7D;
            for (int i = 0; i < 3; i++) {
                double angle = (level.random.nextDouble() * Math.PI * 2.0D) + (ticksRemaining * 0.35D);
                double radius = pulse * (0.25D + level.random.nextDouble() * 0.45D);
                double xx = base.getX() + 0.5D + Math.cos(angle) * radius;
                double zz = base.getZ() + 0.5D + Math.sin(angle) * radius;
                level.sendParticles(ParticleTypes.FLAME, xx, yy, zz, 1, 0.0D, 0.06D, 0.0D, 0.01D);
                level.sendParticles(ParticleTypes.SMOKE, xx, yy, zz, 1, 0.0D, 0.03D, 0.0D, 0.0D);
            }
        }
    }

    private static void damageAroundPillar(ServerLevel level, BlockPos base, boolean damagePlayers) {
        Vec3 center = Vec3.atCenterOf(base);
        AABB box = new AABB(
                center.x - 1.2D, center.y - 0.5D, center.z - 1.2D,
                center.x + 1.2D, center.y + 3.0D, center.z + 1.2D
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e.isAlive() && (damagePlayers || !(e instanceof Player)))) {
            entity.hurt(level.damageSources().onFire(), PILLAR_DAMAGE);
            entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 60));
        }
    }

    private static final class PendingCast {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;
        private final boolean damagePlayers;

        private PendingCast(ServerLevel level, BlockPos origin, int delayTicks, boolean damagePlayers) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
            this.damagePlayers = damagePlayers;
        }
    }

    private static final class PendingPillar {
        private final ServerLevel level;
        private final BlockPos base;
        private int startDelayTicks;
        private int ticksRemaining;
        private boolean soundPlayed;
        private final boolean damagePlayers;

        private PendingPillar(ServerLevel level, BlockPos base, int startDelayTicks, int ticksRemaining, boolean damagePlayers) {
            this.level = level;
            this.base = base;
            this.startDelayTicks = startDelayTicks;
            this.ticksRemaining = ticksRemaining;
            this.soundPlayed = false;
            this.damagePlayers = damagePlayers;
        }
    }
}
