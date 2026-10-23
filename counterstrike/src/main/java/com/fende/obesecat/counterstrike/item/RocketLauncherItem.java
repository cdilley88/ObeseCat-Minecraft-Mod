package com.fende.obesecat.counterstrike.item;

import com.fende.obesecat.counterstrike.entity.RocketProjectile;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RocketLauncherItem extends Item {
    public static final int RELOAD_TICKS = 60; // 3 seconds

    public RocketLauncherItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            RocketProjectile rocket = new RocketProjectile(level, player);
            Vec3 look = player.getLookAngle();
            rocket.shoot(look.x, look.y, look.z, 2.2F, 0.5F);
            level.addFreshEntity(rocket);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.2F, 0.8F);
            player.getCooldowns().addCooldown(this, RELOAD_TICKS);
            player.displayClientMessage(Component.literal("§e[EVA] §fReloading Rocket tube..."), true);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
