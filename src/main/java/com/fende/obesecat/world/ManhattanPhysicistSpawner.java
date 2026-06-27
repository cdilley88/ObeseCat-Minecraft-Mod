package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModVillagerTrades;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fende.obesecat.registry.ModVillagers;

public final class ManhattanPhysicistSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManhattanPhysicistSpawner.class);
    public static final String STRUCTURE_ENTITY_TAG = "obesecat_manhattan_physicist";

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Villager villager)) {
            return;
        }

        if (!villager.getTags().contains(STRUCTURE_ENTITY_TAG)) {
            return;
        }

        configureVillager(villager, ManhattanBunkerWorkstation.findNearestWorkstation(event.getLevel(), villager.blockPosition(), 12));
    }

    public static void configureVillager(Villager villager) {
        configureVillager(villager, null);
    }

    public static void configureVillager(Villager villager, BlockPos workstationPos) {
        if (villager.level() instanceof ServerLevel serverLevel && workstationPos != null) {
            villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
            villager.getBrain().setMemory(MemoryModuleType.JOB_SITE, GlobalPos.of(serverLevel.dimension(), workstationPos));
            LOGGER.info("Bound Manhattan Physicist villager at {} to Nuclear Library at {}", villager.blockPosition(), workstationPos);
        }

        if (villager.level() instanceof ServerLevel serverLevel) {
            villager.refreshBrain(serverLevel);
        }

        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(ModVillagers.MANHATTAN_PHYSICIST.get())
                .setLevel(1));
        villager.setVillagerXp(0);
        villager.setCustomName(Component.literal("Manhattan Physicist"));
        villager.setPersistenceRequired();
        villager.addTag(STRUCTURE_ENTITY_TAG);
        villager.overrideOffers(ModVillagerTrades.createManhattanPhysicistOffers(villager));

        LOGGER.info("Assigned Manhattan Physicist profession and custom trades to villager at {}", villager.blockPosition());
    }

    private ManhattanPhysicistSpawner() {
    }
}
