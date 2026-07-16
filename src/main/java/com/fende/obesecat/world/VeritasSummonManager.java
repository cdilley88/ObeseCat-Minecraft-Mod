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

public final class VeritasSummonManager {
    public static final double RANGE = 50.0D;
    public static final int COOLDOWN_TICKS = 20 * 15;
    public static final int CAST_TICKS = 30;
    public static final int ATTACK_TICKS = 20 * 5;
    public static final double ATTACK_RADIUS = 12.0D;
    public static final float DAMAGE_PER_PULSE = 40.0F;

    private static final int[] CAST_COLORS = {0xFF2BD6, 0x00F5FF, 0x8A2BFF, 0xFFF200, 0x39FF88};
    private static final DustParticleOptions GOLD = dust(0xFFD34E, 1.35F);
    private static final DustParticleOptions WHITE_GOLD = dust(0xFFF4C2, 1.05F);
    private static final List<Cast> CASTS = new ArrayList<>();
    private static final List<Attack> ATTACKS = new ArrayList<>();

    private VeritasSummonManager() {}

    public static void schedule(ServerLevel level, Player caster, BlockPos target) {
        CASTS.add(new Cast(level, caster.getUUID(), target.immutable(), CAST_TICKS));
        LocalSoundHelper.playLocalized(level, caster.position(), ModSounds.SUMMON_CAST.get(), 32.0D, 1.0F, 1.05F);
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
            emitHolyFire(level, attack, age);
            if (age % 20 == 0) damagePulse(level, attack.center);
            if (--attack.ticks <= 0) {
                ATTACKS.remove(attack);
                attack.image.discard();
                level.sendParticles(ParticleTypes.END_ROD, attack.center.x, attack.center.y + 3.0D,
                        attack.center.z, 120, 6.0D, 4.0D, 6.0D, 0.15D);
            }
        }
    }

    private static void emitCastingHelix(ServerLevel level, Player caster, int age) {
        Vec3 base = caster.position();
        for (int arm = 0; arm < 3; arm++) {
            double angle = age * 0.48D + arm * Math.PI * 2.0D / 3.0D;
            double radius = 1.15D - Math.min(age, CAST_TICKS) * 0.018D;
            double y = 0.15D + (age % 24) * 0.095D;
            level.sendParticles(dust(CAST_COLORS[(age + arm) % CAST_COLORS.length], 1.25F),
                    base.x + Math.cos(angle) * radius, base.y + y,
                    base.z + Math.sin(angle) * radius, 2, 0.03D, 0.03D, 0.03D, 0.0D);
        }
        if (age % 4 == 0) level.sendParticles(ParticleTypes.ENCHANT, base.x, base.y + 1.0D, base.z,
                10, 1.1D, 0.8D, 1.1D, 0.15D);
    }

    private static void manifest(ServerLevel level, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        LocalSoundHelper.playLocalized(level, center, ModSounds.VERITAS_SUMMON.get(), 64.0D, 1.0F, 1.0F);
        Display.ItemDisplay image = spawnImage(level, center.add(0.0D, 5.0D, 0.0D));
        long seed = level.getGameTime() ^ target.asLong() ^ 0x564552495441534CL;
        ATTACKS.add(new Attack(level, center, image, seed, ATTACK_TICKS));
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 5.0D, center.z, 3, 0.7D, 0.7D, 0.7D, 0.0D);
        level.sendParticles(GOLD, center.x, center.y + 4.0D, center.z, 100, 4.0D, 4.0D, 4.0D, 0.12D);
    }

    private static Display.ItemDisplay spawnImage(ServerLevel level, Vec3 pos) {
        ItemStack stack = new ItemStack(ModItems.VERITAS_GFX.get());
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(7.5F, 7.5F, 1.0F), new Quaternionf())).result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            cfg.putString("billboard", "center");
            cfg.putFloat("view_range", 64.0F);
            cfg.putBoolean("Glowing", true);
            cfg.putInt("glow_color_override", 0xFFD34E);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos);
        level.addFreshEntity(display);
        return display;
    }

    private static void emitHolyFire(ServerLevel level, Attack attack, int age) {
        RandomSource random = RandomSource.create(attack.seed ^ (age * 0x9E3779B97F4A7C15L));
        int pillarCount = 2 + random.nextInt(3);
        for (int pillar = 0; pillar < pillarCount; pillar++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 1.0D + random.nextDouble() * ATTACK_RADIUS;
            double x = attack.center.x + Math.cos(angle) * radius;
            double z = attack.center.z + Math.sin(angle) * radius;
            double groundY = attack.center.y + random.nextDouble() * 1.5D - 0.5D;
            double height = 4.0D + random.nextDouble() * 9.0D;
            double width = 0.12D + random.nextDouble() * 0.32D;
            for (double y = 0.0D; y < height; y += 0.65D) {
                level.sendParticles(y % 1.3D < 0.65D ? WHITE_GOLD : GOLD,
                        x, groundY + y, z, 2, width, 0.14D, width, 0.0D);
                if (random.nextInt(4) == 0) level.sendParticles(ParticleTypes.END_ROD,
                        x, groundY + y, z, 1, width, 0.18D, width, 0.01D);
            }
            level.sendParticles(ParticleTypes.FLAME, x, groundY + 0.25D, z,
                    14 + random.nextInt(16), 0.75D, 0.45D, 0.75D, 0.08D);
            level.sendParticles(GOLD, x, groundY + 0.4D, z,
                    18 + random.nextInt(18), 0.9D, 0.55D, 0.9D, 0.12D);
        }
        if (age % 6 == 0) level.sendParticles(ParticleTypes.WAX_ON,
                attack.center.x, attack.center.y + 2.5D, attack.center.z,
                24, 7.0D, 3.0D, 7.0D, 0.12D);
    }

    private static void damagePulse(ServerLevel level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(ATTACK_RADIUS, 9.0D, ATTACK_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && !(entity instanceof Player))) {
            entity.hurt(level.damageSources().magic(), DAMAGE_PER_PULSE);
            level.sendParticles(GOLD, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D,
                    entity.getZ(), 30, entity.getBbWidth(), entity.getBbHeight() / 2.0D, entity.getBbWidth(), 0.1D);
            level.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY() + 0.2D,
                    entity.getZ(), 16, entity.getBbWidth(), 0.6D, entity.getBbWidth(), 0.06D);
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
        final ServerLevel level; final Vec3 center; final Display.ItemDisplay image; final long seed; int ticks;
        Attack(ServerLevel level, Vec3 center, Display.ItemDisplay image, long seed, int ticks) {
            this.level = level; this.center = center; this.image = image; this.seed = seed; this.ticks = ticks;
        }
    }
}

