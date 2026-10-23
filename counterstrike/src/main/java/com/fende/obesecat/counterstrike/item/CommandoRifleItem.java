package com.fende.obesecat.counterstrike.item;

import com.fende.obesecat.counterstrike.entity.C4ChargeEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class CommandoRifleItem extends Item {
    public static final int MAG_SIZE = 30;
    public static final int RELOAD_TICKS = 45; // 2.25 seconds
    public static final int C4_COOLDOWN_TICKS = 70; // 3.5 seconds

    public CommandoRifleItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift + Right-Click: Plant Timed C-4 Charge
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                C4ChargeEntity c4 = new C4ChargeEntity(level, player);
                Vec3 look = player.getLookAngle();
                c4.shoot(look.x, look.y + 0.1, look.z, 0.9F, 0.2F);
                level.addFreshEntity(c4);

                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 1.0F, 1.5F);
                player.getCooldowns().addCooldown(this, C4_COOLDOWN_TICKS);
                player.displayClientMessage(Component.literal("§c[EVA] §fC-4 Planted! Detonation in 3 seconds..."), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        // Normal Right-Click: Heavy Armor-Piercing Rifle Burst
        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int ammo = tag.contains("Ammo") ? tag.getInt("Ammo") : MAG_SIZE;

        fireHeavyBullet(level, player);

        ammo--;
        if (ammo <= 0) {
            tag.putInt("Ammo", MAG_SIZE);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, RELOAD_TICKS);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 1.0F, 1.2F);
            player.displayClientMessage(Component.literal("§e[EVA] §fReloading Commando Rifle..."), true);
        } else {
            tag.putInt("Ammo", ammo);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, 3); // rapid 3-tick fire rate
            player.displayClientMessage(Component.literal("§b[COMMANDO AMMO: " + ammo + " / " + MAG_SIZE + "]"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void fireHeavyBullet(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 48.0;
        Vec3 target = eyePos.add(look.scale(range));

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8F, 1.8F);

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePos,
                target,
                searchBox,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable()
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            living.hurt(player.damageSources().playerAttack(player), 10.0F);
            target = hit.getLocation();
        }

        if (level instanceof ServerLevel sl) {
            Vec3 dir = target.subtract(eyePos);
            double dist = dir.length();
            int points = (int) (dist * 2);
            for (int i = 0; i < points; i++) {
                double frac = (double) i / points;
                Vec3 p = eyePos.add(dir.scale(frac));
                sl.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
