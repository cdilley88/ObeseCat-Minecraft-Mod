package com.fende.obesecat.counterstrike.deploy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Interface implemented by the Master BlockEntity of an assembled multiblock structure.
 */
public interface IDeployedCore {

    /**
     * Packs the assembled structure back into its deployable crate/item form,
     * removing the multiblock structure blocks and giving the item back to the player.
     * @return true if pack-up succeeded.
     */
    boolean packUp(Level level, BlockPos corePos, Player player);

    /**
     * Rotates the assembled multiblock structure by 90 degrees clockwise around the core.
     * @return true if rotation succeeded.
     */
    default boolean rotateStructure(Level level, BlockPos corePos, Player player) {
        return false;
    }

    /**
     * Identifier of the structure.
     */
    String getStructureId();
}
