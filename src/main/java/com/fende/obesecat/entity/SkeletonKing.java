package com.fende.obesecat.entity;

import com.fende.obesecat.registry.ModItems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class SkeletonKing extends Monster implements RangedAttackMob {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.obesecat.skeleton_king"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    public SkeletonKing(EntityType<? extends SkeletonKing> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 200;
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MIGHTY_SWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SkeletonKingTeleportSlamGoal(this));
        this.goalSelector.addGoal(2, new SkeletonKingSummonGuardsGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MIGHTY_SWORD.get()));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float pullProgress) {
        // Melee boss primarily; interface allows using vanilla SkeletonModel natively
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
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false));
            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 5 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.0D,
                        this.getY() + 2.2D + this.random.nextDouble() * 0.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.0D,
                        2, 0.04D, 0.05D, 0.04D, 0.01D
                );
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));

            Level level = this.level();
            level.playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.2F, 0.6F);
            level.playSound(null, target.blockPosition(), SoundEvents.SKELETON_HURT, SoundSource.HOSTILE, 1.8F, 0.45F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                        25, 0.4D, 0.4D, 0.4D, 0.1D
                );

                // Cleave adjacent
                AABB cleaveBox = target.getBoundingBox().inflate(2.2D);
                List<LivingEntity> nearby = serverLevel.getEntitiesOfClass(LivingEntity.class, cleaveBox,
                        e -> e != this && e != target && e.isAlive() && !e.isAlliedTo(this));
                for (LivingEntity e : nearby) {
                    e.hurt(serverLevel.damageSources().mobAttack(this), 9.0F);
                }
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        List<Item> perfumes = new ArrayList<>(List.of(
                ModItems.CHERCHE.get(),
                ModItems.SALTY_RAGE.get(),
                ModItems.CHANTAGE.get(),
                ModItems.SETIEMSON.get()
        ));
        for (int i = perfumes.size() - 1; i > 0; i--) {
            int j = this.random.nextInt(i + 1);
            Item temp = perfumes.get(i);
            perfumes.set(i, perfumes.get(j));
            perfumes.set(j, temp);
        }

        // 1 guaranteed perfume catalyst drop
        this.spawnAtLocation(new ItemStack(perfumes.get(0)));

        // 40% chance for a second distinct perfume catalyst drop
        if (this.random.nextFloat() < 0.40F) {
            this.spawnAtLocation(new ItemStack(perfumes.get(1)));
        }

        // 25% chance to drop the crown back
        if (this.random.nextFloat() < 0.25F) {
            this.spawnAtLocation(new ItemStack(ModItems.SKELETON_CROWN.get()));
        }

        // 35% chance to drop Quantum Semiconductor (Jah - Enigma component)
        if (this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(new ItemStack(ModItems.QUANTUM_SEMICONDUCTOR.get()));
        }

        // Dramatic death explosion
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, this.getX(), this.getY() + 1.5D, this.getZ(), 3, 0.5D, 0.5D, 0.5D, 0.0D);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.2D, this.getZ(), 80, 1.2D, 1.2D, 1.2D, 0.15D);
        level.playSound(null, this.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 2.5F, 0.8F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    private static class SkeletonKingTeleportSlamGoal extends Goal {
        private final SkeletonKing king;
        private int cooldown = 140;

        public SkeletonKingTeleportSlamGoal(SkeletonKing king) {
            this.king = king;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            if (target == null || !target.isAlive()) return false;
            if (--this.cooldown > 0) return false;
            double distSq = this.king.distanceToSqr(target);
            return distSq >= 36.0D && distSq <= 1024.0D;
        }

        @Override
        public void start() {
            LivingEntity target = this.king.getTarget();
            if (target == null) return;

            Level level = this.king.level();
            if (level instanceof ServerLevel serverLevel) {
                // Poof particles at old pos
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.king.getX(), this.king.getY() + 1.0D, this.king.getZ(), 30, 0.5D, 0.8D, 0.5D, 0.05D);

                // Teleport 2.5 blocks behind the player
                Vec3 look = target.getViewVector(1.0F).normalize();
                Vec3 behind = target.position().subtract(look.scale(2.4D));
                this.king.teleportTo(behind.x, target.getY(), behind.z);
                this.king.getLookControl().setLookAt(target, 60.0F, 60.0F);

                // Ground slam wave
                serverLevel.playSound(null, this.king.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 2.0F, 0.7F);
                serverLevel.playSound(null, this.king.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.2F, 0.5F);
                serverLevel.playSound(null, this.king.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.8F, 0.65F);

                serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.king.getX(), this.king.getY() + 0.2D, this.king.getZ(), 3, 0.4D, 0.1D, 0.4D, 0.0D);
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, this.king.getX(), this.king.getY() + 0.5D, this.king.getZ(), 10, 1.5D, 0.2D, 1.5D, 0.0D);

                // Area damage and upward pop
                AABB slamBox = this.king.getBoundingBox().inflate(3.5D);
                List<LivingEntity> victims = serverLevel.getEntitiesOfClass(LivingEntity.class, slamBox,
                        e -> e != this.king && e.isAlive() && !e.isAlliedTo(this.king));
                for (LivingEntity v : victims) {
                    v.hurt(serverLevel.damageSources().mobAttack(this.king), 14.0F);
                    v.setDeltaMovement(v.getDeltaMovement().add(0.0D, 0.42D, 0.0D));
                    v.hurtMarked = true;
                    v.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1));
                }
            }

            this.cooldown = this.king.isEnraged() ? 120 : 220;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    private static class SkeletonKingSummonGuardsGoal extends Goal {
        private final SkeletonKing king;
        private int cooldown = 300;

        public SkeletonKingSummonGuardsGoal(SkeletonKing king) {
            this.king = king;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.king.getTarget();
            if (target == null || !target.isAlive()) return false;
            return --this.cooldown <= 0;
        }

        @Override
        public void start() {
            Level level = this.king.level();
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.king.blockPosition(), SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 2.5F, 0.4F);
                serverLevel.playSound(null, this.king.blockPosition(), SoundEvents.SOUL_SAND_BREAK, SoundSource.HOSTILE, 2.0F, 0.5F);

                for (int i = 0; i < 3; i++) {
                    double offsetX = (this.king.random.nextDouble() - 0.5D) * 4.0D;
                    double offsetZ = (this.king.random.nextDouble() - 0.5D) * 4.0D;
                    BlockPos spawnPos = this.king.blockPosition().offset((int) offsetX, 0, (int) offsetZ);

                    Skeleton minion = EntityType.SKELETON.create(serverLevel);
                    if (minion != null) {
                        minion.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
                        minion.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null);
                        minion.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                        minion.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                        minion.setDropChance(EquipmentSlot.HEAD, 0.0F);
                        minion.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
                        serverLevel.addFreshEntity(minion);

                        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D, 20, 0.4D, 0.8D, 0.4D, 0.05D);
                    }
                }
            }
            this.cooldown = this.king.isEnraged() ? 280 : 420;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
