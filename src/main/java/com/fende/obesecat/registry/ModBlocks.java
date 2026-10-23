package com.fende.obesecat.registry;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.block.AtomicFireBlock;
import com.fende.obesecat.block.ChocolatePuddingBlock;
import com.fende.obesecat.block.CreativeTabbyriumSiloBlock;
import com.fende.obesecat.block.EchoingBlastChamberBlock;
import com.fende.obesecat.block.NuclearLibraryBlock;
import com.fende.obesecat.block.TabbyriumBlossomSaplingBlock;
import com.fende.obesecat.block.TabbyriumClusterBlock;
import com.fende.obesecat.block.TabbyriumCrustBlock;
import com.fende.obesecat.block.TabbyriumRefineryBlock;
import com.fende.obesecat.block.TabbyriumSiloBlock;
import com.fende.obesecat.block.TabbyriumSporePodBlock;
import com.fende.obesecat.block.TabbyriumStalkBlock;
import com.fende.obesecat.block.ToiletBlock;
import com.fende.obesecat.block.TrinititeBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
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

    public static final DeferredBlock<ChocolatePuddingBlock> CHOCOLATE_PUDDING = BLOCKS.register(
            "chocolate_pudding",
            () -> new ChocolatePuddingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE))
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

    // Tabbyrium System
    public static final DeferredBlock<TabbyriumBlossomSaplingBlock> TABBYRIUM_BLOSSOM_SAPLING = BLOCKS.register(
            "tabbyrium_blossom_sapling",
            () -> new TabbyriumBlossomSaplingBlock()
    );

    public static final DeferredBlock<TabbyriumSporePodBlock> TABBYRIUM_SPORE_POD = BLOCKS.register(
            "tabbyrium_spore_pod",
            () -> new TabbyriumSporePodBlock()
    );

    public static final DeferredBlock<TabbyriumCrustBlock> TABBYRIUM_CRUST = BLOCKS.register(
            "tabbyrium_crust",
            () -> new TabbyriumCrustBlock()
    );

    public static final DeferredBlock<TabbyriumStalkBlock> TABBYRIUM_STALK = BLOCKS.register(
            "tabbyrium_stalk",
            () -> new TabbyriumStalkBlock()
    );

    public static final DeferredBlock<Block> TABBYRIUM_PLANKS = BLOCKS.register(
            "tabbyrium_planks",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(2.0F, 3.0F)
                    .sound(SoundType.WOOD))
    );

    public static final DeferredBlock<TabbyriumClusterBlock> TABBYRIUM_CLUSTER = BLOCKS.register(
            "tabbyrium_cluster",
            () -> new TabbyriumClusterBlock(BlockBehaviour.Properties.of()
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(state -> state.getValue(TabbyriumClusterBlock.AGE) * 3 + 2)
                    .noOcclusion())
    );

    public static final DeferredBlock<TabbyriumRefineryBlock> TABBYRIUM_REFINERY = BLOCKS.register(
            "tabbyrium_refinery",
            () -> new TabbyriumRefineryBlock()
    );

    public static final DeferredBlock<TabbyriumSiloBlock> TABBYRIUM_SILO = BLOCKS.register(
            "tabbyrium_silo",
            () -> new TabbyriumSiloBlock()
    );

    public static final DeferredBlock<CreativeTabbyriumSiloBlock> CREATIVE_TABBYRIUM_SILO = BLOCKS.register(
            "creative_tabbyrium_silo",
            () -> new CreativeTabbyriumSiloBlock()
    );

    // Compatibility Registrations
    public static final DeferredBlock<TabbyriumRefineryBlock> CAN_OPENER = BLOCKS.register(
            "can_opener",
            () -> new TabbyriumRefineryBlock()
    );

    public static final DeferredBlock<TabbyriumSiloBlock> FOOD_BIN = BLOCKS.register(
            "food_bin",
            () -> new TabbyriumSiloBlock()
    );

    public static final DeferredBlock<CreativeTabbyriumSiloBlock> CREATIVE_FOOD_BIN = BLOCKS.register(
            "creative_food_bin",
            () -> new CreativeTabbyriumSiloBlock()
    );

    private ModBlocks() {
    }
}
