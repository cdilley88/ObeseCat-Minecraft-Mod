package com.fende.obesecat.entity;

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
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CowKing extends Monster {
    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            Component.translatable("entity.obesecat.cow_king"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    ).setDarkenScreen(false);

    private long lastChargedBoltTick = 0L;

    public CowKing(EntityType<? extends CowKing> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 150;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 18.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(2, new CowKingLightningSmiteGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Ambient lightning crackle particles while alive
        if (this.tickCount % 8 == 0 && this.level() instanceof ServerLevel serverLevel) {
            double ox = (this.random.nextDouble() - 0.5D) * 3.5D;
            double oy = this.random.nextDouble() * 5.0D;
            double oz = (this.random.nextDouble() - 0.5D) * 3.5D;
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX() + ox, this.getY() + oy, this.getZ() + oz,
                    2, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Diablo 2 Lightning Enchanted: Immune to lightning, fire, and fall damage
        if (source.is(DamageTypeTags.IS_LIGHTNING) || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide() && amount > 0.0F) {
            // Diablo 2 Lightning Enchanted: Emit Charged Bolts on hit
            long currentTick = this.level().getGameTime();
            if (currentTick - this.lastChargedBoltTick >= 3L) {
                this.lastChargedBoltTick = currentTick;
                triggerChargedBolts();
            }
        }
        return hurt;
    }

    private void triggerChargedBolts() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 1.2F, 1.8F);

        int numBolts = 8;
        double baseAngle = this.random.nextDouble() * Math.PI * 2.0D;
        for (int i = 0; i < numBolts; i++) {
            double angle = baseAngle + (i * Math.PI * 2.0D / numBolts) + (this.random.nextDouble() - 0.5D) * 0.3D;
            double dirX = Math.cos(angle);
            double dirZ = Math.sin(angle);

            // Step each bolt out 1 to 7 blocks along the floor
            for (int step = 1; step <= 7; step++) {
                double bx = this.getX() + dirX * step;
                double bz = this.getZ() + dirZ * step;
                double by = this.getY() + 0.3D;

                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, bx, by, bz, 3, 0.2D, 0.2D, 0.2D, 0.05D);

                AABB box = new AABB(bx - 1.0D, by - 0.5D, bz - 1.0D, bx + 1.0D, by + 1.5D, bz + 1.0D);
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, box,
                        e -> e != this && !(e instanceof Cow) && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurt(this.damageSources().lightningBolt(), 6.0F);
                    target.push(dirX * 0.3D, 0.2D, dirZ * 0.3D);
                }
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity target && this.level() instanceof ServerLevel serverLevel) {
            // Massive seismic cleave + secondary lightning smite
            target.hurt(this.damageSources().lightningBolt(), 6.0F);
            double dx = target.getX() - this.getX();
            double dz = target.getZ() - this.getZ();
            double dist = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
            target.push(dx / dist * 0.8D, 0.4D, dz / dist * 0.8D);

            serverLevel.sendParticles(ParticleTypes.FLASH, target.getX(), target.getY() + 1.0D, target.getZ(), 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + 1.0D, target.getZ(), 20, 0.5D, 0.5D, 0.5D, 0.1D);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.RAVAGER_ATTACK, SoundSource.HOSTILE, 1.5F, 0.5F);

            // Cleave other nearby targets
            AABB cleaveBox = this.getBoundingBox().inflate(2.5D);
            List<LivingEntity> cleaved = serverLevel.getEntitiesOfClass(LivingEntity.class, cleaveBox,
                    e -> e != this && e != target && !(e instanceof Cow) && e.isAlive());
            for (LivingEntity c : cleaved) {
                c.hurt(this.damageSources().mobAttack(this), 10.0F);
                c.hurt(this.damageSources().lightningBolt(), 4.0F);
            }
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (this.level() instanceof ServerLevel serverLevel) {
            // Grand lightning eruption on defeat
            for (int i = 0; i < 5; i++) {
                double ox = (this.random.nextDouble() - 0.5D) * 14.0D;
                double oz = (this.random.nextDouble() - 0.5D) * 14.0D;
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(this.getX() + ox, this.getY(), this.getZ() + oz);
                    bolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(bolt);
                }
            }
            serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 3.0D, this.getZ(), 5, 1.0D, 1.0D, 1.0D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, this.getX(), this.getY() + 3.0D, this.getZ(), 80, 2.5D, 2.5D, 2.5D, 0.25D);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COW_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.COW_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COW_DEATH;
    }

    @Override
    public float getVoicePitch() {
        // Deep, rumbling demonic moo
        return 0.45F;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.COW_STEP, 1.2F, 0.4F);
        this.playSound(SoundEvents.RAVAGER_STEP, 0.6F, 0.6F);
    }

    // ==============================================================================================
    // Custom Goals
    // ==============================================================================================

    private static class CowKingLightningSmiteGoal extends Goal {
        private final CowKing cowKing;
        private int warmupTicks = 0;
        private int cooldownTicks = 60;
        private Vec3 telegraphPos = null;

        public CowKingLightningSmiteGoal(CowKing cowKing) {
            this.cowKing = cowKing;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.cowKing.getTarget();
            if (target == null || !target.isAlive()) {
                return false;
            }
            if (--this.cooldownTicks > 0) {
                return false;
            }
            double distSqr = this.cowKing.distanceToSqr(target);
            return distSqr >= 9.0D && distSqr <= 1024.0D; // Between 3 and 32 blocks
        }

        @Override
        public void start() {
            this.warmupTicks = 20; // 1.0 second telegraph
            this.cooldownTicks = 140; // 7 seconds between smites
            LivingEntity target = this.cowKing.getTarget();
            if (target != null) {
                this.telegraphPos = target.position();
                this.cowKing.getLookControl().setLookAt(target, 30.0F, 30.0F);
                if (this.cowKing.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, this.cowKing.getX(), this.cowKing.getY(), this.cowKing.getZ(),
                            SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.HOSTILE, 1.5F, 1.5F);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.warmupTicks > 0 && this.telegraphPos != null;
        }

        @Override
        public void tick() {
            if (this.telegraphPos == null || !(this.cowKing.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            this.warmupTicks--;

            // Warning telegraph particles on the ground at target location
            double tx = this.telegraphPos.x;
            double ty = this.telegraphPos.y;
            double tz = this.telegraphPos.z;

            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, tx, ty + 0.1D, tz, 8, 0.8D, 0.1D, 0.8D, 0.05D);

            if (this.warmupTicks == 0) {
                // Strike down lightning!
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(tx, ty, tz);
                    bolt.setVisualOnly(false);
                    serverLevel.addFreshEntity(bolt);
                }
                serverLevel.sendParticles(ParticleTypes.FLASH, tx, ty + 1.0D, tz, 2, 0, 0, 0, 0);
            }
        }
    }
}
