package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.AtomicFireBlock;
import com.fende.obesecat.block.NuclearLibraryBlock;
import com.fende.obesecat.block.TrinititeBlock;
import com.fende.obesecat.block.ToiletBlock;
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

    private ModBlocks() {
    }
}
