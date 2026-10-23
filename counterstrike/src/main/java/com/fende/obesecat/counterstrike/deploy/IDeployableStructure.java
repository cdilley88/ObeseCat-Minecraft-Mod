package com.fende.obesecat.counterstrike.deploy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Interface implemented by deployable crate blocks (such as the MCV or structure crates).
 */
public interface IDeployableStructure {

    /**
     * The relative bounds required for this structure to unfold.
     */
    StructureBounds getBounds();

    /**
     * Checks if the structure can be deployed at the given origin position.
     */
    default boolean canDeploy(Level level, BlockPos originPos, Player player) {
        return getBounds().isClear(level, originPos, originPos);
    }

    /**
     * Deploys the multiblock structure into the world.
     * @return true if deployment succeeded.
     */
    boolean deploy(Level level, BlockPos originPos, Player player);

    /**
     * Human-readable name of the structure for EVA alerts.
     */
    String getStructureName();
}
