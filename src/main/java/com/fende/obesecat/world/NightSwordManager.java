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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class NightSwordManager {
    public static final double RANGE = 25.0D;
    public static final int ANIMATION_TICKS = 80;
    public static final int STAB_TICK = 40;
    public static final int COOLDOWN_TICKS = 100;
    public static final float DAMAGE = 10.0F;
    public static final double DAMAGE_RADIUS = 3.5D;
    public static final float HEAL_AMOUNT = 4.0F;

    private static final int STAB_RISE_TICKS = 8;
    private static final float SKULL_SCALE = 3.25F;
    private static final float STAB_SCALE = 6.0F;
    private static final DustParticleOptions SHADOW =
            new DustParticleOptions(new Vector3f(0.18F, 0.02F, 0.28F), 1.35F);
    private static final DustParticleOptions BLOOD =
            new DustParticleOptions(new Vector3f(0.65F, 0.01F, 0.08F), 1.1F);
    private static final List<ActiveCast> ACTIVE_CASTS = new ArrayList<>();

    private NightSwordManager() {}

    public static void schedule(ServerLevel level, Player caster, LivingEntity target) {
        Vec3 anchor = target.position();
        Display.ItemDisplay skull = spawnDisplay(level, ModItems.NIGHT_SWORD_SKULL_GFX.get(),
                anchor.add(0.0D, 5.0D, 0.0D), SKULL_SCALE);
        LocalSoundHelper.playLocalized(level, anchor, ModSounds.NIGHT_SWORD_DRONE.get(), 36.0D, 1.0F, 1.0F);
        ACTIVE_CASTS.add(new ActiveCast(level, caster.getUUID(), target.getUUID(), anchor, skull));
    }

    public static void schedule(ServerLevel level, Player caster, Vec3 anchor) {
        Display.ItemDisplay skull = spawnDisplay(level, ModItems.NIGHT_SWORD_SKULL_GFX.get(),
                anchor.add(0.0D, 5.0D, 0.0D), SKULL_SCALE);
        LocalSoundHelper.playLocalized(level, anchor, ModSounds.NIGHT_SWORD_DRONE.get(), 36.0D, 1.0F, 1.0F);
        ACTIVE_CASTS.add(new ActiveCast(level, caster.getUUID(), null, anchor, skull));
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || ACTIVE_CASTS.isEmpty()) return;

        for (ActiveCast cast : List.copyOf(ACTIVE_CASTS)) {
            if (cast.level != level) continue;
            cast.age++;
            LivingEntity target = livingEntity(level, cast.targetId);
            if (target != null && target.isAlive()) cast.anchor = target.position();
            cast.skull.setPos(cast.anchor.add(0.0D, 5.0D, 0.0D));
            emitShadowSpiral(level, cast.anchor, cast.age);

            if (cast.age == STAB_TICK) {
                cast.stab = spawnDisplay(level, ModItems.NIGHT_SWORD_STAB_GFX.get(),
                        cast.anchor.add(0.0D, -3.0D, 0.0D), STAB_SCALE);
                LocalSoundHelper.playLocalized(level, cast.anchor, ModSounds.NIGHT_SWORD_STAB.get(),
                        36.0D, 1.0F, 1.0F);
                cast.damagedEnemy = damageArea(level, cast.anchor);
            }

            if (cast.stab != null) {
                double rise = Math.min(1.0D, (cast.age - STAB_TICK) / (double) STAB_RISE_TICKS);
                cast.stab.setPos(cast.anchor.add(0.0D, -3.0D + rise * 6.0D, 0.0D));
            }
            if (cast.age >= ANIMATION_TICKS) finish(level, cast);
        }
    }

    private static void finish(ServerLevel level, ActiveCast cast) {
        ACTIVE_CASTS.remove(cast);
        cast.skull.discard();
        if (cast.stab != null) cast.stab.discard();
        ServerPlayer caster = level.getServer().getPlayerList().getPlayer(cast.casterId);
        if (cast.damagedEnemy && caster != null && caster.level() == level && caster.isAlive()) {
            caster.heal(HEAL_AMOUNT);
            level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.2D, caster.getZ(),
                    8, 0.5D, 0.7D, 0.5D, 0.05D);
        }
        level.sendParticles(ParticleTypes.SOUL, cast.anchor.x, cast.anchor.y + 3.0D, cast.anchor.z,
                24, 0.8D, 2.5D, 0.8D, 0.03D);
    }

    private static void emitShadowSpiral(ServerLevel level, Vec3 anchor, int age) {
        for (int arm = 0; arm < 3; arm++) {
            double angle = age * 0.18D + arm * Math.PI * 2.0D / 3.0D;
            double radius = 1.35D - Math.min(age, STAB_TICK) * 0.012D;
            double y = 0.2D + (age % 40) * 0.13D;
            level.sendParticles(arm == 0 ? BLOOD : SHADOW,
                    anchor.x + Math.cos(angle) * radius, anchor.y + y,
                    anchor.z + Math.sin(angle) * radius, 1, 0.0D, 0.015D, 0.0D, 0.0D);
        }
        if (age == STAB_TICK) {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, anchor.x, anchor.y + 2.5D, anchor.z,
                    42, 0.7D, 2.8D, 0.7D, 0.08D);
        }
    }

    private static boolean damageArea(ServerLevel level, Vec3 center) {
        AABB damageBox = new AABB(
                center.x - DAMAGE_RADIUS, center.y - 1.0D, center.z - DAMAGE_RADIUS,
                center.x + DAMAGE_RADIUS, center.y + 6.0D, center.z + DAMAGE_RADIUS);
        boolean damagedEnemy = false;
        for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, damageBox,
                candidate -> candidate.isAlive() && !(candidate instanceof Player))) {
            if (enemy.hurt(level.damageSources().magic(), DAMAGE)) {
                damagedEnemy = true;
            }
        }
        return damagedEnemy;
    }

    private static LivingEntity livingEntity(ServerLevel level, UUID id) {
        if (id == null) return null;
        Entity entity = level.getEntity(id);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static Display.ItemDisplay spawnDisplay(ServerLevel level, Item item, Vec3 position, float scale) {
        ItemStack stack = new ItemStack(item);
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(scale, scale, scale), new Quaternionf())).result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 48);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(position);
        level.addFreshEntity(display);
        return display;
    }

    private static final class ActiveCast {
        private final ServerLevel level;
        private final UUID casterId;
        private final UUID targetId;
        private Vec3 anchor;
        private final Display.ItemDisplay skull;
        private Display.ItemDisplay stab;
        private boolean damagedEnemy;
        private int age;

        private ActiveCast(ServerLevel level, UUID casterId, UUID targetId, Vec3 anchor,
                Display.ItemDisplay skull) {
            this.level = level;
            this.casterId = casterId;
            this.targetId = targetId;
            this.anchor = anchor;
            this.skull = skull;
        }
    }
}
