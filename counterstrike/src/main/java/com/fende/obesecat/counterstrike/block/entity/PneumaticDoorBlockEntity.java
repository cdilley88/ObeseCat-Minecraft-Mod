package com.fende.obesecat.counterstrike.block.entity;

import com.fende.obesecat.counterstrike.block.PneumaticDoorBlock;
import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.Nullable;

public class PneumaticDoorBlockEntity extends BlockEntity {
    private int tickCount = 0;
    private BlockPos corePos = BlockPos.ZERO;

    public PneumaticDoorBlockEntity(BlockPos pos, BlockState state) {
        super(CSBlockEntities.PNEUMATIC_DOOR.get(), pos, state);
    }

    public void setCorePos(BlockPos pos) {
        this.corePos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getCorePos() {
        return corePos.equals(BlockPos.ZERO) ? null : corePos;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PneumaticDoorBlockEntity blockEntity) {
        if (!state.hasProperty(PneumaticDoorBlock.HALF) || state.getValue(PneumaticDoorBlock.HALF) != DoubleBlockHalf.LOWER) {
            return;
        }

        if (++blockEntity.tickCount % 4 != 0) {
            return;
        }

        boolean isOpen = state.getValue(PneumaticDoorBlock.OPEN);
        AABB checkArea = new AABB(pos).inflate(3.2, 1.5, 3.2);
        boolean playerNearby = !level.getEntitiesOfClass(Player.class, checkArea, EntitySelector.NO_SPECTATORS).isEmpty();

        if (playerNearby && !isOpen) {
            PneumaticDoorBlock.setOpenState(level, pos, state, true);
        } else if (!playerNearby && isOpen) {
            PneumaticDoorBlock.setOpenState(level, pos, state, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (corePos != null && !corePos.equals(BlockPos.ZERO)) {
            tag.put("CorePos", NbtUtils.writeBlockPos(corePos));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("CorePos")) {
            NbtUtils.readBlockPos(tag, "CorePos").ifPresent(pos -> this.corePos = pos);
        }
    }
}
