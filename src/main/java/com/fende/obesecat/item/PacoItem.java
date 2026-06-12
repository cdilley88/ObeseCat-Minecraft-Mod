package com.fende.obesecat.item;

import com.fende.obesecat.registry.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PacoItem extends Item {
    protected static final double RANGE = 64.0D;
    private static final String STINK_KEY = "PacoStink";
    private static final int MAX_STINK = 10;
    private static final int BARK_COOLDOWN_TICKS = 5;
    private static final int STINK_COOLDOWN_TICKS = 100;

    public PacoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        SoundEvent bark = getBarkSound(level);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), bark, SoundSource.PLAYERS, 1.0F, 0.95F + (level.random.nextFloat() * 0.1F));

        if (!level.isClientSide()) {
            applyBarkEffect(player);

            if (usesStinkMeter()) {
                int stink = getStink(stack) + 1;
                if (stink >= MAX_STINK) {
                    setStink(stack, 0);
                    player.getCooldowns().addCooldown(this, getStinkCooldownTicks());
                } else {
                    setStink(stack, stink);
                    player.getCooldowns().addCooldown(this, getBarkCooldownTicks());
                }
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    protected void applyBarkEffect(Player player) {
    }

    protected SoundEvent getBarkSound(Level level) {
        return switch (level.random.nextInt(3)) {
            case 0 -> ModSounds.PACO_BARK_1.get();
            case 1 -> ModSounds.PACO_BARK_2.get();
            default -> ModSounds.PACO_BARK_3.get();
        };
    }

    protected int getBarkCooldownTicks() {
        return BARK_COOLDOWN_TICKS;
    }

    protected int getStinkCooldownTicks() {
        return STINK_COOLDOWN_TICKS;
    }

    protected boolean usesStinkMeter() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return usesStinkMeter() && getStink(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getStink(stack) / MAX_STINK);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int stink = getStink(stack);
        int red = Math.max(24, 96 - stink * 6);
        int green = Math.min(255, 120 + stink * 13);
        int blue = Math.max(16, 64 - stink * 4);
        return red << 16 | green << 8 | blue;
    }

    protected LivingEntity findTarget(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getViewVector(1.0F).scale(RANGE));
        HitResult blockHit = player.pick(RANGE, 1.0F, false);
        double maxDistance = blockHit.getType() == HitResult.Type.MISS ? RANGE * RANGE : blockHit.getLocation().distanceToSqr(start);
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1.0D);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchBox,
                entity -> isValidTarget(player, entity),
                maxDistance
        );

        return entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private static boolean isValidTarget(Player player, Entity entity) {
        return entity instanceof LivingEntity
                && entity != player
                && entity.isAlive()
                && entity.isPickable()
                && !entity.isSpectator();
    }

    private static int getStink(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return Math.clamp(tag.getInt(STINK_KEY), 0, MAX_STINK);
    }

    private static void setStink(ItemStack stack, int stink) {
        int clampedStink = Math.clamp(stink, 0, MAX_STINK);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            if (clampedStink <= 0) {
                tag.remove(STINK_KEY);
            } else {
                tag.putInt(STINK_KEY, clampedStink);
            }
        });
    }
}
