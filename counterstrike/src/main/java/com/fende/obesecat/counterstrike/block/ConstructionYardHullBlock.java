package com.fende.obesecat.counterstrike.block;

import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.block.entity.ConstructionYardHullBlockEntity;
import com.fende.obesecat.counterstrike.item.CommandCursorItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ConstructionYardHullBlock extends BaseEntityBlock {
    public static final MapCodec<ConstructionYardHullBlock> CODEC = simpleCodec(ConstructionYardHullBlock::new);

    public ConstructionYardHullBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public ConstructionYardHullBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_GRAY)
                .strength(5.0F, 1200.0F)
                .sound(SoundType.NETHERITE_BLOCK)
                .requiresCorrectToolForDrops());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ConstructionYardHullBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            if (player.getMainHandItem().getItem() instanceof CommandCursorItem) {
                if (player.isShiftKeyDown()) {
                    if (level.getBlockEntity(pos) instanceof ConstructionYardHullBlockEntity hull && hull.getCorePos() != null) {
                        if (level.getBlockEntity(hull.getCorePos()) instanceof ConstructionYardCoreBlockEntity core) {
                            core.packUp(level, hull.getCorePos(), player);
                            player.displayClientMessage(
                                    Component.literal("§a[EVA] §fStructure disassembled. Transport module secured."),
                                    true
                            );
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fConstruction Yard structural hull intact. Access Master Core at base center."),
                    true
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ConstructionYardHullBlockEntity hull && hull.getCorePos() != null) {
                if (level.getBlockEntity(hull.getCorePos()) instanceof ConstructionYardCoreBlockEntity core) {
                    core.packUp(level, hull.getCorePos(), player);
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
