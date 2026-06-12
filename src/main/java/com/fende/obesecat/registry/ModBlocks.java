package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.AtomicFireBlock;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ObeseCatMod.MOD_ID);

    public static final DeferredBlock<AtomicFireBlock> ATOMIC_FIRE = BLOCKS.register(
            "atomic_fire",
            () -> new AtomicFireBlock()
    );

    private ModBlocks() {
    }
}
