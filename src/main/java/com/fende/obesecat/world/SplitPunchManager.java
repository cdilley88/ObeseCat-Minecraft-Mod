package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.world.LocalSoundHelper;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SplitPunchManager {
    public static final double RANGE = 25.0D;
    public static final float DAMAGE = 8.0F;
    public static final double RADIUS = 4.0D;
    public static final int CAST_DELAY_TICKS = 15;
    public static final int GFX_DURATION_TICKS = 40;
    public static final int PARTICLE_DURATION_TICKS = 10;
    public static final int COOLDOWN_TICKS = 40;

    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingRemoval> PENDING_REMOVALS = new ArrayList<>();
    private static final List<PendingParticles> PENDING_PARTICLES = new ArrayList<>();

    private SplitPunchManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (PENDING_CASTS.isEmpty() && PENDING_REMOVALS.isEmpty() && PENDING_PARTICLES.isEmpty()) {
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
                execute(level, pending.origin);
            }
        }

        List<PendingRemoval> pendingRemovals = List.copyOf(PENDING_REMOVALS);
        for (PendingRemoval pending : pendingRemovals) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                PENDING_REMOVALS.remove(pending);
                pending.entity.discard();
                PENDING_PARTICLES.add(
                        new PendingParticles(level, pending.gfxX, pending.gfxY, pending.gfxZ, PARTICLE_DURATION_TICKS));
            }
        }

        List<PendingParticles> pendingParticles = List.copyOf(PENDING_PARTICLES);
        for (PendingParticles pending : pendingParticles) {
            if (pending.level != level) {
                continue;
            }
            emitParticles(level, pending.x, pending.y, pending.z);
            pending.ticksRemaining--;
            if (pending.ticksRemaining <= 0) {
                PENDING_PARTICLES.remove(pending);
            }
        }
    }

    private static void emitParticles(ServerLevel level, double x, double y, double z) {
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                x, y, z, 6, 1.5D, 2.0D, 1.5D, 0.05D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE,
                x, y, z, 4, 1.5D, 2.0D, 1.5D, 0.02D);
    }

    private static void execute(ServerLevel level, BlockPos origin) {
        // Spawn GFX: ItemDisplay 2 blocks above target block center, scaled to 5 blocks tall.
        // All Display setters are private in 1.21.1, so configure via readAdditionalSaveData
        // called from an anonymous subclass instance initializer (safe in a static method).
        final ItemStack displayStack = new ItemStack(ModItems.SPLIT_PUNCH_GFX.get());
        final Tag transformTag = Transformation.CODEC
                .encodeStart(NbtOps.INSTANCE,
                        new Transformation(new Vector3f(0, 0, 0), new Quaternionf(),
                                new Vector3f(8.0F, -8.0F, 8.0F), new Quaternionf()))
                .result()
                .orElse(null);

        double gfxX = origin.getX() + 0.5D;
        double gfxY = origin.getY() + 6.0D;
        double gfxZ = origin.getZ() + 0.5D;

        // Anonymous subclass so the instance initializer can access the protected
        // readAdditionalSaveData method as a subclass member call.
        Display.ItemDisplay gfx = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", displayStack.save(registryAccess()));
            cfg.putString("billboard", "vertical");
            if (transformTag != null) {
                cfg.put("transformation", transformTag);
            }
            readAdditionalSaveData(cfg);
        }};
        gfx.setPos(gfxX, gfxY, gfxZ);
        level.addFreshEntity(gfx);
        PENDING_REMOVALS.add(new PendingRemoval(level, gfx, gfxX, gfxY, gfxZ, GFX_DURATION_TICKS));

        // Deal 8 damage to all living entities (non-player) within 2-block radius of target block center
        Vec3 center = Vec3.atCenterOf(origin);
        AABB damageBox = new AABB(
                center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, damageBox,
                e -> e.isAlive() && !(e instanceof Player))) {
            entity.hurt(level.damageSources().magic(), DAMAGE);
        }

        // Play sound at target block
        LocalSoundHelper.playLocalized(level,
                new Vec3(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D),
                ModSounds.SPLIT_PUNCH.get(), 32.0D, 1.0F, 1.0F);
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

    private static final class PendingRemoval {
        private final ServerLevel level;
        private final Display.ItemDisplay entity;
        private final double gfxX;
        private final double gfxY;
        private final double gfxZ;
        private int delayTicks;

        private PendingRemoval(ServerLevel level, Display.ItemDisplay entity,
                double gfxX, double gfxY, double gfxZ, int delayTicks) {
            this.level = level;
            this.entity = entity;
            this.gfxX = gfxX;
            this.gfxY = gfxY;
            this.gfxZ = gfxZ;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingParticles {
        private final ServerLevel level;
        private final double x;
        private final double y;
        private final double z;
        private int ticksRemaining;

        private PendingParticles(ServerLevel level, double x, double y, double z, int ticksRemaining) {
            this.level = level;
            this.x = x;
            this.y = y;
            this.z = z;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
