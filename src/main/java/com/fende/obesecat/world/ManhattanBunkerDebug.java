package com.fende.obesecat.world;

import com.fende.obesecat.ObeseCatMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ManhattanBunkerDebug {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManhattanBunkerDebug.class);
    private static final com.mojang.brigadier.exceptions.SimpleCommandExceptionType ERROR_TEMPLATE_MISSING =
            new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal("Manhattan bunker template is missing"));
    private static final com.mojang.brigadier.exceptions.SimpleCommandExceptionType ERROR_TEMPLATE_PLACE_FAILED =
            new com.mojang.brigadier.exceptions.SimpleCommandExceptionType(Component.literal("Failed to place Manhattan bunker template"));
    private static final ResourceLocation BUNKER_TEMPLATE_ID = ResourceLocation.fromNamespaceAndPath(
            ObeseCatMod.MOD_ID,
            "village/plains/houses/manhattan_bunker"
    );
    private static final ResourceKey<StructureTemplatePool> BUNKER_START_POOL = ResourceKey.create(
            Registries.TEMPLATE_POOL,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_bunker/start_pool")
    );
    private static final ResourceKey<Structure> BUNKER_STRUCTURE = ResourceKey.create(
            Registries.STRUCTURE,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_bunker")
    );
    private static final ResourceKey<StructureSet> BUNKER_STRUCTURE_SET = ResourceKey.create(
            Registries.STRUCTURE_SET,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_bunker")
    );
    private static final TagKey<Biome> BUNKER_BIOME_TAG = TagKey.create(
            Registries.BIOME,
            ResourceLocation.fromNamespaceAndPath(ObeseCatMod.MOD_ID, "manhattan_bunker_spawnable")
    );

    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal("obesecat")
                        .requires(source -> source.hasPermission(2))
                        .then(
                                Commands.literal("place_bunker")
                                        .executes(context -> placeBunker(context.getSource(), BlockPos.containing(context.getSource().getPosition())))
                                        .then(
                                                Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(context -> placeBunker(context.getSource(), BlockPosArgument.getLoadedBlockPos(context, "pos")))
                                        )
                        )
        );
    }

    public static void onServerStarted(ServerStartedEvent event) {
        ServerLevel level = event.getServer().overworld();
        boolean templatePresent = level.getStructureManager().get(BUNKER_TEMPLATE_ID).isPresent();
        int startPoolSize = getPoolSize(level, BUNKER_START_POOL);
        boolean structureRegistered = hasStructure(level, BUNKER_STRUCTURE);
        boolean structureSetRegistered = hasStructureSet(level, BUNKER_STRUCTURE_SET);
        int taggedBiomeCount = getTaggedBiomeCount(level, BUNKER_BIOME_TAG);
        LOGGER.info("Manhattan bunker template present at data/obesecat/structure/...: {}", templatePresent);
        LOGGER.info("Manhattan bunker start pool registered: {} (elements={})", startPoolSize >= 0, startPoolSize);
        LOGGER.info("Manhattan bunker structure registered: {}", structureRegistered);
        LOGGER.info("Manhattan bunker structure set registered: {}", structureSetRegistered);
        LOGGER.info("Manhattan bunker biome tag loaded: {} (biomes={})", taggedBiomeCount >= 0, taggedBiomeCount);
        LOGGER.info("Manhattan bunker generation settings loaded: spacing=24, separation=8, step=surface_structures, beacon=disabled");
    }

    private static int placeBunker(CommandSourceStack source, BlockPos pos) throws CommandSyntaxException {
        ServerLevel level = source.getLevel();
        StructureTemplate template = level.getStructureManager().get(BUNKER_TEMPLATE_ID)
                .orElseThrow(ERROR_TEMPLATE_MISSING::create);

        BlockPos end = pos.offset(template.getSize());
        if (!isAreaLoaded(level, pos, end)) {
            throw BlockPosArgument.ERROR_NOT_LOADED.create();
        }

        boolean placed = template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.getRandom(), 2);
        if (!placed) {
            throw ERROR_TEMPLATE_PLACE_FAILED.create();
        }

        var size = template.getSize();
        BoundingBox box = new BoundingBox(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + size.getX() - 1, pos.getY() + size.getY() - 1, pos.getZ() + size.getZ() - 1
        );
        int replaced = ManhattanBunkerWorkstation.replaceLecterns(level, box);
        int cleaned = ManhattanBunkerWorkstation.cleanInteriorFloor(level, box);

        source.sendSuccess(() -> Component.literal("Placed Manhattan bunker template at " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + ", converted " + replaced + " lectern(s), and cleaned " + cleaned + " floor block(s)"), true);
        LOGGER.info("Placed Manhattan bunker template at {}, converted {} lectern(s), and cleaned {} floor block(s)", pos, replaced, cleaned);
        return 1;
    }

    private static boolean isAreaLoaded(ServerLevel level, BlockPos start, BlockPos end) {
        ChunkPos chunkStart = new ChunkPos(start);
        ChunkPos chunkEnd = new ChunkPos(end);
        return ChunkPos.rangeClosed(chunkStart, chunkEnd)
                .noneMatch(chunkPos -> !level.isLoaded(chunkPos.getWorldPosition()));
    }

    private static int getPoolSize(ServerLevel level, ResourceKey<StructureTemplatePool> poolKey) {
        try {
            Registry<StructureTemplatePool> registry = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);
            Holder.Reference<StructureTemplatePool> holder = registry.getHolderOrThrow(poolKey);
            return holder.value().size();
        } catch (Exception exception) {
            LOGGER.warn("Unable to inspect village pool {}", poolKey.location(), exception);
            return -1;
        }
    }

    private static boolean hasStructure(ServerLevel level, ResourceKey<Structure> structureKey) {
        try {
            Registry<Structure> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
            return registry.getHolder(structureKey).isPresent();
        } catch (Exception exception) {
            LOGGER.warn("Unable to inspect structure {}", structureKey.location(), exception);
            return false;
        }
    }

    private static boolean hasStructureSet(ServerLevel level, ResourceKey<StructureSet> structureSetKey) {
        try {
            Registry<StructureSet> registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
            return registry.getHolder(structureSetKey).isPresent();
        } catch (Exception exception) {
            LOGGER.warn("Unable to inspect structure set {}", structureSetKey.location(), exception);
            return false;
        }
    }

    private static int getTaggedBiomeCount(ServerLevel level, TagKey<Biome> biomeTag) {
        try {
            Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
            HolderSet.Named<Biome> holderSet = registry.getTag(biomeTag).orElse(null);
            return holderSet == null ? -1 : holderSet.size();
        } catch (Exception exception) {
            LOGGER.warn("Unable to inspect biome tag {}", biomeTag.location(), exception);
            return -1;
        }
    }

    private ManhattanBunkerDebug() {
    }
}
