package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ShellbustStabManager {
    public static final double RANGE = 25.0D;
    public static final float DAMAGE = 10.0F;
    public static final double RADIUS = 3.5D;
    public static final float ARMOR_DROP_CHANCE = 0.50F;
    public static final int CAST_DELAY_TICKS = 12;
    public static final int COOLDOWN_TICKS = 60;

    private static final int FRAME_TICKS = 3;
    private static final int FINAL_FRAME_TICKS = 7;
    private static final float OVERLAY_SCALE = 8.0F;
    private static final DustParticleOptions CRIMSON_DUST = new DustParticleOptions(new Vector3f(0.9F, 0.05F, 0.08F), 1.35F);
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.65F, 0.08F), 0.9F);
    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingOverlay> PENDING_OVERLAYS = new ArrayList<>();

    private ShellbustStabManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        LocalSoundHelper.playLocalized(level, center, ModSounds.SHELLBUST_STAB_CHARGE.get(), 28.0D, 1.0F, 1.0F);
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), CAST_DELAY_TICKS));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || (PENDING_CASTS.isEmpty() && PENDING_OVERLAYS.isEmpty())) {
            return;
        }

        for (PendingCast pending : List.copyOf(PENDING_CASTS)) {
            if (pending.level != level) {
                continue;
            }

            pending.delayTicks--;
            emitLockOnParticles(level, pending.origin, pending.delayTicks);
            if (pending.delayTicks <= 0) {
                PENDING_CASTS.remove(pending);
                execute(level, pending.origin);
            }
        }

        for (PendingOverlay overlay : List.copyOf(PENDING_OVERLAYS)) {
            if (overlay.level != level) {
                continue;
            }

            overlay.ticksRemaining--;
            if (overlay.ticksRemaining > 0) {
                continue;
            }

            overlay.display.discard();
            if (overlay.frame >= 3) {
                PENDING_OVERLAYS.remove(overlay);
                continue;
            }

            overlay.frame++;
            overlay.display = spawnOverlayFrame(level, overlay.origin, overlay.frame);
            overlay.ticksRemaining = overlay.frame == 3 ? FINAL_FRAME_TICKS : FRAME_TICKS;
        }
    }

    private static void execute(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atCenterOf(origin);
        LocalSoundHelper.playLocalized(level, center, ModSounds.SHELLBUST_STAB_IMPACT.get(), 32.0D, 1.0F, 1.0F);
        PENDING_OVERLAYS.add(new PendingOverlay(level, origin.immutable(),
                spawnOverlayFrame(level, origin, 1), 1, FRAME_TICKS));
        emitImpactParticles(level, center);

        AABB damageBox = new AABB(
                center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS
        );
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, damageBox,
                candidate -> candidate.isAlive() && !(candidate instanceof Player));
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), DAMAGE);
        }

        targets.stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
                .ifPresent(target -> tryDropBodyArmor(level, target));
    }

    private static Display.ItemDisplay spawnOverlayFrame(ServerLevel level, BlockPos origin, int frame) {
        Item frameItem = switch (frame) {
            case 2 -> ModItems.SHELLBUST_STAB_GFX_2.get();
            case 3 -> ModItems.SHELLBUST_STAB_GFX_3.get();
            default -> ModItems.SHELLBUST_STAB_GFX_1.get();
        };
        ItemStack displayStack = new ItemStack(frameItem);
        Tag transformTag = Transformation.CODEC
                .encodeStart(NbtOps.INSTANCE,
                        new Transformation(new Vector3f(), new Quaternionf(),
                                new Vector3f(OVERLAY_SCALE, OVERLAY_SCALE, OVERLAY_SCALE), new Quaternionf()))
                .result()
                .orElse(null);

        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", displayStack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 48);
            if (transformTag != null) {
                cfg.put("transformation", transformTag);
            }
            readAdditionalSaveData(cfg);
        }};
        display.setPos(origin.getX() + 0.5D, origin.getY() + 3.0D, origin.getZ() + 0.5D);
        level.addFreshEntity(display);
        return display;
    }

    static boolean tryDropBodyArmor(ServerLevel level, LivingEntity target) {
        ItemStack bodyArmor = target.getItemBySlot(EquipmentSlot.CHEST);
        if (bodyArmor.isEmpty() || level.random.nextFloat() >= ARMOR_DROP_CHANCE) {
            return false;
        }

        target.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        target.spawnAtLocation(bodyArmor.copy());
        return true;
    }

    private static void emitLockOnParticles(ServerLevel level, BlockPos origin, int ticksRemaining) {
        Vec3 center = Vec3.atCenterOf(origin).add(0.0D, 1.0D, 0.0D);
        double progress = 1.0D - Math.max(0, ticksRemaining) / (double) CAST_DELAY_TICKS;
        double radius = 1.6D - progress * 0.9D;
        double angle = ticksRemaining * 0.75D;
        for (int i = 0; i < 4; i++) {
            double armAngle = angle + i * Math.PI / 2.0D;
            level.sendParticles(CRIMSON_DUST,
                    center.x + Math.cos(armAngle) * radius,
                    center.y + Math.sin(armAngle * 2.0D) * 0.65D,
                    center.z + Math.sin(armAngle) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void emitImpactParticles(ServerLevel level, Vec3 center) {
        for (int i = 0; i < 32; i++) {
            double angle = i * (Math.PI * 2.0D / 32.0D);
            double radius = 0.35D + level.random.nextDouble() * 1.5D;
            double y = center.y + 0.2D + level.random.nextDouble() * 2.2D;
            double motionX = Math.cos(angle) * 0.22D;
            double motionZ = Math.sin(angle) * 0.22D;
            level.sendParticles(i % 3 == 0 ? GOLD_DUST : CRIMSON_DUST,
                    center.x + Math.cos(angle) * radius * 0.35D, y,
                    center.z + Math.sin(angle) * radius * 0.35D,
                    1, motionX, 0.04D, motionZ, 0.0D);
        }
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y + 1.0D, center.z, 36, 1.2D, 1.2D, 1.2D, 0.22D);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, center.x, center.y + 1.0D, center.z, 24, 1.0D, 1.0D, 1.0D, 0.15D);
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

    private static final class PendingOverlay {
        private final ServerLevel level;
        private final BlockPos origin;
        private Display.ItemDisplay display;
        private int frame;
        private int ticksRemaining;

        private PendingOverlay(ServerLevel level, BlockPos origin, Display.ItemDisplay display,
                int frame, int ticksRemaining) {
            this.level = level;
            this.origin = origin;
            this.display = display;
            this.frame = frame;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
