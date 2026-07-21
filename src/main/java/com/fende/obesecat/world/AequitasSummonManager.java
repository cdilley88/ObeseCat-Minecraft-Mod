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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
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

public final class AequitasSummonManager {
    public static final double RANGE = 50.0D;
    public static final int COOLDOWN_TICKS = 20 * 15;
    public static final int CAST_TICKS = 30;
    public static final int ATTACK_TICKS = 20 * 5;
    public static final double ATTACK_RADIUS = 12.0D;
    public static final float DAMAGE_PER_PULSE = 40.0F;

    private static final DustParticleOptions ELECTRIC_BLUE = dust(0x39BFFF, 1.15F);
    private static final DustParticleOptions STORM_PURPLE = dust(0x8B4DFF, 1.1F);
    private static final DustParticleOptions SOUL_CYAN = dust(0x35FFF2, 1.05F);
    private static final List<Cast> CASTS = new ArrayList<>();
    private static final List<Attack> ATTACKS = new ArrayList<>();

    private AequitasSummonManager() {}

    public static void schedule(ServerLevel level, Player caster, BlockPos target) {
        CASTS.add(new Cast(level, caster.getUUID(), target.immutable(), CAST_TICKS));
        LocalSoundHelper.playLocalized(level, caster.position(), ModSounds.SUMMON_CAST.get(), 32.0D, 1.0F, 1.02F);
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
            animateStormTriad(attack, age);
            emitStorm(level, attack, age);
            if (age % 20 == 0) damagePulse(level, attack.center);
            if (--attack.ticks <= 0) {
                ATTACKS.remove(attack);
                attack.images.forEach(Display.ItemDisplay::discard);
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        attack.center.x, attack.center.y + 2.0D, attack.center.z,
                        100, 6.5D, 3.0D, 6.5D, 0.08D);
            }
        }
    }

    private static void emitCastingHelix(ServerLevel level, Player caster, int age) {
        Vec3 base = caster.position();
        for (int arm = 0; arm < 3; arm++) {
            double angle = age * 0.48D + arm * Math.PI * 2.0D / 3.0D;
            double radius = 1.15D - Math.min(age, CAST_TICKS) * 0.018D;
            double y = 0.15D + (age % 24) * 0.095D;
            level.sendParticles(arm == 1 ? STORM_PURPLE : ELECTRIC_BLUE,
                    base.x + Math.cos(angle) * radius, base.y + y,
                    base.z + Math.sin(angle) * radius,
                    2, 0.03D, 0.03D, 0.03D, 0.0D);
        }
        if (age % 4 == 0) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    base.x, base.y + 1.0D, base.z,
                    9, 1.0D, 0.75D, 1.0D, 0.12D);
        }
    }

    private static void manifest(ServerLevel level, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        LocalSoundHelper.playLocalized(level, center, SoundEvents.LIGHTNING_BOLT_THUNDER,
                64.0D, 1.0F, 0.85F);
        LocalSoundHelper.playLocalized(level, center, SoundEvents.LIGHTNING_BOLT_IMPACT,
                48.0D, 1.0F, 1.0F);
        List<Display.ItemDisplay> images = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            images.add(spawnImage(level, center.add(0.0D, 5.0D, 0.0D)));
        }
        long seed = level.getGameTime() ^ target.asLong() ^ 0x4145515549544153L;
        ATTACKS.add(new Attack(level, center, images, seed, ATTACK_TICKS));
        level.sendParticles(ParticleTypes.FLASH,
                center.x, center.y + 5.0D, center.z,
                3, 0.7D, 0.7D, 0.7D, 0.0D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                center.x, center.y + 4.0D, center.z,
                90, 5.0D, 4.0D, 5.0D, 0.25D);
    }

    private static Display.ItemDisplay spawnImage(ServerLevel level, Vec3 pos) {
        ItemStack stack = new ItemStack(ModItems.AEQUITAS_GFX.get());
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(7.5F, 7.5F, 1.0F), new Quaternionf())).result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putFloat("view_range", 64.0F);
            cfg.putBoolean("Glowing", true);
            cfg.putInt("glow_color_override", 0x536DFF);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos);
        level.addFreshEntity(display);
        return display;
    }

    private static void animateStormTriad(Attack attack, int age) {
        double phase = age * 0.13D;
        double charge = 0.5D + 0.5D * Math.sin(age * 0.085D);
        double radius = 0.65D + charge * 3.2D;
        for (int i = 0; i < attack.images.size(); i++) {
            double angle = phase + i * Math.PI * 2.0D / attack.images.size();
            double y = 5.0D + Math.sin(phase * 2.0D + i * Math.PI * 2.0D / 3.0D) * 0.8D;
            Display.ItemDisplay image = attack.images.get(i);
            image.setPos(attack.center.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius));
            image.setYRot((float) Math.toDegrees(-angle));
        }
    }

    private static void emitStorm(ServerLevel level, Attack attack, int age) {
        RandomSource random = RandomSource.create(attack.seed ^ (age * 0x9E3779B97F4A7C15L));
        if (age % 4 == 0) {
            for (int bolt = 0; bolt < 3; bolt++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 1.0D + random.nextDouble() * ATTACK_RADIUS;
                double groundX = attack.center.x + Math.cos(angle) * radius;
                double groundZ = attack.center.z + Math.sin(angle) * radius;
                emitLightningBolt(level, groundX, groundZ, attack.center.y, random);
            }
        }

        if (age % 3 == 0) {
            for (int pillar = 0; pillar < 2; pillar++) {
                double angle = random.nextDouble() * Math.PI * 2.0D;
                double radius = 1.0D + random.nextDouble() * (ATTACK_RADIUS - 1.0D);
                double x = attack.center.x + Math.cos(angle) * radius;
                double z = attack.center.z + Math.sin(angle) * radius;
                emitSoulFirePillar(level, x, attack.center.y, z, random);
            }
        }

        if (age % 20 == 0) {
            LocalSoundHelper.playLocalized(level, attack.center,
                    SoundEvents.LIGHTNING_BOLT_IMPACT, 44.0D, 0.8F,
                    0.9F + random.nextFloat() * 0.2F);
        }
    }

    private static void emitLightningBolt(
            ServerLevel level, double groundX, double groundZ, double groundY, RandomSource random) {
        double x = groundX + random.nextDouble() * 2.0D - 1.0D;
        double z = groundZ + random.nextDouble() * 2.0D - 1.0D;
        for (int segment = 12; segment >= 0; segment--) {
            double y = groundY + segment;
            double convergence = segment / 12.0D;
            double px = groundX + (x - groundX) * convergence
                    + (random.nextDouble() - 0.5D) * 0.65D;
            double pz = groundZ + (z - groundZ) * convergence
                    + (random.nextDouble() - 0.5D) * 0.65D;
            level.sendParticles(segment % 3 == 0 ? STORM_PURPLE : ELECTRIC_BLUE,
                    px, y, pz, 2, 0.04D, 0.04D, 0.04D, 0.0D);
            if (segment % 4 == 0) {
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        px, y, pz, 2, 0.15D, 0.2D, 0.15D, 0.08D);
            }
        }
        level.sendParticles(ParticleTypes.FLASH,
                groundX, groundY + 0.4D, groundZ,
                1, 0.15D, 0.15D, 0.15D, 0.0D);
    }

    private static void emitSoulFirePillar(
            ServerLevel level, double x, double groundY, double z, RandomSource random) {
        double height = 4.5D + random.nextDouble() * 3.5D;
        for (double y = 0.0D; y < height; y += 0.7D) {
            double curl = y * 0.55D + random.nextDouble() * 0.4D;
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    x + Math.cos(curl) * 0.22D, groundY + y, z + Math.sin(curl) * 0.22D,
                    1, 0.16D, 0.12D, 0.16D, 0.025D);
            if (y % 1.4D < 0.7D) {
                level.sendParticles(SOUL_CYAN, x, groundY + y, z,
                        1, 0.12D, 0.12D, 0.12D, 0.0D);
            }
        }
        level.sendParticles(ParticleTypes.SOUL,
                x, groundY + 0.3D, z,
                8, 0.65D, 0.35D, 0.65D, 0.06D);
    }

    private static void damagePulse(ServerLevel level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(ATTACK_RADIUS, 9.0D, ATTACK_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && !(entity instanceof Player))) {
            entity.hurt(level.damageSources().lightningBolt(), DAMAGE_PER_PULSE);
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D, entity.getZ(),
                    20, entity.getBbWidth(), entity.getBbHeight() / 2.0D,
                    entity.getBbWidth(), 0.06D);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D, entity.getZ(),
                    18, entity.getBbWidth(), entity.getBbHeight() / 2.0D,
                    entity.getBbWidth(), 0.18D);
        }
    }

    private static DustParticleOptions dust(int rgb, float scale) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255.0F,
                ((rgb >> 8) & 255) / 255.0F, (rgb & 255) / 255.0F), scale);
    }

    private static final class Cast {
        final ServerLevel level; final UUID casterId; final BlockPos target; int ticks;
        Cast(ServerLevel level, UUID casterId, BlockPos target, int ticks) {
            this.level = level; this.casterId = casterId; this.target = target; this.ticks = ticks;
        }
    }

    private static final class Attack {
        final ServerLevel level; final Vec3 center; final List<Display.ItemDisplay> images;
        final long seed; int ticks;
        Attack(ServerLevel level, Vec3 center, List<Display.ItemDisplay> images, long seed, int ticks) {
            this.level = level; this.center = center; this.images = images;
            this.seed = seed; this.ticks = ticks;
        }
    }
}
