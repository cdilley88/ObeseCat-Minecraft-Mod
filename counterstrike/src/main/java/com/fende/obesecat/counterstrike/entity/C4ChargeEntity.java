package com.fende.obesecat.counterstrike.entity;

import com.fende.obesecat.counterstrike.registry.CSEntities;
import com.fende.obesecat.counterstrike.registry.CSItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class C4ChargeEntity extends ThrowableItemProjectile {
    private boolean isPlanted = false;
    private int timer = 60; // 3 seconds

    public C4ChargeEntity(EntityType<? extends C4ChargeEntity> entityType, Level level) {
        super(entityType, level);
    }

    public C4ChargeEntity(Level level, LivingEntity shooter) {
        super(CSEntities.C4_CHARGE.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return CSItems.COMMANDO_RIFLE.get();
    }

    @Override
    public void tick() {
        super.tick();

        if (isPlanted) {
            setDeltaMovement(Vec3.ZERO);
        }

        if (!level().isClientSide) {
            timer--;

            // Warning beeps and flashes
            if (timer == 40) {
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0F, 1.2F);
            } else if (timer == 20) {
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.2F, 1.6F);
            } else if (timer == 5) {
                level().playSound(null, getX(), getY(), getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.4F, 2.0F);
            }

            if (timer <= 0) {
                detonate();
            }
        } else {
            // Blinking red light
            if (timer % 5 == 0) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER, getX(), getY() + 0.3, getZ(), 0, 0.05, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        plant();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        plant();
    }

    private void plant() {
        if (!isPlanted) {
            isPlanted = true;
            setNoGravity(true);
            setDeltaMovement(Vec3.ZERO);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.8F, 1.8F);
        }
    }

    private void detonate() {
        if (!level().isClientSide) {
            level().explode(this, getX(), getY(), getZ(), 5.0F, Level.ExplosionInteraction.BLOCK);
            if (level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 2, 0.5, 0.5, 0.5, 0);
            }

            if (getOwner() instanceof Player player) {
                player.displayClientMessage(
                        Component.literal("§c[EVA] §fC-4 detonated! Sector cleared."),
                        true
                );
            }
            discard();
        }
    }
}
