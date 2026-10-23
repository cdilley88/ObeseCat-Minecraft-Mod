package com.fende.obesecat.counterstrike.registry;

import com.fende.obesecat.counterstrike.ObeseCatCounterstrike;
import com.fende.obesecat.counterstrike.block.ConstructionYardCoreBlock;
import com.fende.obesecat.counterstrike.block.ConstructionYardHullBlock;
import com.fende.obesecat.counterstrike.block.MeowbileConstructionVehicleBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CSBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ObeseCatCounterstrike.MOD_ID);

    public static final DeferredBlock<MeowbileConstructionVehicleBlock> MEOWBILE_CONSTRUCTION_VEHICLE = BLOCKS.register(
            "meowbile_construction_vehicle",
            () -> new MeowbileConstructionVehicleBlock()
    );

    public static final DeferredBlock<ConstructionYardCoreBlock> CONSTRUCTION_YARD_CORE = BLOCKS.register(
            "construction_yard_core",
            () -> new ConstructionYardCoreBlock()
    );

    public static final DeferredBlock<ConstructionYardHullBlock> CONSTRUCTION_YARD_HULL = BLOCKS.register(
            "construction_yard_hull",
            () -> new ConstructionYardHullBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.PneumaticDoorBlock> PNEUMATIC_DOOR = BLOCKS.register(
            "pneumatic_door",
            () -> new com.fende.obesecat.counterstrike.block.PneumaticDoorBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.FissionTurbineCrateBlock> FISSION_TURBINE_CRATE = BLOCKS.register(
            "fission_turbine_crate",
            () -> new com.fende.obesecat.counterstrike.block.FissionTurbineCrateBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.FissionTurbineCoreBlock> FISSION_TURBINE_CORE = BLOCKS.register(
            "fission_turbine_core",
            () -> new com.fende.obesecat.counterstrike.block.FissionTurbineCoreBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.TurbineReactorCoreBlock> TURBINE_REACTOR_CORE = BLOCKS.register(
            "turbine_reactor_core",
            () -> new com.fende.obesecat.counterstrike.block.TurbineReactorCoreBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.BarracksCrateBlock> BARRACKS_CRATE = BLOCKS.register(
            "barracks_crate",
            () -> new com.fende.obesecat.counterstrike.block.BarracksCrateBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.BarracksCoreBlock> BARRACKS_CORE = BLOCKS.register(
            "barracks_core",
            () -> new com.fende.obesecat.counterstrike.block.BarracksCoreBlock()
    );

    public static final DeferredBlock<com.fende.obesecat.counterstrike.block.ArmoryLockerBlock> ARMORY_LOCKER = BLOCKS.register(
            "armory_locker",
            () -> new com.fende.obesecat.counterstrike.block.ArmoryLockerBlock()
    );

    private CSBlocks() {
    }
}
