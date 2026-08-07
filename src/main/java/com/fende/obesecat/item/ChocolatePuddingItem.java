package com.fende.obesecat.item;

import com.fende.obesecat.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class ChocolatePuddingItem extends BlockItem {
    public ChocolatePuddingItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        return placeBehindPlayer(context.getLevel(), player, context.getHand(), context.getItemInHand());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult result = placeBehindPlayer(level, player, hand, stack);
        return new InteractionResultHolder<>(result, stack);
    }

    private InteractionResult placeBehindPlayer(Level level, Player player, InteractionHand hand, ItemStack stack) {
        BlockPos behindPlayer = player.blockPosition().relative(player.getDirection().getOpposite());
        BlockPlaceContext redirected = new BlockPlaceContext(
                level,
                player,
                hand,
                stack,
                new BlockHitResult(Vec3.atCenterOf(behindPlayer), Direction.UP, behindPlayer, false)
        );

        if (!redirected.getClickedPos().equals(behindPlayer)) {
            return InteractionResult.FAIL;
        }

        return this.place(redirected);
    }

    @Override
    protected SoundEvent getPlaceSound(BlockState state, Level level, BlockPos pos, Player player) {
        return switch (level.random.nextInt(3)) {
            case 0 -> ModSounds.CHOCOLATE_PUDDING_1.get();
            case 1 -> ModSounds.CHOCOLATE_PUDDING_2.get();
            default -> ModSounds.CHOCOLATE_PUDDING_3.get();
        };
    }
}