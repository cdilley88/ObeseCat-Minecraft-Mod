package com.fende.obesecat.counterstrike.entity;

import com.fende.obesecat.counterstrike.registry.CSEntities;
import com.fende.obesecat.counterstrike.registry.CSItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class RocketProjectile extends ThrowableItemProjectile {

    public RocketProjectile(EntityType<? extends RocketProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public RocketProjectile(Level level, LivingEntity shooter) {
        super(CSEntities.ROCKET.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return CSItems.ROCKET_LAUNCHER.get();
    }

    @Override
    public void tick() {
        super.tick();

        // Rocket exhaust trail particles
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 0.0, 0.02, 0.0);
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), -getDeltaMovement().x * 0.2, -getDeltaMovement().y * 0.2, -getDeltaMovement().z * 0.2);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            DamageSource damageSource = damageSources().playerAttack((net.minecraft.world.entity.player.Player) (getOwner() instanceof net.minecraft.world.entity.player.Player p ? p : null));
            result.getEntity().hurt(damageSource, 18.0F);
            detonate();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide) {
            detonate();
        }
    }

    private void detonate() {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.MOB);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            }
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.2F, 0.9F);
            discard();
        }
    }
}
