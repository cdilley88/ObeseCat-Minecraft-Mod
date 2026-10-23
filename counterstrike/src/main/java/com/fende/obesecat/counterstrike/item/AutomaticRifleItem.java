package com.fende.obesecat.counterstrike.item;

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

public class AutomaticRifleItem extends Item {
    public static final int MAG_SIZE = 24;
    public static final int RELOAD_TICKS = 40; // 2 seconds

    public AutomaticRifleItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int ammo = tag.contains("Ammo") ? tag.getInt("Ammo") : MAG_SIZE;

        // Fire round
        fireBullet(level, player);

        ammo--;
        if (ammo <= 0) {
            // Magazine depleted -> trigger reload cooldown
            tag.putInt("Ammo", MAG_SIZE);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, RELOAD_TICKS);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.IRON_TRAPDOOR_OPEN, SoundSource.PLAYERS, 0.8F, 1.5F);
            player.displayClientMessage(Component.literal("§e[EVA] §fReloading Automatic Rifle magazine..."), true);
        } else {
            tag.putInt("Ammo", ammo);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, 3); // 3-tick burst delay
            player.displayClientMessage(Component.literal("§a[AMMO: " + ammo + " / " + MAG_SIZE + "]"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void fireBullet(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 40.0;
        Vec3 target = eyePos.add(look.scale(range));

        // Sound & recoil kick
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6F, 2.0F);

        // Raycast against entities
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.5);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                level,
                player,
                eyePos,
                target,
                searchBox,
                entity -> entity instanceof LivingEntity && !entity.isSpectator() && entity.isPickable()
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity living) {
            living.hurt(player.damageSources().playerAttack(player), 6.0F);
            target = hit.getLocation();
        }

        // Particle tracer line
        if (level instanceof ServerLevel sl) {
            Vec3 dir = target.subtract(eyePos);
            double dist = dir.length();
            int points = (int) (dist * 2);
            for (int i = 0; i < points; i++) {
                double frac = (double) i / points;
                Vec3 p = eyePos.add(dir.scale(frac));
                sl.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
        }
    }
}
