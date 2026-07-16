package com.fende.obesecat.world;

import com.fende.obesecat.registry.ModVillagerTrades;
import com.fende.obesecat.registry.ModVillagers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerType;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManhattanPhysicistSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManhattanPhysicistSpawner.class);
    public static final String STRUCTURE_ENTITY_TAG = "obesecat_manhattan_physicist";
    public static final String PHYSICIST_ENTITY_TAG = "obesecat_manhattan_physicist_initialized";
    private static final Component DISPLAY_NAME = Component.literal("Manhattan Physicist");

    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Villager villager)) {
            return;
        }

        boolean structureResident = villager.getTags().contains(STRUCTURE_ENTITY_TAG);
        if (!structureResident && !villager.getTags().contains(PHYSICIST_ENTITY_TAG)) {
            return;
        }

        BlockPos workstation = structureResident
                ? ManhattanBunkerWorkstation.findNearestWorkstation(event.getLevel(), villager.blockPosition(), 12)
                : null;
        normalizeExistingVillager(villager, workstation);
    }

    public static void initializeEggVillager(Villager villager) {
        initializeVillager(villager, null, false);
    }

    public static void configureVillager(Villager villager) {
        initializeEggVillager(villager);
    }

    public static void configureVillager(Villager villager, BlockPos workstationPos) {
        initializeVillager(villager, workstationPos, true);
    }

    private static void initializeVillager(Villager villager, BlockPos workstationPos, boolean structureResident) {
        setProfession(villager);
        bindWorkstation(villager, workstationPos);
        villager.setVillagerXp(Math.max(1, villager.getVillagerXp()));
        villager.setCustomName(DISPLAY_NAME);
        villager.setPersistenceRequired();
        villager.addTag(PHYSICIST_ENTITY_TAG);
        if (structureResident) {
            villager.addTag(STRUCTURE_ENTITY_TAG);
        }
        villager.overrideOffers(ModVillagerTrades.createManhattanPhysicistOffers(villager));
        refreshBrain(villager);
        LOGGER.info("Initialized Manhattan Physicist with custom profession and trades at {}", villager.blockPosition());
    }

    private static void normalizeExistingVillager(Villager villager, BlockPos workstationPos) {
        boolean professionChanged = villager.getVillagerData().getProfession() != ModVillagers.MANHATTAN_PHYSICIST.get();
        if (professionChanged) {
            setProfession(villager);
        }
        bindWorkstation(villager, workstationPos);
        villager.setVillagerXp(Math.max(1, villager.getVillagerXp()));
        villager.setCustomName(DISPLAY_NAME);
        villager.setPersistenceRequired();
        villager.addTag(PHYSICIST_ENTITY_TAG);
        if (villager.getOffers().isEmpty()) {
            villager.overrideOffers(ModVillagerTrades.createManhattanPhysicistOffers(villager));
        }
        if (professionChanged || workstationPos != null) {
            refreshBrain(villager);
        }
    }

    private static void setProfession(Villager villager) {
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(ModVillagers.MANHATTAN_PHYSICIST.get())
                .setLevel(Math.max(1, villager.getVillagerData().getLevel())));
    }

    private static void bindWorkstation(Villager villager, BlockPos workstationPos) {
        if (!(villager.level() instanceof ServerLevel serverLevel) || workstationPos == null) {
            return;
        }
        villager.getBrain().eraseMemory(MemoryModuleType.POTENTIAL_JOB_SITE);
        villager.getBrain().setMemory(MemoryModuleType.JOB_SITE,
                GlobalPos.of(serverLevel.dimension(), workstationPos));
    }

    private static void refreshBrain(Villager villager) {
        if (villager.level() instanceof ServerLevel serverLevel) {
            villager.refreshBrain(serverLevel);
        }
    }

    private ManhattanPhysicistSpawner() {
    }
}
