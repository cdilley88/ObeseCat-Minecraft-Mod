package com.fende.obesecat.counterstrike.power;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * Server-wide manager for wireless base power grids, segmented by player UUID / Team.
 */
public class BasePowerManager extends SavedData {
    public static final String DATA_NAME = "counterstrike_power_grid";

    public static final int TURBINE_OUTPUT = 200;
    public static final int CON_YARD_DRAIN = 25;
    public static final int CONSTRUCTION_YARD_DRAIN = CON_YARD_DRAIN;
    public static final int BARRACKS_DRAIN = 20;

    public record GeneratorEntry(UUID owner, int power) {}
    public record ConsumerEntry(UUID owner, int power) {}

    private final Map<BlockPos, GeneratorEntry> generators = new HashMap<>();
    private final Map<BlockPos, ConsumerEntry> consumers = new HashMap<>();
    private final Map<UUID, BasePowerGrid> grids = new HashMap<>();

    public static SavedData.Factory<BasePowerManager> factory() {
        return new SavedData.Factory<>(BasePowerManager::new, BasePowerManager::load, null);
    }

    public static BasePowerManager get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public synchronized BasePowerGrid getGrid(UUID owner) {
        return grids.computeIfAbsent(owner, BasePowerGrid::new);
    }

    public synchronized void registerGenerator(BlockPos pos, UUID owner, int power) {
        if (owner == null) return;
        unregisterGenerator(pos);
        generators.put(pos, new GeneratorEntry(owner, power));
        recalculate(owner);
        setDirty();
    }

    public synchronized void unregisterGenerator(BlockPos pos) {
        GeneratorEntry removed = generators.remove(pos);
        if (removed != null) {
            recalculate(removed.owner());
            setDirty();
        }
    }

    public synchronized void registerConsumer(BlockPos pos, UUID owner, int power) {
        if (owner == null) return;
        unregisterConsumer(pos);
        consumers.put(pos, new ConsumerEntry(owner, power));
        recalculate(owner);
        setDirty();
    }

    public synchronized void unregisterConsumer(BlockPos pos) {
        ConsumerEntry removed = consumers.remove(pos);
        if (removed != null) {
            recalculate(removed.owner());
            setDirty();
        }
    }

    public synchronized void recalculate(UUID owner) {
        int gen = 0;
        for (GeneratorEntry g : generators.values()) {
            if (g.owner().equals(owner)) {
                gen += g.power();
            }
        }
        int con = 0;
        for (ConsumerEntry c : consumers.values()) {
            if (c.owner().equals(owner)) {
                con += c.power();
            }
        }
        BasePowerGrid grid = getGrid(owner);
        grid.setGenerated(gen);
        grid.setConsumed(con);
    }

    public static BasePowerManager load(CompoundTag tag, HolderLookup.Provider provider) {
        BasePowerManager manager = new BasePowerManager();

        if (tag.contains("Generators", Tag.TAG_LIST)) {
            ListTag genList = tag.getList("Generators", Tag.TAG_COMPOUND);
            for (int i = 0; i < genList.size(); i++) {
                CompoundTag entry = genList.getCompound(i);
                if (entry.contains("Pos") && entry.contains("Owner") && entry.contains("Power")) {
                    NbtUtils.readBlockPos(entry, "Pos").ifPresent(pos -> {
                        UUID owner = entry.getUUID("Owner");
                        int power = entry.getInt("Power");
                        manager.generators.put(pos, new GeneratorEntry(owner, power));
                    });
                }
            }
        }

        if (tag.contains("Consumers", Tag.TAG_LIST)) {
            ListTag conList = tag.getList("Consumers", Tag.TAG_COMPOUND);
            for (int i = 0; i < conList.size(); i++) {
                CompoundTag entry = conList.getCompound(i);
                if (entry.contains("Pos") && entry.contains("Owner") && entry.contains("Power")) {
                    NbtUtils.readBlockPos(entry, "Pos").ifPresent(pos -> {
                        UUID owner = entry.getUUID("Owner");
                        int power = entry.getInt("Power");
                        manager.consumers.put(pos, new ConsumerEntry(owner, power));
                    });
                }
            }
        }

        Set<UUID> allOwners = new HashSet<>();
        for (GeneratorEntry g : manager.generators.values()) allOwners.add(g.owner());
        for (ConsumerEntry c : manager.consumers.values()) allOwners.add(c.owner());
        for (UUID owner : allOwners) {
            manager.recalculate(owner);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag genList = new ListTag();
        for (Map.Entry<BlockPos, GeneratorEntry> entry : generators.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
            c.putUUID("Owner", entry.getValue().owner());
            c.putInt("Power", entry.getValue().power());
            genList.add(c);
        }
        tag.put("Generators", genList);

        ListTag conList = new ListTag();
        for (Map.Entry<BlockPos, ConsumerEntry> entry : consumers.entrySet()) {
            CompoundTag c = new CompoundTag();
            c.put("Pos", NbtUtils.writeBlockPos(entry.getKey()));
            c.putUUID("Owner", entry.getValue().owner());
            c.putInt("Power", entry.getValue().power());
            conList.add(c);
        }
        tag.put("Consumers", conList);

        return tag;
    }
}
