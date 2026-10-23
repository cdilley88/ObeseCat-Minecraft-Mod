package com.fende.obesecat.counterstrike.block.entity;

import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Structural hull block entity that maintains a reference to the Master Core of the Construction Yard.
 */
public class ConstructionYardHullBlockEntity extends BlockEntity {
    private BlockPos corePos = BlockPos.ZERO;

    public ConstructionYardHullBlockEntity(BlockPos pos, BlockState state) {
        super(CSBlockEntities.CONSTRUCTION_YARD_HULL.get(), pos, state);
    }

    public void setCorePos(BlockPos pos) {
        this.corePos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getCorePos() {
        return corePos.equals(BlockPos.ZERO) ? null : corePos;
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
