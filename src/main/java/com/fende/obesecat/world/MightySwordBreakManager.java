package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.mojang.math.Transformation;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MightySwordBreakManager {
    public static final double RANGE = 25.0D;
    public static final float DAMAGE = 10.0F;
    public static final double RADIUS = 3.5D;
    public static final float EQUIPMENT_DROP_CHANCE = 0.50F;
    public static final int CAST_DELAY_TICKS = 12;
    public static final int COOLDOWN_TICKS = 60;

    private static final int METEOR_TICKS = 12;
    private static final float METEOR_SCALE = 4.5F;
    private static final int STUCK_HOLD_TICKS = 40;
    private static final int FADE_TICKS = 8;
    private static final int HELLCRY_FRAME_TICKS = 1;
    private static final int HELLCRY_HOLD_TICKS = 4;
    private static final int[] HELLCRY_SEQUENCE = {1, 2, 3, 4, 5, 6, 5, 4, 3, 2, 1};
    private static final float HELLCRY_SCALE = 8.0F;
    private static final int ICEWOLF_MANIFEST_TICKS = 5;
    private static final int ICEWOLF_HOLD_TICKS = 15;
    private static final int ICEWOLF_FADE_TICKS = 8;
    private static final float ICEWOLF_SCALE = 8.0F;
    private static final List<PendingCast> PENDING_CASTS = new ArrayList<>();
    private static final List<PendingMeteor> PENDING_METEORS = new ArrayList<>();
    private static final List<PendingStuckBlade> PENDING_STUCK_BLADES = new ArrayList<>();
    private static final List<PendingHellcryOverlay> PENDING_HELLCRY_OVERLAYS = new ArrayList<>();
    private static final List<PendingIcewolf> PENDING_ICEWOLVES = new ArrayList<>();

    public enum Skill {
        BLASTAR_PUNCH(EquipmentSlot.HEAD, new Vector3f(1.0F, 0.7F, 0.1F)),
        HELLCRY_PUNCH(EquipmentSlot.MAINHAND, new Vector3f(0.7F, 0.1F, 0.95F)),
        ICEWOLF_BITE(EquipmentSlot.OFFHAND, new Vector3f(0.25F, 0.8F, 1.0F));

        private final EquipmentSlot slot;
        private final Vector3f color;

        Skill(EquipmentSlot slot, Vector3f color) {
            this.slot = slot;
            this.color = color;
        }
    }

    private MightySwordBreakManager() {
    }

    public static void schedule(ServerLevel level, BlockPos origin, Skill skill) {
        PENDING_CASTS.add(new PendingCast(level, origin.immutable(), skill, CAST_DELAY_TICKS));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || (PENDING_CASTS.isEmpty() && PENDING_METEORS.isEmpty() && PENDING_STUCK_BLADES.isEmpty()
                && PENDING_HELLCRY_OVERLAYS.isEmpty() && PENDING_ICEWOLVES.isEmpty())) {
            return;
        }
        for (PendingCast pending : List.copyOf(PENDING_CASTS)) {
            if (pending.level != level) {
                continue;
            }
            pending.delayTicks--;
            emitChargeParticles(level, pending.origin, pending.skill, pending.delayTicks);
            if (pending.delayTicks <= 0) {
                PENDING_CASTS.remove(pending);
                if (pending.skill == Skill.BLASTAR_PUNCH) {
                    beginBlastarMeteor(level, pending.origin);
                } else if (pending.skill == Skill.HELLCRY_PUNCH) {
                    beginHellcryStab(level, pending.origin);
                    executeImpact(level, pending.origin, pending.skill);
                } else if (pending.skill == Skill.ICEWOLF_BITE) {
                    beginIcewolfBite(level, pending.origin);
                } else {
                    executeImpact(level, pending.origin, pending.skill);
                }
            }
        }

        for (PendingMeteor meteor : List.copyOf(PENDING_METEORS)) {
            if (meteor.level != level) {
                continue;
            }
            meteor.display.discard();
            meteor.age++;
            if (meteor.age >= METEOR_TICKS) {
                PENDING_METEORS.remove(meteor);
                executeImpact(level, meteor.origin, Skill.BLASTAR_PUNCH);
                PENDING_STUCK_BLADES.add(new PendingStuckBlade(level, meteor.origin,
                        spawnStuckBlade(level, meteor.origin, METEOR_SCALE),
                        STUCK_HOLD_TICKS, FADE_TICKS));
                continue;
            }
            meteor.display = spawnMeteorFrame(level, meteor.origin, meteor.age);
            Vec3 pos = meteorPosition(meteor.origin, meteor.age);
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z,
                    5, 0.35D, 0.35D, 0.35D, 0.02D);
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z,
                    3, 0.3D, 0.3D, 0.3D, 0.01D);
        }

        for (PendingHellcryOverlay overlay : List.copyOf(PENDING_HELLCRY_OVERLAYS)) {
            if (overlay.level != level) continue;
            overlay.ticksRemaining--;
            if (overlay.ticksRemaining > 0) continue;
            overlay.display.discard();
            if (overlay.step >= HELLCRY_SEQUENCE.length - 1) {
                PENDING_HELLCRY_OVERLAYS.remove(overlay);
                continue;
            }
            overlay.step++;
            int frame = HELLCRY_SEQUENCE[overlay.step];
            overlay.display = spawnHellcryFrame(level, overlay.origin, frame);
            overlay.ticksRemaining = frame == 6 ? HELLCRY_HOLD_TICKS : HELLCRY_FRAME_TICKS;
        }

        for (PendingIcewolf icewolf : List.copyOf(PENDING_ICEWOLVES)) {
            if (icewolf.level != level) continue;
            emitIcewolfVortex(level, icewolf.origin, icewolf.age++);
            if (icewolf.manifestTicks > 0) {
                icewolf.manifestTicks--;
                if (icewolf.manifestTicks == 0) {
                    executeImpact(level, icewolf.origin, Skill.ICEWOLF_BITE);
                    icewolf.display = spawnIcewolfImage(level, icewolf.origin, ICEWOLF_SCALE);
                }
                continue;
            }
            if (icewolf.holdTicks > 0) {
                icewolf.holdTicks--;
                continue;
            }
            if (icewolf.display != null) icewolf.display.discard();
            icewolf.fadeTicks--;
            if (icewolf.fadeTicks <= 0) {
                PENDING_ICEWOLVES.remove(icewolf);
                Vec3 center = Vec3.atCenterOf(icewolf.origin).add(0.0D, 2.0D, 0.0D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y, center.z,
                        45, 2.0D, 2.0D, 2.0D, 0.08D);
                continue;
            }
            float fadeScale = ICEWOLF_SCALE * icewolf.fadeTicks / (float) ICEWOLF_FADE_TICKS;
            icewolf.display = spawnIcewolfImage(level, icewolf.origin, fadeScale);
        }

        for (PendingStuckBlade stuck : List.copyOf(PENDING_STUCK_BLADES)) {
            if (stuck.level != level) continue;
            if (stuck.holdTicks > 0) {
                stuck.holdTicks--;
                continue;
            }
            stuck.display.discard();
            stuck.fadeTicks--;
            if (stuck.fadeTicks <= 0) {
                PENDING_STUCK_BLADES.remove(stuck);
                level.sendParticles(ParticleTypes.SMOKE, stuck.origin.getX() + 0.5D,
                        stuck.origin.getY() + 0.6D, stuck.origin.getZ() + 0.5D,
                        12, 0.5D, 0.25D, 0.5D, 0.02D);
                continue;
            }
            float fadeProgress = stuck.fadeTicks / (float) FADE_TICKS;
            stuck.display = spawnStuckBlade(level, stuck.origin, METEOR_SCALE * fadeProgress);
            level.sendParticles(dust(Skill.BLASTAR_PUNCH, 0.8F), stuck.origin.getX() + 0.5D,
                    stuck.origin.getY() + 0.8D, stuck.origin.getZ() + 0.5D,
                    5, 0.5D, 0.45D, 0.5D, 0.0D);
        }
    }

    private static void beginBlastarMeteor(ServerLevel level, BlockPos origin) {
        LocalSoundHelper.playLocalized(level, Vec3.atCenterOf(origin),
                ModSounds.BLASTAR_PUNCH_IMPACT.get(), 40.0D, 1.0F, 1.0F);
        PENDING_METEORS.add(new PendingMeteor(level, origin.immutable(),
                spawnMeteorFrame(level, origin, 0), 0));
    }

    private static void beginHellcryStab(ServerLevel level, BlockPos origin) {
        PENDING_HELLCRY_OVERLAYS.add(new PendingHellcryOverlay(level, origin.immutable(),
                spawnHellcryFrame(level, origin, 1), 1, HELLCRY_FRAME_TICKS));
    }

    private static Display.ItemDisplay spawnHellcryFrame(ServerLevel level, BlockPos origin, int frame) {
        ItemStack stack = new ItemStack(switch (frame) {
            case 2 -> ModItems.HELLCRY_PUNCH_GFX_2.get();
            case 3 -> ModItems.HELLCRY_PUNCH_GFX_3.get();
            case 4 -> ModItems.HELLCRY_PUNCH_GFX_4.get();
            case 5 -> ModItems.HELLCRY_PUNCH_GFX_5.get();
            case 6 -> ModItems.HELLCRY_PUNCH_GFX_6.get();
            default -> ModItems.HELLCRY_PUNCH_GFX_1.get();
        });
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(HELLCRY_SCALE, HELLCRY_SCALE, 1.0F), new Quaternionf()))
                .result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 64);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(origin.getX() + 0.5D, origin.getY() + 3.0D, origin.getZ() + 0.5D);
        level.addFreshEntity(display);
        return display;
    }

    private static void beginIcewolfBite(ServerLevel level, BlockPos origin) {
        PENDING_ICEWOLVES.add(new PendingIcewolf(level, origin.immutable(),
                ICEWOLF_MANIFEST_TICKS, ICEWOLF_HOLD_TICKS, ICEWOLF_FADE_TICKS));
    }

    private static void emitIcewolfVortex(ServerLevel level, BlockPos origin, int age) {
        Vec3 center = Vec3.atCenterOf(origin);
        for (int arm = 0; arm < 5; arm++) {
            double angle = age * 0.8D + arm * Math.PI * 2.0D / 5.0D;
            for (int step = 0; step < 4; step++) {
                double radius = 0.55D + step * 0.55D;
                double y = 0.25D + step * 0.55D + Math.sin(angle + step) * 0.3D;
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        center.x + Math.cos(angle + step * 0.35D) * radius, center.y + y,
                        center.z + Math.sin(angle + step * 0.35D) * radius,
                        2, 0.12D, 0.15D, 0.12D, 0.025D);
            }
        }
    }

    private static Display.ItemDisplay spawnIcewolfImage(ServerLevel level, BlockPos origin, float scale) {
        ItemStack stack = new ItemStack(ModItems.ICEWOLF_BITE_GFX.get());
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(scale, scale, 1.0F), new Quaternionf()))
                .result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 64);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(origin.getX() + 0.5D, origin.getY() + 3.0D, origin.getZ() + 0.5D);
        level.addFreshEntity(display);
        return display;
    }

    private static Display.ItemDisplay spawnStuckBlade(ServerLevel level, BlockPos origin, float scale) {
        ItemStack stack = new ItemStack(ModItems.BLASTAR_PUNCH_GFX.get());
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.toRadians(90.0D)).rotateY(0.2F);
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), rotation,
                        new Vector3f(scale, scale, 1.0F), new Quaternionf()))
                .result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 64);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(origin.getX() + 0.5D, origin.getY() + 2.45D, origin.getZ() + 0.5D);
        level.addFreshEntity(display);
        return display;
    }
    private static Display.ItemDisplay spawnMeteorFrame(ServerLevel level, BlockPos origin, int age) {
        ItemStack stack = new ItemStack(ModItems.BLASTAR_PUNCH_GFX.get());
        float spin = age * 1.25F;
        Quaternionf rotation = new Quaternionf().rotateZ(spin).rotateY(0.35F);
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), rotation,
                        new Vector3f(METEOR_SCALE, METEOR_SCALE, 1.0F), new Quaternionf()))
                .result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 64);
            if (transform != null) {
                cfg.put("transformation", transform);
            }
            readAdditionalSaveData(cfg);
        }};
        display.setPos(meteorPosition(origin, age));
        level.addFreshEntity(display);
        return display;
    }

    private static Vec3 meteorPosition(BlockPos origin, int age) {
        double progress = age / (double) METEOR_TICKS;
        double eased = progress * progress;
        Vec3 target = Vec3.atCenterOf(origin).add(0.0D, 1.1D, 0.0D);
        return target.add(7.0D * (1.0D - progress), 13.0D * (1.0D - eased),
                -5.0D * (1.0D - progress));
    }

    private static void executeImpact(ServerLevel level, BlockPos origin, Skill skill) {
        Vec3 center = Vec3.atCenterOf(origin);
        if (skill != Skill.BLASTAR_PUNCH) {
            LocalSoundHelper.playLocalized(level, center, impactSound(skill), 32.0D, 1.0F, 1.0F);
        }
        emitImpactParticles(level, center, skill);

        AABB damageBox = new AABB(
                center.x - RADIUS, center.y - RADIUS, center.z - RADIUS,
                center.x + RADIUS, center.y + RADIUS, center.z + RADIUS
        );
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, damageBox,
                target -> target.isAlive() && !(target instanceof Player));
        for (LivingEntity target : targets) {
            target.hurt(level.damageSources().magic(), DAMAGE);
        }
        targets.stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(center)))
                .ifPresent(target -> tryDropEquipment(level, target, skill.slot));
    }

    static boolean tryDropEquipment(ServerLevel level, LivingEntity target, EquipmentSlot slot) {
        ItemStack equipped = target.getItemBySlot(slot);
        if (equipped.isEmpty() || level.random.nextFloat() >= EQUIPMENT_DROP_CHANCE) {
            return false;
        }
        target.setItemSlot(slot, ItemStack.EMPTY);
        target.spawnAtLocation(equipped.copy());
        return true;
    }

    private static SoundEvent impactSound(Skill skill) {
        return switch (skill) {
            case BLASTAR_PUNCH -> ModSounds.BLASTAR_PUNCH_IMPACT.get();
            case HELLCRY_PUNCH -> ModSounds.HELLCRY_PUNCH_IMPACT.get();
            case ICEWOLF_BITE -> ModSounds.ICEWOLF_BITE_IMPACT.get();
        };
    }

    private static DustParticleOptions dust(Skill skill, float scale) {
        return new DustParticleOptions(skill.color, scale);
    }

    private static void emitChargeParticles(ServerLevel level, BlockPos origin, Skill skill, int ticksRemaining) {
        Vec3 center = Vec3.atCenterOf(origin).add(0.0D, 1.0D, 0.0D);
        double radius = 0.7D + Math.max(0, ticksRemaining) * 0.07D;
        double angle = ticksRemaining * 0.8D;
        for (int i = 0; i < 5; i++) {
            double phase = angle + i * Math.PI * 2.0D / 5.0D;
            level.sendParticles(dust(skill, 1.15F),
                    center.x + Math.cos(phase) * radius,
                    center.y + i * 0.28D,
                    center.z + Math.sin(phase) * radius,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static void emitImpactParticles(ServerLevel level, Vec3 center, Skill skill) {
        switch (skill) {
            case BLASTAR_PUNCH -> {
                level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.8D, center.z,
                        8, 1.2D, 0.7D, 1.2D, 0.0D);
                level.sendParticles(ParticleTypes.FIREWORK, center.x, center.y + 1.2D, center.z,
                        36, 1.8D, 1.2D, 1.8D, 0.18D);
            }
            case HELLCRY_PUNCH -> {
                level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 1.0D, center.z,
                        3, 0.4D, 0.5D, 0.4D, 0.0D);
                level.sendParticles(dust(skill, 1.4F), center.x, center.y + 1.0D, center.z,
                        55, 2.0D, 1.3D, 2.0D, 0.12D);
            }
            case ICEWOLF_BITE -> {
                level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 1.0D, center.z,
                        65, 2.2D, 1.5D, 2.2D, 0.15D);
                level.sendParticles(dust(skill, 1.3F), center.x, center.y + 1.0D, center.z,
                        42, 1.8D, 1.2D, 1.8D, 0.05D);
            }
        }
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, center.x, center.y + 1.0D, center.z,
                24, 1.2D, 1.0D, 1.2D, 0.18D);
    }

    private static final class PendingCast {
        private final ServerLevel level;
        private final BlockPos origin;
        private final Skill skill;
        private int delayTicks;

        private PendingCast(ServerLevel level, BlockPos origin, Skill skill, int delayTicks) {
            this.level = level;
            this.origin = origin;
            this.skill = skill;
            this.delayTicks = delayTicks;
        }
    }


    private static final class PendingStuckBlade {
        private final ServerLevel level;
        private final BlockPos origin;
        private Display.ItemDisplay display;
        private int holdTicks;
        private int fadeTicks;

        private PendingStuckBlade(ServerLevel level, BlockPos origin, Display.ItemDisplay display,
                int holdTicks, int fadeTicks) {
            this.level = level;
            this.origin = origin;
            this.display = display;
            this.holdTicks = holdTicks;
            this.fadeTicks = fadeTicks;
        }
    }
    private static final class PendingMeteor {
        private final ServerLevel level;
        private final BlockPos origin;
        private Display.ItemDisplay display;
        private int age;

        private PendingMeteor(ServerLevel level, BlockPos origin, Display.ItemDisplay display, int age) {
            this.level = level;
            this.origin = origin;
            this.display = display;
            this.age = age;
        }
    }

    private static final class PendingHellcryOverlay {
        private final ServerLevel level;
        private final BlockPos origin;
        private Display.ItemDisplay display;
        private int step;
        private int ticksRemaining;

        private PendingHellcryOverlay(ServerLevel level, BlockPos origin, Display.ItemDisplay display,
                int step, int ticksRemaining) {
            this.level = level;
            this.origin = origin;
            this.display = display;
            this.step = step;
            this.ticksRemaining = ticksRemaining;
        }
    }

    private static final class PendingIcewolf {
        private final ServerLevel level;
        private final BlockPos origin;
        private Display.ItemDisplay display;
        private int manifestTicks;
        private int holdTicks;
        private int fadeTicks;
        private int age;

        private PendingIcewolf(ServerLevel level, BlockPos origin, int manifestTicks,
                int holdTicks, int fadeTicks) {
            this.level = level;
            this.origin = origin;
            this.manifestTicks = manifestTicks;
            this.holdTicks = holdTicks;
            this.fadeTicks = fadeTicks;
        }
    }
}






