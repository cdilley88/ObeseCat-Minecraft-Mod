package com.fende.obesecat.world;

import com.fende.obesecat.network.FissionFirestormPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FissionFirestorm {
    private static final double VISUAL_RADIUS = 384.0D;

    private FissionFirestorm() {
    }

    public static void detonate(ServerLevel level, BlockPos origin) {
        triggerFirestormOverlay(level, origin);
        NuclearCatExplosion.flashThenDetonate(
                level,
                origin,
                NuclearCatExplosion.LITHIUM_CRATER_RADIUS,
                NuclearCatExplosion.LITHIUM_CRATER_MAX_DEPTH
        );
        AtomicFireSphere.createDelayed(level, origin, NuclearCatExplosion.FLASH_TO_CRATER_DELAY_TICKS + 2);
    }

    private static void triggerFirestormOverlay(ServerLevel level, BlockPos origin) {
        PacketDistributor.sendToPlayersNear(
                level,
                null,
                origin.getX() + 0.5D,
                origin.getY() + 0.5D,
                origin.getZ() + 0.5D,
                VISUAL_RADIUS,
                new FissionFirestormPayload(AtomicFireSphere.LIFETIME_TICKS)
        );
    }
}
