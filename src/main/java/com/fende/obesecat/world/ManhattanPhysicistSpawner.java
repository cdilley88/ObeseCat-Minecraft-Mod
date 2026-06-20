package com.fende.obesecat.world;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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

        configureVillager(villager);
    }

    public static void configureVillager(Villager villager) {
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.PLAINS)
                .setProfession(ModVillagers.MANHATTAN_PHYSICIST.get())
                .setLevel(1));
        villager.setCustomName(Component.literal("Manhattan Physicist"));
        villager.setPersistenceRequired();
        villager.addTag(STRUCTURE_ENTITY_TAG);

        if (villager.level() instanceof ServerLevel serverLevel) {
            villager.refreshBrain(serverLevel);
        }

        LOGGER.info("Assigned Manhattan Physicist profession to villager at {}", villager.blockPosition());
    }

    private ManhattanPhysicistSpawner() {
    }
}
