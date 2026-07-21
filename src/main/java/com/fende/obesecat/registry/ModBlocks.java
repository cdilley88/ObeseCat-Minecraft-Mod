package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.AtomicFireBlock;
import com.fende.obesecat.block.AtomicCanOpenerBlock;
import com.fende.obesecat.block.FoodBinBlock;
import com.fende.obesecat.block.CatChargerBlock;
import com.fende.obesecat.block.CreativeFoodBinBlock;
import com.fende.obesecat.block.NuclearLibraryBlock;
import com.fende.obesecat.block.TrinititeBlock;
import com.fende.obesecat.block.ToiletBlock;
import com.fende.obesecat.block.EchoingBlastChamberBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ObeseCatMod.MOD_ID);

    public static final DeferredBlock<AtomicFireBlock> ATOMIC_FIRE = BLOCKS.register(
            "atomic_fire",
            () -> new AtomicFireBlock()
    );

    public static final DeferredBlock<ToiletBlock> TOILET = BLOCKS.register(
            "toilet",
            () -> new ToiletBlock()
    );

    public static final DeferredBlock<TrinititeBlock> TRINITITE = BLOCKS.register(
            "trinitite",
            () -> new TrinititeBlock()
    );

    public static final DeferredBlock<NuclearLibraryBlock> NUCLEAR_LIBRARY = BLOCKS.register(
            "nuclear_library",
            () -> new NuclearLibraryBlock()
    );

    public static final DeferredBlock<EchoingBlastChamberBlock> ECHOING_BLAST_CHAMBER = BLOCKS.register(
            "echoing_blast_chamber",
            () -> new EchoingBlastChamberBlock()
    );

    public static final DeferredBlock<AtomicCanOpenerBlock> CAN_OPENER = BLOCKS.register(
            "can_opener",
            () -> new AtomicCanOpenerBlock()
    );
    public static final DeferredBlock<FoodBinBlock> FOOD_BIN = BLOCKS.register(
            "food_bin",
            () -> new FoodBinBlock()
    );

    public static final DeferredBlock<CatChargerBlock> CAT_CHARGER = BLOCKS.register(
            "cat_charger",
            () -> new CatChargerBlock()
    );

    public static final DeferredBlock<CreativeFoodBinBlock> CREATIVE_FOOD_BIN = BLOCKS.register(
            "creative_food_bin",
            () -> new CreativeFoodBinBlock()
    );

    private ModBlocks() {
    }
}
