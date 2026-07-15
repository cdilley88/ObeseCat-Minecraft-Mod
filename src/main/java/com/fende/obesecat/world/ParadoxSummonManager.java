package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public final class ParadoxSummonManager {
    public static final double RANGE = 50.0D;
    public static final int COOLDOWN_TICKS = 20 * 15;
    public static final int CAST_TICKS = 30;
    public static final int ATTACK_TICKS = 20 * 5;
    public static final double ATTACK_RADIUS = 12.0D;
    public static final float DAMAGE_PER_PULSE = 40.0F;

    private static final List<Cast> CASTS = new ArrayList<>();
    private static final List<Attack> ATTACKS = new ArrayList<>();
    private static final int[] COLORS = {0xFF2BD6, 0x00F5FF, 0x8A2BFF, 0xFFF200, 0x39FF88};
    private static final int[] CHROMATIC_COLORS = {0x00FFFF, 0xFF2020, 0x35FF58};

    private ParadoxSummonManager() {}

    public static void schedule(ServerLevel level, Player caster, BlockPos target) {
        CASTS.add(new Cast(level, caster.getUUID(), target.immutable(), CAST_TICKS));
        LocalSoundHelper.playLocalized(level, caster.position(), ModSounds.SUMMON_CAST.get(), 32.0D, 1.0F, 0.9F);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (Cast cast : List.copyOf(CASTS)) {
            if (cast.level != level) continue;
            ServerPlayer caster = level.getServer().getPlayerList().getPlayer(cast.casterId);
            if (caster != null && caster.level() == level) emitCastingHelix(level, caster, CAST_TICKS - cast.ticks);
            if (--cast.ticks <= 0) {
                CASTS.remove(cast);
                manifest(level, cast.target);
            }
        }

        for (Attack attack : List.copyOf(ATTACKS)) {
            if (attack.level != level) continue;
            int age = ATTACK_TICKS - attack.ticks;
            animateRealityBreak(level, attack, age);
            if (age % 20 == 0) damagePulse(level, attack.center);
            if (--attack.ticks <= 0) {
                ATTACKS.remove(attack);
                attack.displays.forEach(Display.ItemDisplay::discard);
                attack.warpEffect.discard();
                level.sendParticles(ParticleTypes.PORTAL, attack.center.x, attack.center.y + 2.0D,
                        attack.center.z, 180, 7.0D, 4.0D, 7.0D, 1.0D);
            }
        }
    }

    private static void emitCastingHelix(ServerLevel level, Player caster, int age) {
        Vec3 base = caster.position();
        for (int arm = 0; arm < 3; arm++) {
            double angle = age * 0.48D + arm * Math.PI * 2.0D / 3.0D;
            double radius = 1.15D - Math.min(age, CAST_TICKS) * 0.018D;
            double y = 0.15D + (age % 24) * 0.095D;
            level.sendParticles(dust(COLORS[(age + arm) % COLORS.length]),
                    base.x + Math.cos(angle) * radius, base.y + y,
                    base.z + Math.sin(angle) * radius, 2, 0.03D, 0.03D, 0.03D, 0.0D);
        }
        if (age % 4 == 0) level.sendParticles(ParticleTypes.ENCHANT, base.x, base.y + 1.0D, base.z,
                10, 1.1D, 0.8D, 1.1D, 0.15D);
    }

    private static void manifest(ServerLevel level, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        LocalSoundHelper.playLocalized(level, center, ModSounds.PARADOX_SUMMON.get(), 48.0D, 1.0F, 1.0F);
        List<Display.ItemDisplay> displays = new ArrayList<>();
        ParadoxVisuals.Effect warpEffect = ParadoxVisuals.spawn(level, target);
        ATTACKS.add(new Attack(level, center, displays, warpEffect, ATTACK_TICKS));
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 5.0D, center.z, 2, 0.5D, 0.5D, 0.5D, 0.0D);
        for (int channel = 0; channel < CHROMATIC_COLORS.length; channel++) {
            level.sendParticles(dust(CHROMATIC_COLORS[channel]),
                    center.x + (channel - 1) * 0.35D, center.y + 5.0D, center.z,
                    55, 3.2D, 3.5D, 3.2D, 0.08D);
        }
    }

    private static Display.ItemDisplay spawnImage(ServerLevel level, Vec3 pos, float scale) {
        ItemStack stack = new ItemStack(ModItems.PARADOX_GFX.get());
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(scale, scale, scale), new Quaternionf())).result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putInt("view_range", 64);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos);
        level.addFreshEntity(display);
        return display;
    }

    private static void animateRealityBreak(ServerLevel level, Attack attack, int age) {
        double phase = age * 0.37D;
        attack.warpEffect.tick(age);
        for (int i = 0; i < attack.displays.size(); i++) {
            Display.ItemDisplay display = attack.displays.get(i);
            Vec3 anchor = i == 0 ? attack.center.add(0, 5, 0) : attack.center.add(
                    Math.cos(i * 0.9D) * 5.0D, 3.0D + i % 3, Math.sin(i * 0.9D) * 5.0D);
            double tear = ((age + i * 7) % 13 == 0) ? 1.2D : 0.12D;
            display.setPos(anchor.x + Math.sin(phase + i) * tear,
                    anchor.y + Math.cos(phase * 1.7D + i) * tear * 0.35D,
                    anchor.z + Math.cos(phase + i) * tear);
        }

        net.minecraft.util.RandomSource chaos = net.minecraft.util.RandomSource.create(
                Double.doubleToLongBits(attack.center.x + attack.center.z) ^ (age * 0x9E3779B97F4A7C15L));
        for (int i = 0; i < 14; i++) {
            double angle = chaos.nextDouble() * Math.PI * 2.0D;
            double radius = 1.0D + chaos.nextDouble() * ATTACK_RADIUS;
            double y = 0.15D + chaos.nextDouble() * 7.5D;
            double x = attack.center.x + Math.cos(angle) * radius;
            double z = attack.center.z + Math.sin(angle) * radius;
            level.sendParticles(dust(CHROMATIC_COLORS[chaos.nextInt(CHROMATIC_COLORS.length)]),
                    x, attack.center.y + y, z, 3 + chaos.nextInt(4), 0.15D, 0.4D, 0.15D, 0.0D);
            if (i % 4 == 0) level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, attack.center.y + y, z,
                    5, 0.4D, 0.5D, 0.4D, 0.08D);
        }
        if (age % 5 == 0) {
            level.sendParticles(ParticleTypes.SCULK_CHARGE_POP, attack.center.x, attack.center.y + 0.5D,
                    attack.center.z, 18, ATTACK_RADIUS * 0.6D, 1.0D, ATTACK_RADIUS * 0.6D, 0.05D);
        }
    }

    private static void damagePulse(ServerLevel level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(ATTACK_RADIUS, 8.0D, ATTACK_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && !(entity instanceof Player))) {
            entity.hurt(level.damageSources().magic(), DAMAGE_PER_PULSE);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D,
                    entity.getZ(), 35, entity.getBbWidth(), entity.getBbHeight() / 2.0D, entity.getBbWidth(), 0.5D);
        }
    }

    private static DustParticleOptions dust(int rgb) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F), 1.25F);
    }

    private static final class Cast {
        final ServerLevel level; final UUID casterId; final BlockPos target; int ticks;
        Cast(ServerLevel level, UUID casterId, BlockPos target, int ticks) {
            this.level = level; this.casterId = casterId; this.target = target; this.ticks = ticks;
        }
    }

    private static final class Attack {
        final ServerLevel level; final Vec3 center; final List<Display.ItemDisplay> displays;
        final ParadoxVisuals.Effect warpEffect; int ticks;
        Attack(ServerLevel level, Vec3 center, List<Display.ItemDisplay> displays,
                ParadoxVisuals.Effect warpEffect, int ticks) {
            this.level = level; this.center = center; this.displays = displays;
            this.warpEffect = warpEffect; this.ticks = ticks;
        }
    }
}
