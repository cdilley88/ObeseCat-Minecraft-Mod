package com.fende.obesecat.entity;

import com.fende.obesecat.registry.ModItems;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class TheCarver extends Monster {
    private static final DustParticleOptions BLOOD_DUST =
            new DustParticleOptions(new Vector3f(0.85F, 0.05F, 0.1F), 1.4F);
    private static final DustParticleOptions CHAIN_DUST =
            new DustParticleOptions(new Vector3f(0.35F, 0.38F, 0.42F), 1.2F);

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.obesecat.the_carver"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    public TheCarver(EntityType<? extends TheCarver> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 200;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CURSED_CLEAVER.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 22.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TheCarverMeatHookGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.22D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CURSED_CLEAVER.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        if (name != null) {
            this.bossEvent.setName(name);
        }
    }

    public boolean isEnraged() {
        return this.getHealth() < this.getMaxHealth() * 0.35F;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if (this.isEnraged()) {
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 1, false, false));
            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 4 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        this.getY() + this.random.nextDouble() * 2.5D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                        2, 0.05D, 0.08D, 0.05D, 0.02D
                );
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity livingTarget) {
            Level level = this.level();
            // Slow down the victim
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));

            // Heavy cleaver impact sounds & blood particles
            level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.4F, 0.65F);
            level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.HOSTILE, 1.8F, 0.75F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        BLOOD_DUST,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        35, 0.5D, 0.5D, 0.5D, 0.1D
                );

                // Cleave adjacent enemies
                AABB cleaveBox = target.getBoundingBox().inflate(2.5D);
                List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class, cleaveBox,
                        e -> e != this && e != target && e.isAlive() && !e.isAlliedTo(this));
                for (LivingEntity entity : nearby) {
                    entity.hurt(serverLevel.damageSources().mobAttack(this), 11.0F);
                    entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0));
                }
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Guaranteed catalysts
        this.spawnAtLocation(new ItemStack(ModItems.FLAYED_DEMON_HEART.get()));
        this.spawnAtLocation(new ItemStack(ModItems.BLOODIED_IRON_HOOK.get()));

        // Chance to recover the cleaver
        if (this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(ModItems.CURSED_CLEAVER.get()));
        }

        // 35% chance to drop Atomic Semiconductor (Ber - Enigma component)
        if (this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(new ItemStack(ModItems.ATOMIC_SEMICONDUCTOR.get()));
        }

        // Dramatic death effect
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
        level.sendParticles(BLOOD_DUST, this.getX(), this.getY() + 1.0D, this.getZ(), 100, 1.5D, 1.2D, 1.5D, 0.2D);
        level.playSound(null, this.blockPosition(), SoundEvents.WARDEN_DEATH, SoundSource.HOSTILE, 2.5F, 0.7F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIGLIN_BRUTE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_BRUTE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_BRUTE_DEATH;
    }

    private static class TheCarverMeatHookGoal extends Goal {
        private final TheCarver carver;
        private int cooldown = 60;

        public TheCarverMeatHookGoal(TheCarver carver) {
            this.carver = carver;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.carver.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (--this.cooldown > 0) {
                return false;
            }
            double distSq = this.carver.distanceToSqr(target);
            // Engage hook between 8 and 28 blocks
            return distSq >= 64.0D && distSq <= 784.0D && this.carver.hasLineOfSight(target);
        }

        @Override
        public void start() {
            LivingEntity target = this.carver.getTarget();
            if (target == null) return;

            Level level = this.carver.level();
            this.carver.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Hook sound cues
            level.playSound(null, this.carver.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 2.0F, 0.6F);
            level.playSound(null, this.carver.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 2.0F, 0.65F);
            level.playSound(null, this.carver.blockPosition(), SoundEvents.PIGLIN_BRUTE_ANGRY, SoundSource.HOSTILE, 2.0F, 0.75F);

            if (level instanceof ServerLevel serverLevel) {
                Vec3 start = this.carver.position().add(0.0D, 1.8D, 0.0D);
                Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
                Vec3 line = end.subtract(start);
                double steps = Math.max(8.0D, line.length() * 2.5D);
                Vec3 step = line.scale(1.0D / steps);

                // Draw chain projectile particles
                for (int i = 0; i < steps; i++) {
                    Vec3 p = start.add(step.scale(i));
                    serverLevel.sendParticles(CHAIN_DUST, p.x, p.y, p.z, 2, 0.04D, 0.04D, 0.04D, 0.0D);
                }

                // Yank the target to Carver
                Vec3 pull = this.carver.position().subtract(target.position()).normalize().scale(1.45D);
                target.setDeltaMovement(pull.x, 0.42D, pull.z);
                target.hurtMarked = true;

                // Hook damage & snare
                target.hurt(serverLevel.damageSources().mobAttack(this.carver), 6.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2));

                // Target hit sounds & particles
                serverLevel.sendParticles(BLOOD_DUST, target.getX(), target.getY() + 1.0D, target.getZ(), 25, 0.3D, 0.3D, 0.3D, 0.1D);
                level.playSound(null, target.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.5F, 0.7F);
            }

            // Cooldown: 8s normal, 4.5s enraged
            this.cooldown = this.carver.isEnraged() ? 90 : 160;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
