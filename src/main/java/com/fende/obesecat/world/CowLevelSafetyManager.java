package com.fende.obesecat.world;

import net.minecraft.world.entity.monster.Phantom;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

public final class CowLevelSafetyManager {
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Phantom)) {
            return;
        }

        if (!event.getLevel().dimension().equals(CowLevelPortalManager.SECRET_COW_LEVEL)) {
            return;
        }

        event.setCanceled(true);
    }

    private CowLevelSafetyManager() {
    }
}
