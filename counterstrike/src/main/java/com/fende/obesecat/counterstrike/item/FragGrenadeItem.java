package com.fende.obesecat.counterstrike.item;

import com.fende.obesecat.counterstrike.entity.FragGrenadeProjectile;
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

public class FragGrenadeItem extends Item {
    public static final int THROW_COOLDOWN = 20; // 1 second

    public FragGrenadeItem() {
        super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            FragGrenadeProjectile grenade = new FragGrenadeProjectile(level, player);
            Vec3 look = player.getLookAngle();
            grenade.shoot(look.x, look.y + 0.15, look.z, 1.2F, 1.0F);
            level.addFreshEntity(grenade);

            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.9F);
            player.getCooldowns().addCooldown(this, THROW_COOLDOWN);

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
