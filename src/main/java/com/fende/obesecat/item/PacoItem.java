package com.fende.obesecat.item;

import com.fende.obesecat.registry.ModSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PacoItem extends Item {
    protected static final double DEFAULT_RANGE = 64.0D;
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
            } else if (getBarkCooldownTicks() > 0) {
                player.getCooldowns().addCooldown(this, getBarkCooldownTicks());
            }
        } else if (!usesStinkMeter() && getBarkCooldownTicks() > 0) {
            player.getCooldowns().addCooldown(this, getBarkCooldownTicks());
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

    protected double getRange() {
        return DEFAULT_RANGE;
    }

    protected String getCaptionKey() {
        return getClass() == PacoItem.class ? "item.obesecat.paco.caption" : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        String captionKey = getCaptionKey();
        if (captionKey != null) {
            tooltipComponents.add(Component.translatable(captionKey).withStyle(ChatFormatting.YELLOW));
        }
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

    protected double getHitboxWiggleRoom() {
        return 0.85D; // Generous aiming tolerance for companion barks
    }

    protected LivingEntity findTarget(Player player) {
        double range = getRange();
        Vec3 start = player.getEyePosition();
        Vec3 viewVec = player.getViewVector(1.0F).normalize();
        Vec3 end = start.add(viewVec.scale(range));
        HitResult blockHit = player.pick(range, 1.0F, false);
        double maxDistanceSqr = blockHit.getType() == HitResult.Type.MISS ? range * range : blockHit.getLocation().distanceToSqr(start);

        double baseWiggle = getHitboxWiggleRoom();
        double searchInflation = Math.max(2.0D, baseWiggle + 1.5D);
        AABB searchBox = player.getBoundingBox().expandTowards(end.subtract(start)).inflate(searchInflation);

        List<LivingEntity> potentialTargets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> isValidTarget(player, entity)
        );

        LivingEntity bestDirectHit = null;
        double bestDirectDistSqr = Double.MAX_VALUE;

        LivingEntity bestWiggleHit = null;
        double bestWiggleScore = Double.MAX_VALUE;

        for (LivingEntity entity : potentialTargets) {
            // 1. Check exact bounding box for direct crosshair aim
            AABB exactBox = entity.getBoundingBox().inflate((double) entity.getPickRadius());
            Optional<Vec3> exactClip = exactBox.clip(start, end);
            if (exactBox.contains(start)) {
                return entity; // Immediate point-blank direct hit
            } else if (exactClip.isPresent()) {
                double distSqr = start.distanceToSqr(exactClip.get());
                if (distSqr <= maxDistanceSqr && distSqr < bestDirectDistSqr) {
                    bestDirectDistSqr = distSqr;
                    bestDirectHit = entity;
                }
                continue;
            }

            // 2. If no direct hit and item provides wiggle room, check expanded hit tolerance
            if (baseWiggle > 0.0D) {
                Vec3 entityCenter = entity.getBoundingBox().getCenter();
                Vec3 toCenter = entityCenter.subtract(start);
                double projectionAlongRay = toCenter.dot(viewVec);
                if (projectionAlongRay < 0.0D || projectionAlongRay * projectionAlongRay > maxDistanceSqr) {
                    continue;
                }

                // Slight expansion with distance (acoustic cone expansion), capped reasonably
                double effectiveWiggle = Math.min(2.0D, baseWiggle + (projectionAlongRay * 0.02D));
                AABB inflatedBox = entity.getBoundingBox().inflate(effectiveWiggle);
                Optional<Vec3> inflatedClip = inflatedBox.clip(start, end);
                if (inflatedBox.contains(start) || inflatedClip.isPresent()) {
                    double clipDistSqr = inflatedClip.map(start::distanceToSqr).orElse(0.0D);
                    if (clipDistSqr <= maxDistanceSqr) {
                        // Score based on distance to crosshair ray, favoring entities closest to where the player aimed
                        double perpDistSqr = Math.max(0.0D, toCenter.lengthSqr() - (projectionAlongRay * projectionAlongRay));
                        if (perpDistSqr < bestWiggleScore) {
                            bestWiggleScore = perpDistSqr;
                            bestWiggleHit = entity;
                        }
                    }
                }
            }
        }

        // Direct crosshair hits always take priority over generous wiggle hits
        return bestDirectHit != null ? bestDirectHit : bestWiggleHit;
    }

    protected BlockPos resolveTargetPosition(Player player, double range) {
        HitResult hitResult = player.pick(range, 1.0F, false);
        if (hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos();
        }

        LivingEntity target = findTarget(player);
        if (target != null) {
            return target.blockPosition();
        }

        if (hitResult.getType() != HitResult.Type.MISS) {
            return BlockPos.containing(hitResult.getLocation());
        }

        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 eyePos = player.getEyePosition();
            Vec3 viewVec = player.getViewVector(1.0F);
            Vec3 traceEnd = eyePos.add(viewVec.scale(range));

            if (serverLevel.dimensionType().hasCeiling()) {
                return BlockPos.containing(traceEnd);
            }

            int targetX = (int) Math.floor(traceEnd.x);
            int targetZ = (int) Math.floor(traceEnd.z);
            int surfaceY = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(targetX, 0, targetZ)).getY();
            if (surfaceY > serverLevel.getMinBuildHeight() && surfaceY < serverLevel.getMaxBuildHeight()) {
                return new BlockPos(targetX, Math.max(serverLevel.getMinBuildHeight() + 2, surfaceY), targetZ);
            }
            return BlockPos.containing(traceEnd);
        }

        return null;
    }

    protected BlockPos resolveTargetPosition(Player player) {
        return resolveTargetPosition(player, getRange());
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
