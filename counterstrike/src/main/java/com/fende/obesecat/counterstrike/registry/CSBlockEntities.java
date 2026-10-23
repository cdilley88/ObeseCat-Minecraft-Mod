package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.block.entity.ConstructionYardCoreBlockEntity;
import com.fende.obesecat.counterstrike.block.entity.ConstructionYardHullBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CSBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ObeseCatCounterstrike.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConstructionYardCoreBlockEntity>> CONSTRUCTION_YARD_CORE =
            BLOCK_ENTITIES.register("construction_yard_core", () -> BlockEntityType.Builder.of(
                    ConstructionYardCoreBlockEntity::new,
                    CSBlocks.CONSTRUCTION_YARD_CORE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ConstructionYardHullBlockEntity>> CONSTRUCTION_YARD_HULL =
            BLOCK_ENTITIES.register("construction_yard_hull", () -> BlockEntityType.Builder.of(
                    ConstructionYardHullBlockEntity::new,
                    CSBlocks.CONSTRUCTION_YARD_HULL.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.fende.obesecat.counterstrike.block.entity.PneumaticDoorBlockEntity>> PNEUMATIC_DOOR =
            BLOCK_ENTITIES.register("pneumatic_door", () -> BlockEntityType.Builder.of(
                    com.fende.obesecat.counterstrike.block.entity.PneumaticDoorBlockEntity::new,
                    CSBlocks.PNEUMATIC_DOOR.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.fende.obesecat.counterstrike.block.entity.FissionTurbineCoreBlockEntity>> FISSION_TURBINE_CORE =
            BLOCK_ENTITIES.register("fission_turbine_core", () -> BlockEntityType.Builder.of(
                    com.fende.obesecat.counterstrike.block.entity.FissionTurbineCoreBlockEntity::new,
                    CSBlocks.FISSION_TURBINE_CORE.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.fende.obesecat.counterstrike.block.entity.BarracksCoreBlockEntity>> BARRACKS_CORE =
            BLOCK_ENTITIES.register("barracks_core", () -> BlockEntityType.Builder.of(
                    com.fende.obesecat.counterstrike.block.entity.BarracksCoreBlockEntity::new,
                    CSBlocks.BARRACKS_CORE.get()
            ).build(null));

    private CSBlockEntities() {
    }
}
