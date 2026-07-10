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

public final class CrushPunchManager {
    public static final double RANGE = 25.0D;
    public static final float DAMAGE = 10.0F;
    public static final double RADIUS = 4.0D;
    public static final int CAST_DELAY_TICKS = 10;
    public static final int BLADE_DELAY_TICKS = 27;
    public static final int GFX_DURATION_TICKS = 20;
    public static final int COOLDOWN_TICKS = 60;
    public static final int STORM_DURATION_TICKS = 40;

    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingBlade> PENDING_BLADES = new ArrayList<>();
    private static final List<PendingStorm> PENDING_STORMS = new ArrayList<>();
    private static final List<PendingRemoval> PENDING_REMOVALS = new ArrayList<>();

    private CrushPunchManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (PENDING_CASTS.isEmpty() && PENDING_BLADES.isEmpty() && PENDING_STORMS.isEmpty() && PENDING_REMOVALS.isEmpty()) {
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
                executeCast(level, pending.origin);
            }
        }

        List<PendingBlade> pendingBlades = List.copyOf(PENDING_BLADES);
        for (PendingBlade pending : pendingBlades) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                PENDING_BLADES.remove(pending);
                executeBlade(level, pending.origin);
            }
        }

        List<PendingStorm> pendingStorms = List.copyOf(PENDING_STORMS);
        for (PendingStorm pending : pendingStorms) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            if (pending.delayTicks <= 0) {
                PENDING_STORMS.remove(pending);
            } else {
                spawnStormParticles(level, pending.origin);
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
            }
        }
    }

    private static void executeCast(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        LocalSoundHelper.playLocalized(level, center, ModSounds.CRUSH_PUNCH_BACK.get(), 32.0D, 1.0F, 1.0F);
        PENDING_STORMS.add(new PendingStorm(level, origin.immutable(), STORM_DURATION_TICKS));
        PENDING_BLADES.add(new PendingBlade(level, origin.immutable(), BLADE_DELAY_TICKS));
    }

    private static void executeBlade(ServerLevel level, BlockPos origin) {
        LocalSoundHelper.playLocalized(level, new Vec3(origin.getX() + 0.5D, origin.getY() + 0.5D, origin.getZ() + 0.5D),
                ModSounds.CRUSH_PUNCH_BLADE.get(), 32.0D, 1.0F, 1.0F);

        final ItemStack displayStack = new ItemStack(ModItems.CRUSH_PUNCH_GFX.get());
        final Tag transformTag = Transformation.CODEC
                .encodeStart(NbtOps.INSTANCE,
                        new Transformation(new Vector3f(0, 0, 0), new Quaternionf(),
                                new Vector3f(8.0F, -8.0F, 8.0F), new Quaternionf()))
                .result()
                .orElse(null);

        double gfxX = origin.getX() + 0.5D;
        double gfxY = origin.getY() + 5.0D;
        double gfxZ = origin.getZ() + 0.5D;

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
        PENDING_REMOVALS.add(new PendingRemoval(level, gfx, GFX_DURATION_TICKS));

        Vec3 center = Vec3.atCenterOf(origin);
        AABB damageBox = new AABB(
                center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS
        );
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, damageBox,
                e -> e.isAlive() && !(e instanceof Player))) {
            entity.hurt(level.damageSources().magic(), DAMAGE);
        }
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

    private static void spawnStormParticles(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        for (int i = 0; i < 8; i++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 3.5D;
            double offsetY = level.random.nextDouble() * 4.0D + 1.0D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 3.5D;
            double swirlX = offsetX * 0.35D;
            double swirlZ = offsetZ * 0.35D;
            double swirlY = -0.04D - (offsetY * 0.01D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.WHITE_ASH,
                    center.x + offsetX, center.y + 0.5D + offsetY, center.z + offsetZ,
                    1, swirlX, swirlY, swirlZ, 0.0D);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE,
                    center.x + offsetX, center.y + 0.5D + offsetY, center.z + offsetZ,
                    1, swirlX, swirlY, swirlZ, 0.0D);
        }
    }

    private static final class PendingBlade {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;

        private PendingBlade(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingStorm {
        private final ServerLevel level;
        private final BlockPos origin;
        private int delayTicks;

        private PendingStorm(ServerLevel level, BlockPos origin, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.delayTicks = delayTicks;
        }
    }

    private static final class PendingRemoval {
        private final ServerLevel level;
        private final Display.ItemDisplay entity;
        private int delayTicks;

        private PendingRemoval(ServerLevel level, Display.ItemDisplay entity, int delayTicks) {
            this.level = level;
            this.entity = entity;
            this.delayTicks = delayTicks;
        }
    }
}
