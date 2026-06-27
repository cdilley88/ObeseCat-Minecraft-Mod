package com.fende.obesecat.world;

import com.fende.obesecat.ObeseCatMod;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManhattanBunkerResidentSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManhattanBunkerResidentSpawner.class);
    private static final ResourceKey<Structure> BUNKER_STRUCTURE = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_bunker")
    );

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }

        Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Holder.Reference<Structure> bunkerHolder = registry.getHolder(BUNKER_STRUCTURE).orElse(null);
        if (bunkerHolder == null) {
            return;
        }

        for (StructureStart structureStart : chunk.getAllStarts().values()) {
            if (!structureStart.isValid() || structureStart.getStructure() != bunkerHolder.value()) {
                continue;
            }

            if (!structureStart.getChunkPos().equals(chunk.getPos())) {
                continue;
            }

            ensureResident(level, structureStart.getBoundingBox());
        }
    }

    private static void ensureResident(ServerLevel level, BoundingBox box) {
        int replaced = ManhattanBunkerWorkstation.replaceLecterns(level, box);
        if (replaced > 0) {
            LOGGER.info("Replaced {} vanilla lectern(s) with Nuclear Library blocks in Manhattan bunker at {}", replaced, box.getCenter());
        }
        int cleaned = ManhattanBunkerWorkstation.cleanInteriorFloor(level, box);
        if (cleaned > 0) {
            LOGGER.info("Cleaned {} bunker floor/interior plant block(s) at {}", cleaned, box.getCenter());
        }

        AABB searchBox = new AABB(
                box.minX(), box.minY(), box.minZ(),
                box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1
        );

        List<Villager> existing = level.getEntitiesOfClass(Villager.class, searchBox, villager ->
                villager.getTags().contains(ManhattanPhysicistSpawner.STRUCTURE_ENTITY_TAG));
        if (!existing.isEmpty()) {
            return;
        }

        BlockPos workstationPos = ManhattanBunkerWorkstation.findWorkstationPos(level, box);
        BlockPos spawnPos = ManhattanBunkerWorkstation.findSpawnPos(level, box);
        if (spawnPos == null) {
            spawnPos = new BlockPos((box.minX() + box.maxX()) / 2, box.minY() + 1, (box.minZ() + box.maxZ()) / 2);
            LOGGER.warn("No Nuclear Library spawn position found in Manhattan bunker at {}; using center fallback {}", box.getCenter(), spawnPos);
        }

        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            LOGGER.warn("Failed to create Manhattan Physicist villager for bunker at {}", box.getCenter());
            return;
        }

        villager.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        ManhattanPhysicistSpawner.configureVillager(villager, workstationPos);
        level.addFreshEntity(villager);
        LOGGER.info("Spawned Manhattan Physicist inside Manhattan bunker at {}", spawnPos);
    }

    private ManhattanBunkerResidentSpawner() {
    }
}
