package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class DarkSwordManager {
    public static final double RANGE = 25.0D;
    public static final int ANIMATION_TICKS = 80;
    public static final int STAB_TICK = 40;
    public static final int TWIST_TICKS = 10;
    public static final int COOLDOWN_TICKS = 100;
    public static final float DAMAGE = 10.0F;
    public static final double DAMAGE_RADIUS = 3.5D;
    public static final float TEMPORARY_LIFE_STEAL = 4.0F;

    private static final float BLADE_SCALE = 6.0F;
    private static final float TWIST_DEGREES = 160.0F;
    private static final DustParticleOptions VOID_DUST =
            new DustParticleOptions(new Vector3f(0.08F, 0.0F, 0.14F), 1.4F);
    private static final DustParticleOptions ARC_DUST =
            new DustParticleOptions(new Vector3f(0.55F, 0.05F, 0.75F), 1.1F);
    private static final DustParticleOptions TARGET_CYAN =
            new DustParticleOptions(new Vector3f(0.0F, 0.9F, 1.0F), 1.15F);
    private static final DustParticleOptions TARGET_PURPLE =
            new DustParticleOptions(new Vector3f(0.62F, 0.08F, 0.92F), 1.15F);
    private static final List<ActiveCast> ACTIVE_CASTS = new ArrayList<>();

    private DarkSwordManager() {}

    public static void schedule(ServerLevel level, Player caster, LivingEntity target) {
        start(level, caster, target.getUUID(), target.position());
    }

    public static void schedule(ServerLevel level, Player caster, Vec3 anchor) {
        start(level, caster, null, anchor);
    }

    private static void start(ServerLevel level, Player caster, UUID targetId, Vec3 anchor) {
        LocalSoundHelper.playLocalized(level, anchor, ModSounds.DARK_SWORD_DRONE.get(), 36.0D, 1.0F, 1.0F);
        ACTIVE_CASTS.add(new ActiveCast(level, caster.getUUID(), targetId, anchor));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_CASTS.isEmpty()) return;

        for (ActiveCast cast : List.copyOf(ACTIVE_CASTS)) {
            if (cast.level != level) continue;
            cast.age++;
            LivingEntity target = livingEntity(level, cast.targetId);
            if (target != null && target.isAlive()) cast.anchor = target.position();

            if (cast.age < STAB_TICK) {
                emitTargetingCharge(level, cast.anchor, cast.age);
            } else {
                if (cast.blade == null) {
                    cast.blade = spawnBlade(level, cast.anchor.add(0.0D, -3.0D, 0.0D));
                }
                float twistProgress = Math.min(1.0F, (cast.age - STAB_TICK) / (float) TWIST_TICKS);
                positionBlade(cast, easeOutCubic(twistProgress));
                emitCrescentParticles(level, cast.anchor, cast.age);
            }
            if (cast.age == STAB_TICK) {
                LocalSoundHelper.playLocalized(level, cast.anchor, ModSounds.DARK_SWORD_STAB.get(),
                        36.0D, 1.0F, 1.0F);
                cast.damagedEnemies = damageArea(level, cast.anchor);
            }
            if (cast.age >= ANIMATION_TICKS) finish(level, cast);
        }
    }

    private static void positionBlade(ActiveCast cast, float progress) {
        if (cast.blade == null) return;
        double y = -3.0D + progress * 6.0D;
        cast.blade.setPos(cast.anchor.add(0.0D, y, 0.0D));
        float radians = (float) Math.toRadians(TWIST_DEGREES * progress);
        cast.blade.updateTransformation(new Transformation(
                new Vector3f(), new Quaternionf().rotateZ(radians),
                new Vector3f(BLADE_SCALE, BLADE_SCALE, BLADE_SCALE), new Quaternionf()));
    }

    private static float easeOutCubic(float value) {
        float inverse = 1.0F - value;
        return 1.0F - inverse * inverse * inverse;
    }

    private static int damageArea(ServerLevel level, Vec3 center) {
        AABB damageBox = new AABB(
                center.x - DAMAGE_RADIUS, center.y - 1.0D, center.z - DAMAGE_RADIUS,
                center.x + DAMAGE_RADIUS, center.y + 6.0D, center.z + DAMAGE_RADIUS);
        int damagedEnemies = 0;
        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, damageBox,
                candidate -> candidate.isAlive() && !(candidate instanceof Player))) {
            if (enemy.hurt(level.damageSources().magic(), DAMAGE)) damagedEnemies++;
        }
        return damagedEnemies;
    }

    private static void finish(ServerLevel level, ActiveCast cast) {
        ACTIVE_CASTS.remove(cast);
        if (cast.blade != null) cast.blade.discard();
        ServerPlayer caster = level.getServer().getPlayerList().getPlayer(cast.casterId);
        if (caster != null && caster.level() == level && caster.isAlive() && cast.damagedEnemies > 0) {
            applyDrainReward(caster, cast.damagedEnemies);
            level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.2D, caster.getZ(),
                    8, 0.5D, 0.7D, 0.5D, 0.05D);
        }
        level.sendParticles(ParticleTypes.SOUL, cast.anchor.x, cast.anchor.y + 2.5D, cast.anchor.z,
                30, 1.2D, 2.4D, 1.2D, 0.04D);
    }

    private static void applyDrainReward(ServerPlayer caster, int damagedEnemies) {
        // Future MP/resource restoration belongs here. Life steal is the temporary reward until that system exists.
        caster.heal(TEMPORARY_LIFE_STEAL);
    }

    private static void emitCrescentParticles(ServerLevel level, Vec3 anchor, int age) {
        double progress = Math.min(1.0D, Math.max(0, age - STAB_TICK) / (double) TWIST_TICKS);
        double arc = Math.toRadians(TWIST_DEGREES * progress);
        for (int i = 0; i < 5; i++) {
            double angle = arc - i * 0.18D;
            double radius = 1.0D + i * 0.18D;
            level.sendParticles(i % 2 == 0 ? ARC_DUST : VOID_DUST,
                    anchor.x + Math.cos(angle) * radius,
                    anchor.y + 0.3D + progress * 4.8D + Math.sin(angle) * 0.7D,
                    anchor.z + Math.sin(angle) * radius,
                    1, 0.0D, 0.02D, 0.0D, 0.0D);
        }
    }

    private static void emitTargetingCharge(ServerLevel level, Vec3 anchor, int age) {
        double tightening = 1.0D - age / (double) STAB_TICK;
        double radius = 0.7D + tightening * 2.2D;
        double rise = 0.25D + age / (double) STAB_TICK * 2.0D;
        for (int arm = 0; arm < 2; arm++) {
            double angle = age * 0.34D + arm * Math.PI;
            double x = anchor.x + Math.cos(angle) * radius;
            double z = anchor.z + Math.sin(angle) * radius;
            level.sendParticles(arm == 0 ? TARGET_CYAN : TARGET_PURPLE,
                    x, anchor.y + rise, z, 2, 0.05D, 0.08D, 0.05D, 0.0D);
        }
        if (age % 5 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT,
                    anchor.x, anchor.y + 0.35D, anchor.z,
                    8, radius * 0.65D, 0.18D, radius * 0.65D, 0.08D);
        }
    }
    private static LivingEntity livingEntity(ServerLevel level, UUID id) {
        if (id == null) return null;
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static AnimatedBladeDisplay spawnBlade(ServerLevel level, Vec3 position) {
        ItemStack stack = new ItemStack(ModItems.DARK_SWORD_BLADE_GFX.get());
        AnimatedBladeDisplay display = new AnimatedBladeDisplay(level, stack);
        display.setPos(position);
        level.addFreshEntity(display);
        return display;
    }

    private static final class ActiveCast {
        private final ServerLevel level;
        private final UUID casterId;
        private final UUID targetId;
        private Vec3 anchor;
        private AnimatedBladeDisplay blade;
        private int damagedEnemies;
        private int age;

        private ActiveCast(ServerLevel level, UUID casterId, UUID targetId, Vec3 anchor) {
            this.level = level;
            this.casterId = casterId;
            this.targetId = targetId;
            this.anchor = anchor;
        }
    }
    private static final class AnimatedBladeDisplay extends Display.ItemDisplay {
        private final ItemStack bladeStack;

        private AnimatedBladeDisplay(ServerLevel level, ItemStack stack) {
            super(EntityType.ITEM_DISPLAY, level);
            bladeStack = stack.copy();
            updateTransformation(new Transformation(new Vector3f(), new Quaternionf(),
                    new Vector3f(BLADE_SCALE, BLADE_SCALE, BLADE_SCALE), new Quaternionf()));
        }

        private void updateTransformation(Transformation transformation) {
            Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE, transformation)
                    .result().orElse(null);
            if (transform == null) return;
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", bladeStack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 48);
            cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }
    }
}
