package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModItems;
import com.fende.obesecat.registry.ModSounds;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PraxisSummonManager {
    public static final double RANGE = 50.0D;
    public static final int COOLDOWN_TICKS = 20 * 15;
    public static final int CAST_TICKS = 30;
    public static final int ATTACK_TICKS = 20 * 5;
    public static final double ATTACK_RADIUS = 12.0D;
    public static final float DAMAGE_PER_PULSE = 40.0F;

    private static final int ICE_CHUNKS = 14;
    private static final DustParticleOptions ICE_BLUE = dust(0x9CEBFF, 1.2F);
    private static final DustParticleOptions DEEP_BLUE = dust(0x4A9DFF, 1.0F);
    private static final BlockParticleOption ICE_SHARD =
            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState());
    private static final List<Cast> CASTS = new ArrayList<>();
    private static final List<Attack> ATTACKS = new ArrayList<>();

    private PraxisSummonManager() {}

    public static void schedule(ServerLevel level, Player caster, BlockPos target) {
        CASTS.add(new Cast(level, caster.getUUID(), target.immutable(), CAST_TICKS));
        LocalSoundHelper.playLocalized(level, caster.position(), ModSounds.SUMMON_CAST.get(), 32.0D, 1.0F, 0.96F);
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
            emitBlizzard(level, attack, age);
            animateFrozenDescent(attack, age);
            animateIcefall(level, attack, age);
            if (age % 20 == 0) damagePulse(level, attack.center);
            if (--attack.ticks <= 0) {
                ATTACKS.remove(attack);
                attack.image.discard();
                attack.chunks.forEach(chunk -> chunk.display.discard());
                level.sendParticles(ParticleTypes.SNOWFLAKE, attack.center.x, attack.center.y + 3.0D,
                        attack.center.z, 180, 7.0D, 4.0D, 7.0D, 0.12D);
            }
        }
    }

    private static void emitCastingHelix(ServerLevel level, Player caster, int age) {
        Vec3 base = caster.position();
        for (int arm = 0; arm < 3; arm++) {
            double angle = age * 0.48D + arm * Math.PI * 2.0D / 3.0D;
            double radius = 1.15D - Math.min(age, CAST_TICKS) * 0.018D;
            double y = 0.15D + (age % 24) * 0.095D;
            level.sendParticles(arm == 1 ? DEEP_BLUE : ICE_BLUE,
                    base.x + Math.cos(angle) * radius, base.y + y,
                    base.z + Math.sin(angle) * radius, 2, 0.03D, 0.03D, 0.03D, 0.0D);
        }
        if (age % 4 == 0) level.sendParticles(ParticleTypes.SNOWFLAKE,
                base.x, base.y + 1.0D, base.z, 12, 1.1D, 0.8D, 1.1D, 0.08D);
    }

    private static void manifest(ServerLevel level, BlockPos target) {
        Vec3 center = Vec3.atCenterOf(target);
        LocalSoundHelper.playLocalized(level, center, ModSounds.PRAXIS_SUMMON.get(), 64.0D, 1.0F, 1.0F);
        Display.ItemDisplay image = spawnDisplay(level, new ItemStack(ModItems.PRAXIS_GFX.get()),
                center.add(0.0D, 5.0D, 0.0D), 7.5F, true);
        long seed = level.getGameTime() ^ target.asLong() ^ 0x505241584953L;
        RandomSource random = RandomSource.create(seed);
        List<IceChunk> chunks = new ArrayList<>();
        for (int i = 0; i < ICE_CHUNKS; i++) {
            Display.ItemDisplay display = spawnDisplay(level, new ItemStack(Items.PACKED_ICE),
                    center.add(0.0D, 18.0D, 0.0D), 0.7F + random.nextFloat() * 0.8F, false);
            IceChunk chunk = new IceChunk(display);
            resetChunk(chunk, center, random, -random.nextInt(30));
            chunks.add(chunk);
        }
        ATTACKS.add(new Attack(level, center, image, chunks, seed, ATTACK_TICKS));
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 5.0D, center.z, 3, 0.7D, 0.7D, 0.7D, 0.0D);
        level.sendParticles(ParticleTypes.SNOWFLAKE, center.x, center.y + 5.0D, center.z,
                180, 6.0D, 5.0D, 6.0D, 0.2D);
    }

    private static Display.ItemDisplay spawnDisplay(
            ServerLevel level, ItemStack stack, Vec3 pos, float scale, boolean billboard) {
        Tag transform = Transformation.CODEC.encodeStart(NbtOps.INSTANCE,
                new Transformation(new Vector3f(), new Quaternionf(),
                        new Vector3f(scale, scale, scale), new Quaternionf())).result().orElse(null);
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level) {{
            CompoundTag cfg = new CompoundTag();
            cfg.put("item", stack.save(registryAccess()));
            if (billboard) cfg.putString("billboard", "center");
            cfg.putFloat("view_range", 64.0F);
            if (transform != null) cfg.put("transformation", transform);
            readAdditionalSaveData(cfg);
        }};
        display.setPos(pos);
        level.addFreshEntity(display);
        return display;
    }

    private static void emitBlizzard(ServerLevel level, Attack attack, int age) {
        RandomSource random = RandomSource.create(attack.seed ^ (age * 0x9E3779B97F4A7C15L));
        for (int i = 0; i < 24; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 1.0D + random.nextDouble() * ATTACK_RADIUS;
            double y = 0.4D + random.nextDouble() * 8.5D;
            double x = attack.center.x + Math.cos(angle) * radius;
            double z = attack.center.z + Math.sin(angle) * radius;
            level.sendParticles(ParticleTypes.SNOWFLAKE, x, attack.center.y + y, z,
                    1, 0.12D, 0.2D, 0.12D, 0.22D);
            if (i % 5 == 0) {
                double spiral = angle + age * 0.22D;
                level.sendParticles(i % 10 == 0 ? DEEP_BLUE : ICE_BLUE,
                        attack.center.x + Math.cos(spiral) * radius,
                        attack.center.y + y,
                        attack.center.z + Math.sin(spiral) * radius,
                        2, 0.18D, 0.18D, 0.18D, 0.0D);
            }
        }
        if (age % 4 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, attack.center.x, attack.center.y + 1.8D,
                    attack.center.z, 14, 7.0D, 2.5D, 7.0D, 0.18D);
        }
        if (age % 18 == 0) {
            LocalSoundHelper.playLocalized(level, attack.center, SoundEvents.POWDER_SNOW_BREAK,
                    36.0D, 0.8F, 0.55F + random.nextFloat() * 0.2F);
        }
    }

    private static void animateFrozenDescent(Attack attack, int age) {
        double descent = Math.min(1.0D, age / 35.0D);
        double settledY = 5.0D + Math.sin(age * 0.045D) * 0.22D;
        double y = 8.5D + (settledY - 8.5D) * (1.0D - Math.pow(1.0D - descent, 3.0D));
        double sway = Math.sin(age * 0.032D) * 0.65D;
        attack.image.setPos(attack.center.add(sway, y, -sway * 0.35D));
        attack.image.setYRot((float) (Math.sin(age * 0.025D) * 8.0D));
    }

    private static void animateIcefall(ServerLevel level, Attack attack, int age) {
        RandomSource random = RandomSource.create(attack.seed + age * 31L);
        for (IceChunk chunk : attack.chunks) {
            int elapsed = age - chunk.startAge;
            if (elapsed < 0) continue;
            if (elapsed >= chunk.fallTicks) {
                level.sendParticles(ICE_SHARD, chunk.x, attack.center.y + 0.35D, chunk.z,
                        20, 0.9D, 0.45D, 0.9D, 0.24D);
                level.sendParticles(ParticleTypes.SNOWFLAKE, chunk.x, attack.center.y + 0.55D, chunk.z,
                        14, 1.1D, 0.65D, 1.1D, 0.15D);
                if (random.nextInt(3) == 0) {
                    LocalSoundHelper.playLocalized(level, new Vec3(chunk.x, attack.center.y, chunk.z),
                            SoundEvents.GLASS_BREAK, 30.0D, 0.65F, 0.65F + random.nextFloat() * 0.35F);
                }
                resetChunk(chunk, attack.center, random, age + 2 + random.nextInt(10));
                continue;
            }
            double progress = elapsed / (double) chunk.fallTicks;
            double y = attack.center.y + chunk.startHeight * (1.0D - progress * progress);
            double sway = Math.sin(progress * Math.PI * 3.0D + chunk.spin) * 0.75D;
            chunk.display.setPos(chunk.x + sway, y, chunk.z + Math.cos(chunk.spin + progress * 8.0D) * 0.5D);
            chunk.display.setYRot((float) Math.toDegrees(chunk.spin + progress * 8.0D));
        }
    }

    private static void resetChunk(IceChunk chunk, Vec3 center, RandomSource random, int startAge) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = 1.5D + random.nextDouble() * (ATTACK_RADIUS - 1.5D);
        chunk.x = center.x + Math.cos(angle) * radius;
        chunk.z = center.z + Math.sin(angle) * radius;
        chunk.startHeight = 11.0D + random.nextDouble() * 9.0D;
        chunk.fallTicks = 12 + random.nextInt(15);
        chunk.startAge = startAge;
        chunk.spin = random.nextDouble() * Math.PI * 2.0D;
        chunk.display.setPos(chunk.x, center.y + chunk.startHeight, chunk.z);
    }

    private static void damagePulse(ServerLevel level, Vec3 center) {
        AABB box = new AABB(center, center).inflate(ATTACK_RADIUS, 9.0D, ATTACK_RADIUS);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity.isAlive() && !(entity instanceof Player))) {
            entity.hurt(level.damageSources().freeze(), DAMAGE_PER_PULSE);
            entity.setTicksFrozen(Math.min(entity.getTicksFrozen() + 100, 300));
            level.sendParticles(ICE_SHARD, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D,
                    entity.getZ(), 26, entity.getBbWidth(), entity.getBbHeight() / 2.0D,
                    entity.getBbWidth(), 0.15D);
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

    private static final class IceChunk {
        final Display.ItemDisplay display;
        double x; double z; double startHeight; double spin;
        int startAge; int fallTicks;
        IceChunk(Display.ItemDisplay display) { this.display = display; }
    }

    private static final class Attack {
        final ServerLevel level; final Vec3 center; final Display.ItemDisplay image;
        final List<IceChunk> chunks; final long seed; int ticks;
        Attack(ServerLevel level, Vec3 center, Display.ItemDisplay image,
                List<IceChunk> chunks, long seed, int ticks) {
            this.level = level; this.center = center; this.image = image;
            this.chunks = chunks; this.seed = seed; this.ticks = ticks;
        }
    }
}
