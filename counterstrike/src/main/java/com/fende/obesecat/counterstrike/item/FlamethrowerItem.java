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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FlamethrowerItem extends Item {
    public static final int CANISTER_CAPACITY = 40;
    public static final int RECHARGE_TICKS = 60; // 3 seconds

    public FlamethrowerItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        int fuel = tag.contains("Fuel") ? tag.getInt("Fuel") : CANISTER_CAPACITY;

        // Emit jet of flame
        spewFlames(level, player);

        fuel--;
        if (fuel <= 0) {
            tag.putInt("Fuel", CANISTER_CAPACITY);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, RECHARGE_TICKS);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.9F, 1.2F);
            player.displayClientMessage(Component.literal("§e[EVA] §fPressurizing Flamethrower canister..."), true);
        } else {
            tag.putInt("Fuel", fuel);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, 2); // 2-tick continuous stream delay
            player.displayClientMessage(Component.literal("§6[FUEL: " + fuel + " / " + CANISTER_CAPACITY + "]"), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private void spewFlames(Level level, Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 9.0;

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.7F, 1.2F);

        // Find enemies in front cone
        AABB coneBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(2.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, coneBox, e -> e != player && !e.isSpectator());

        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().add(0, target.getBbHeight() / 2, 0).subtract(eyePos);
            double dist = toTarget.length();
            if (dist <= range) {
                double dot = look.dot(toTarget.normalize());
                if (dot > 0.65) {
                    target.setRemainingFireTicks(100);
                    target.hurt(player.damageSources().inFire(), 5.0F);
                }
            }
        }

        // Flame and smoke stream
        if (level instanceof ServerLevel sl) {
            for (int i = 1; i <= (int) range; i++) {
                Vec3 p = eyePos.add(look.scale(i));
                double spread = i * 0.08;
                sl.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 4, spread, spread, spread, 0.04);
                sl.sendParticles(ParticleTypes.SMOKE, p.x, p.y, p.z, 2, spread, spread, spread, 0.02);
            }
        }
    }
}
