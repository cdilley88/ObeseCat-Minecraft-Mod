package com.fende.obesecat.counterstrike.entity;

import com.fende.obesecat.counterstrike.registry.CSEntities;
import com.fende.obesecat.counterstrike.registry.CSItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class FragGrenadeProjectile extends ThrowableItemProjectile {
    private int fuse = 40; // 2 seconds

    public FragGrenadeProjectile(EntityType<? extends FragGrenadeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FragGrenadeProjectile(Level level, LivingEntity shooter) {
        super(CSEntities.FRAG_GRENADE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return CSItems.FRAG_GRENADE.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.2, getZ(), 0, 0.02, 0);
        } else {
            fuse--;
            if (fuse <= 0) {
                detonate();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide) {
            // Direct entity hit triggers instant explosion
            detonate();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // Bounce off block
        Vec3 motion = getDeltaMovement();
        switch (result.getDirection().getAxis()) {
            case X -> setDeltaMovement(-motion.x * 0.45, motion.y * 0.45, motion.z * 0.45);
            case Y -> setDeltaMovement(motion.x * 0.45, -motion.y * 0.35, motion.z * 0.45);
            case Z -> setDeltaMovement(motion.x * 0.45, motion.y * 0.45, -motion.z * 0.45);
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.4F, 1.8F);
    }

    private void detonate() {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 2.8F, Level.ExplosionInteraction.NONE);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.CRIT, getX(), getY() + 0.5, getZ(), 30, 1.5, 1.0, 1.5, 0.2);
            }
            discard();
        }
    }
}
