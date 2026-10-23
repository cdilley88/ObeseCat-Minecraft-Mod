package com.fende.obesecat.counterstrike.block.entity;

import com.fende.obesecat.counterstrike.block.ConstructionYardCoreBlock;
import com.fende.obesecat.counterstrike.block.PneumaticDoorBlock;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.menu.ConstructionYardMenu;
import com.fende.obesecat.counterstrike.power.BasePowerGrid;
import com.fende.obesecat.counterstrike.power.BasePowerManager;
import com.fende.obesecat.counterstrike.registry.CSBlockEntities;
import com.fende.obesecat.counterstrike.registry.CSBlocks;
import com.fende.obesecat.counterstrike.registry.CSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import com.fende.obesecat.counterstrike.resource.ResourceValues;
import java.util.Set;
import java.util.UUID;

public class ConstructionYardCoreBlockEntity extends BlockEntity implements IDeployedCore, MenuProvider {
    private boolean isDisassembling = false;
    private @Nullable UUID owner = null;

    // Interior Resource Silo Tanks
    private int ironBuffer = 0;
    private int goldBuffer = 0;
    private int diamondBuffer = 0;
    private int trinititeBuffer = 0;
    private int tabbyriumBuffer = 0;

    // 6-Slot Inventory: Slots 0-4 Dedicated Hoppers (Iron, Gold, Diamond, Trinitite, Tabbyrium), Slot 5 Output Crate Bay
    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private @Nullable String currentRecipe = null;
    private int progress = 0;
    private int maxProgress = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> getBlockState().hasProperty(ConstructionYardCoreBlock.FACING)
                        ? getBlockState().getValue(ConstructionYardCoreBlock.FACING).get2DDataValue()
                        : 0;
                case 1 -> progress;
                case 2 -> maxProgress;
                case 3 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).getGenerated();
                    }
                    yield 0;
                }
                case 4 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).getConsumed();
                    }
                    yield 0;
                }
                case 5 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).isLowPower() ? 1 : 0;
                    }
                    yield 0;
                }
                case 6 -> currentRecipe != null ? 1 : 0;
                case 7 -> ironBuffer;
                case 8 -> goldBuffer;
                case 9 -> diamondBuffer;
                case 10 -> trinititeBuffer;
                case 11 -> tabbyriumBuffer;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 1 -> progress = value;
                case 2 -> maxProgress = value;
                case 7 -> ironBuffer = value;
                case 8 -> goldBuffer = value;
                case 9 -> diamondBuffer = value;
                case 10 -> trinititeBuffer = value;
                case 11 -> tabbyriumBuffer = value;
            }
        }

        @Override
        public int getCount() {
            return 12;
        }
    };

    public ConstructionYardCoreBlockEntity(BlockPos pos, BlockState state) {
        super(CSBlockEntities.CONSTRUCTION_YARD_CORE.get(), pos, state);
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public static BlockPos getRelativePos(BlockPos origin, Direction facing, int u, int v, int w) {
        Direction inward = facing.getOpposite();
        Direction right = facing.getClockWise();
        int dx = right.getStepX() * u + inward.getStepX() * w;
        int dy = v - 1; // v=0 is ground floor (origin.below()), v=1 is originPos
        int dz = right.getStepZ() * u + inward.getStepZ() * w;
        return origin.offset(dx, dy, dz);
    }

    public static BlockPos getOriginPos(BlockPos corePos, Direction facing) {
        return corePos.relative(facing, 4);
    }

    public static void buildStructure(Level level, BlockPos origin, Direction facing, BlockPos corePos) {
        buildStructure(level, origin, facing, corePos, null);
    }

    public static void buildStructure(Level level, BlockPos origin, Direction facing, BlockPos corePos, @Nullable UUID owner) {
        // 1. Floor at v = 0 (u: -2..2, w: 0..5) - replaces top layer of ground
        for (int u = -2; u <= 2; u++) {
            for (int w = 0; w < 6; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 0, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 2. Roof at v = 4 (u: -2..2, w: 0..5)
        for (int u = -2; u <= 2; u++) {
            for (int w = 0; w < 6; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 4, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 3. Left Wall at u = -2 (v: 1..3, w: 0..5)
        for (int v = 1; v <= 3; v++) {
            for (int w = 0; w < 6; w++) {
                BlockPos target = getRelativePos(origin, facing, -2, v, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 4. Right Wall at u = 2 (v: 1..3, w: 0..5)
        for (int v = 1; v <= 3; v++) {
            for (int w = 0; w < 6; w++) {
                BlockPos target = getRelativePos(origin, facing, 2, v, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 5. Back Wall at w = 5 (u: -1..1, v: 1..3)
        for (int v = 1; v <= 3; v++) {
            for (int u = -1; u <= 1; u++) {
                BlockPos target = getRelativePos(origin, facing, u, v, 5);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 6. Front Wall at w = 0 (u: -1..1, v: 1..3)
        for (int v = 1; v <= 3; v++) {
            for (int u : new int[]{-1, 1}) {
                BlockPos target = getRelativePos(origin, facing, u, v, 0);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }
        // Door lintel at u = 0, v = 3, w = 0
        BlockPos lintelPos = getRelativePos(origin, facing, 0, 3, 0);
        level.setBlock(lintelPos, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
        if (level.getBlockEntity(lintelPos) instanceof ConstructionYardHullBlockEntity hull) {
            hull.setCorePos(corePos);
        }

        // Pneumatic Door at u = 0, w = 0 (v = 1 LOWER, v = 2 UPPER)
        BlockPos doorLowerPos = getRelativePos(origin, facing, 0, 1, 0);
        BlockPos doorUpperPos = getRelativePos(origin, facing, 0, 2, 0);

        BlockState doorLowerState = CSBlocks.PNEUMATIC_DOOR.get().defaultBlockState()
                .setValue(PneumaticDoorBlock.FACING, facing)
                .setValue(PneumaticDoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(PneumaticDoorBlock.OPEN, false);
        BlockState doorUpperState = CSBlocks.PNEUMATIC_DOOR.get().defaultBlockState()
                .setValue(PneumaticDoorBlock.FACING, facing)
                .setValue(PneumaticDoorBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(PneumaticDoorBlock.OPEN, false);

        level.setBlock(doorLowerPos, doorLowerState, 3);
        level.setBlock(doorUpperPos, doorUpperState, 3);

        if (level.getBlockEntity(doorLowerPos) instanceof PneumaticDoorBlockEntity doorLower) {
            doorLower.setCorePos(corePos);
        }
        if (level.getBlockEntity(doorUpperPos) instanceof PneumaticDoorBlockEntity doorUpper) {
            doorUpper.setCorePos(corePos);
        }

        // 7. Interior room (u: -1..1, v: 1..3, w: 1..4)
        for (int v = 1; v <= 3; v++) {
            for (int u = -1; u <= 1; u++) {
                for (int w = 1; w <= 4; w++) {
                    BlockPos target = getRelativePos(origin, facing, u, v, w);
                    if (u == 0 && v == 1 && w == 4) {
                        BlockState coreState = CSBlocks.CONSTRUCTION_YARD_CORE.get().defaultBlockState()
                                .setValue(ConstructionYardCoreBlock.FACING, facing);
                        level.setBlock(target, coreState, 3);
                        if (level.getBlockEntity(target) instanceof ConstructionYardCoreBlockEntity coreBE) {
                            if (owner != null) {
                                coreBE.setOwner(owner);
                            }
                        }
                    } else {
                        BlockState existing = level.getBlockState(target);
                        if (!existing.isAir()) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    public int getIronBuffer() { return ironBuffer; }
    public void setIronBuffer(int ironBuffer) { this.ironBuffer = ironBuffer; setChanged(); }

    public int getGoldBuffer() { return goldBuffer; }
    public void setGoldBuffer(int goldBuffer) { this.goldBuffer = goldBuffer; setChanged(); }

    public int getDiamondBuffer() { return diamondBuffer; }
    public void setDiamondBuffer(int diamondBuffer) { this.diamondBuffer = diamondBuffer; setChanged(); }

    public int getTrinititeBuffer() { return trinititeBuffer; }
    public void setTrinititeBuffer(int trinititeBuffer) { this.trinititeBuffer = trinititeBuffer; setChanged(); }

    public int getTabbyriumBuffer() { return tabbyriumBuffer; }
    public void setTabbyriumBuffer(int tabbyriumBuffer) { this.tabbyriumBuffer = tabbyriumBuffer; setChanged(); }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ConstructionYardCoreBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Maintain consumer registration on BasePowerManager (-25 GW)
        if (blockEntity.owner != null) {
            BasePowerManager.get(serverLevel).registerConsumer(pos, blockEntity.owner, BasePowerManager.CONSTRUCTION_YARD_DRAIN);
        }

        // Digest items in 5 dedicated permanent hoppers into interior resource silo tanks
        digestHoppers(serverLevel, pos, blockEntity);

        if (blockEntity.currentRecipe != null) {
            BasePowerGrid grid = blockEntity.owner != null
                    ? BasePowerManager.get(serverLevel).getGrid(blockEntity.owner)
                    : null;
            boolean lowPower = grid != null && grid.isLowPower();

            // In Low Power, fabrication speed is penalized 50%
            if (!lowPower || serverLevel.getGameTime() % 2 == 0) {
                blockEntity.progress++;
                blockEntity.setChanged();
            }

            // Assembly sparks
            if (blockEntity.progress % 12 == 0) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                        2, 0.15, 0.15, 0.15, 0.05);
            }

            if (blockEntity.progress >= blockEntity.maxProgress) {
                ItemStack result = ItemStack.EMPTY;
                if ("fission_turbine".equals(blockEntity.currentRecipe)) {
                    result = new ItemStack(CSItems.FISSION_TURBINE_CRATE.get());
                } else if ("barracks".equals(blockEntity.currentRecipe)) {
                    result = new ItemStack(CSItems.BARRACKS_CRATE.get());
                }

                if (!result.isEmpty()) {
                    ItemStack leftover = blockEntity.inventory.insertItem(5, result, false);
                    if (!leftover.isEmpty()) {
                        ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, leftover);
                        level.addFreshEntity(drop);
                    }
                }

                blockEntity.currentRecipe = null;
                blockEntity.progress = 0;
                blockEntity.maxProgress = 0;
                blockEntity.setChanged();

                serverLevel.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9F, 1.2F);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
                        15, 0.8, 0.5, 0.8, 0.05);
            }
        }
    }

    public static void digestHoppers(Level level, BlockPos pos, ConstructionYardCoreBlockEntity blockEntity) {
        boolean changed = false;

        // Slot 0: Iron Hopper
        ItemStack ironStack = blockEntity.inventory.getStackInSlot(0);
        if (!ironStack.isEmpty() && ResourceValues.matchesType(ironStack, ResourceValues.ResourceType.IRON)) {
            int val = ResourceValues.getUnitValue(ironStack);
            if (val > 0 && blockEntity.ironBuffer + val <= ResourceValues.MAX_TANK_CAPACITY) {
                int canTake = (ResourceValues.MAX_TANK_CAPACITY - blockEntity.ironBuffer) / val;
                int take = Math.min(Math.min(ironStack.getCount(), canTake), 4);
                if (take > 0) {
                    blockEntity.ironBuffer += take * val;
                    ironStack.shrink(take);
                    changed = true;
                }
            }
        }

        // Slot 1: Gold Hopper
        ItemStack goldStack = blockEntity.inventory.getStackInSlot(1);
        if (!goldStack.isEmpty() && ResourceValues.matchesType(goldStack, ResourceValues.ResourceType.GOLD)) {
            int val = ResourceValues.getUnitValue(goldStack);
            if (val > 0 && blockEntity.goldBuffer + val <= ResourceValues.MAX_TANK_CAPACITY) {
                int canTake = (ResourceValues.MAX_TANK_CAPACITY - blockEntity.goldBuffer) / val;
                int take = Math.min(Math.min(goldStack.getCount(), canTake), 4);
                if (take > 0) {
                    blockEntity.goldBuffer += take * val;
                    goldStack.shrink(take);
                    changed = true;
                }
            }
        }

        // Slot 2: Diamond Hopper
        ItemStack diamondStack = blockEntity.inventory.getStackInSlot(2);
        if (!diamondStack.isEmpty() && ResourceValues.matchesType(diamondStack, ResourceValues.ResourceType.DIAMOND)) {
            int val = ResourceValues.getUnitValue(diamondStack);
            if (val > 0 && blockEntity.diamondBuffer + val <= ResourceValues.MAX_TANK_CAPACITY) {
                int canTake = (ResourceValues.MAX_TANK_CAPACITY - blockEntity.diamondBuffer) / val;
                int take = Math.min(Math.min(diamondStack.getCount(), canTake), 4);
                if (take > 0) {
                    blockEntity.diamondBuffer += take * val;
                    diamondStack.shrink(take);
                    changed = true;
                }
            }
        }

        // Slot 3: Trinitite Hopper
        ItemStack trinititeStack = blockEntity.inventory.getStackInSlot(3);
        if (!trinititeStack.isEmpty() && ResourceValues.matchesType(trinititeStack, ResourceValues.ResourceType.TRINITITE)) {
            int val = ResourceValues.getUnitValue(trinititeStack);
            if (val > 0 && blockEntity.trinititeBuffer + val <= ResourceValues.MAX_TANK_CAPACITY) {
                int canTake = (ResourceValues.MAX_TANK_CAPACITY - blockEntity.trinititeBuffer) / val;
                int take = Math.min(Math.min(trinititeStack.getCount(), canTake), 4);
                if (take > 0) {
                    blockEntity.trinititeBuffer += take * val;
                    trinititeStack.shrink(take);
                    changed = true;
                }
            }
        }

        // Slot 4: Tabbyrium Hopper
        ItemStack tabbyriumStack = blockEntity.inventory.getStackInSlot(4);
        if (!tabbyriumStack.isEmpty() && ResourceValues.matchesType(tabbyriumStack, ResourceValues.ResourceType.TABBYRIUM)) {
            int val = ResourceValues.getUnitValue(tabbyriumStack);
            if (val > 0 && blockEntity.tabbyriumBuffer + val <= ResourceValues.MAX_TANK_CAPACITY) {
                int canTake = (ResourceValues.MAX_TANK_CAPACITY - blockEntity.tabbyriumBuffer) / val;
                int take = Math.min(Math.min(tabbyriumStack.getCount(), canTake), 4);
                if (take > 0) {
                    blockEntity.tabbyriumBuffer += take * val;
                    tabbyriumStack.shrink(take);
                    changed = true;
                }
            }
        }

        if (changed) {
            blockEntity.setChanged();
            if (level.getGameTime() % 6 == 0) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.4F);
            }
        }
    }

    public void depositPlayerInventory(Player player) {
        if (level == null || level.isClientSide) return;

        boolean depositedAny = false;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            ResourceValues.ResourceType type = ResourceValues.getResourceType(stack);
            if (type == null) continue;

            int unitVal = ResourceValues.getUnitValue(stack);
            if (unitVal <= 0) continue;

            int currentBuffer = switch (type) {
                case IRON -> ironBuffer;
                case GOLD -> goldBuffer;
                case DIAMOND -> diamondBuffer;
                case TRINITITE -> trinititeBuffer;
                case TABBYRIUM -> tabbyriumBuffer;
            };

            int space = ResourceValues.MAX_TANK_CAPACITY - currentBuffer;
            if (space >= unitVal) {
                int canFit = space / unitVal;
                int take = Math.min(stack.getCount(), canFit);
                if (take > 0) {
                    int addedUnits = take * unitVal;
                    switch (type) {
                        case IRON -> ironBuffer += addedUnits;
                        case GOLD -> goldBuffer += addedUnits;
                        case DIAMOND -> diamondBuffer += addedUnits;
                        case TRINITITE -> trinititeBuffer += addedUnits;
                        case TABBYRIUM -> tabbyriumBuffer += addedUnits;
                    }
                    stack.shrink(take);
                    depositedAny = true;
                }
            }
        }

        if (depositedAny) {
            setChanged();
            level.playSound(null, getBlockPos(), SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SoundSource.BLOCKS, 0.8F, 1.2F);
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fEligible minerals successfully deposited into base silos."),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.literal("§e[EVA] §fNo compatible mineral resources found in inventory or silos full."),
                    true
            );
        }
    }

    public boolean startConstruction(String recipeId, Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (currentRecipe != null) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§c[EVA] §fConstruction Yard busy. Structure already in production."),
                        true
                );
            }
            return false;
        }

        if ("fission_turbine".equals(recipeId)) {
            // Check output slot (Slot 5)
            ItemStack outputSlot = inventory.getStackInSlot(5);
            if (!outputSlot.isEmpty() && (!outputSlot.is(CSItems.FISSION_TURBINE_CRATE.get()) || outputSlot.getCount() >= outputSlot.getMaxStackSize())) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[EVA] §fOutput crate bay obstructed! Clear slot before initiating construction."),
                            true
                    );
                }
                return false;
            }

            // Recipe Cost: 36 Iron Units, 16 Tabbyrium Units
            if (ironBuffer < 36 || tabbyriumBuffer < 16) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[EVA] §fInsufficient base resources! Required in Silos: 36 Iron, 16 Tabbyrium."),
                            true
                    );
                }
                level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                return false;
            }

            ironBuffer -= 36;
            tabbyriumBuffer -= 16;
            currentRecipe = "fission_turbine";
            progress = 0;
            maxProgress = 200; // 10 seconds at full power
            setChanged();

            level.playSound(null, getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.3F);
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§e[EVA] §fBuilding: Tabbyrium Fission Turbine..."),
                        true
                );
            }
            return true;
        }

        if ("barracks".equals(recipeId)) {
            ItemStack outputSlot = inventory.getStackInSlot(5);
            if (!outputSlot.isEmpty() && (!outputSlot.is(CSItems.BARRACKS_CRATE.get()) || outputSlot.getCount() >= outputSlot.getMaxStackSize())) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[EVA] §fOutput crate bay obstructed! Clear slot before initiating construction."),
                            true
                    );
                }
                return false;
            }

            // Recipe Cost: 72 Iron Units, 24 Tabbyrium Units, 18 Gold Units
            if (ironBuffer < 72 || tabbyriumBuffer < 24 || goldBuffer < 18) {
                if (player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[EVA] §fInsufficient base resources! Required in Silos: 72 Iron, 24 Tabbyrium, 18 Gold."),
                            true
                    );
                }
                level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                return false;
            }

            ironBuffer -= 72;
            tabbyriumBuffer -= 24;
            goldBuffer -= 18;
            currentRecipe = "barracks";
            progress = 0;
            maxProgress = 240; // 12 seconds
            setChanged();

            level.playSound(null, getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.3F);
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§e[EVA] §fBuilding: Infantry Barracks..."),
                        true
                );
            }
            return true;
        }

        return false;
    }

    public boolean rotateStructure(Level level, BlockPos corePos, Player player) {
        if (isDisassembling || level.isClientSide) {
            return false;
        }

        BlockState coreState = level.getBlockState(corePos);
        if (!coreState.hasProperty(ConstructionYardCoreBlock.FACING)) {
            return false;
        }

        Direction currentFacing = coreState.getValue(ConstructionYardCoreBlock.FACING);
        Direction nextFacing = currentFacing.getClockWise();

        BlockPos oldOrigin = getOriginPos(corePos, currentFacing);
        BlockPos newOrigin = getOriginPos(corePos, nextFacing);

        // Track all old positions
        Set<BlockPos> oldPositions = new HashSet<>();
        for (int v = 0; v <= 4; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 6; w++) {
                    oldPositions.add(getRelativePos(oldOrigin, currentFacing, u, v, w));
                }
            }
        }

        // Clearance check for new footprint
        for (int v = 1; v <= 4; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 6; w++) {
                    BlockPos target = getRelativePos(newOrigin, nextFacing, u, v, w);
                    if (target.equals(corePos) || oldPositions.contains(target)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(target);
                    if (!targetState.isAir() && !targetState.canBeReplaced()) {
                        if (player != null) {
                            player.displayClientMessage(
                                    Component.literal("§c[EVA] §fCannot rotate structure! Sector obstructed at " + target.toShortString()),
                                    true
                            );
                        }
                        level.playSound(null, corePos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                        return false;
                    }
                }
            }
        }

        // Identify new positions
        Set<BlockPos> newPositions = new HashSet<>();
        for (int v = 0; v <= 4; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 6; w++) {
                    newPositions.add(getRelativePos(newOrigin, nextFacing, u, v, w));
                }
            }
        }

        // Remove old blocks that aren't in the new footprint
        for (BlockPos oldPos : oldPositions) {
            if (!newPositions.contains(oldPos)) {
                BlockState bs = level.getBlockState(oldPos);
                if (bs.is(CSBlocks.CONSTRUCTION_YARD_HULL.get()) || bs.is(CSBlocks.PNEUMATIC_DOOR.get())) {
                    if (oldPos.getY() == oldOrigin.getY() - 1) {
                        level.setBlock(oldPos, Blocks.DIRT.defaultBlockState(), 3);
                    } else {
                        level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Preserve state before rebuilding
        UUID savedOwner = this.owner;
        String savedRecipe = this.currentRecipe;
        int savedProgress = this.progress;
        int savedMaxProgress = this.maxProgress;
        int savedIron = this.ironBuffer;
        int savedGold = this.goldBuffer;
        int savedDiamond = this.diamondBuffer;
        int savedTrinitite = this.trinititeBuffer;
        int savedTabbyrium = this.tabbyriumBuffer;
        ItemStack[] savedInventory = new ItemStack[inventory.getSlots()];
        for (int i = 0; i < inventory.getSlots(); i++) {
            savedInventory[i] = inventory.getStackInSlot(i).copy();
        }

        // Rebuild structure in new orientation
        buildStructure(level, newOrigin, nextFacing, corePos, savedOwner);

        // Restore state to core block entity
        if (level.getBlockEntity(corePos) instanceof ConstructionYardCoreBlockEntity newBE) {
            newBE.setOwner(savedOwner);
            newBE.currentRecipe = savedRecipe;
            newBE.progress = savedProgress;
            newBE.maxProgress = savedMaxProgress;
            newBE.ironBuffer = savedIron;
            newBE.goldBuffer = savedGold;
            newBE.diamondBuffer = savedDiamond;
            newBE.trinititeBuffer = savedTrinitite;
            newBE.tabbyriumBuffer = savedTabbyrium;
            for (int i = 0; i < savedInventory.length; i++) {
                newBE.inventory.setStackInSlot(i, savedInventory[i]);
            }
            newBE.setChanged();
        }

        // Sound & feedback
        level.playSound(null, corePos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.playSound(null, corePos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 1.4F);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    corePos.getX() + 0.5, corePos.getY() + 1.0, corePos.getZ() + 0.5,
                    25, 1.5, 0.8, 1.5, 0.05);
        }

        if (player != null) {
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fConstruction Yard re-aligned to face §e" + nextFacing.getName().toUpperCase() + "§f."),
                    true
            );
        }
        return true;
    }

    @Override
    public boolean packUp(Level level, BlockPos corePos, Player player) {
        if (isDisassembling || level.isClientSide) {
            return false;
        }
        isDisassembling = true;

        if (level instanceof ServerLevel sl) {
            BasePowerManager.get(sl).unregisterConsumer(corePos);
        }

        BlockState coreState = level.getBlockState(corePos);
        Direction facing = coreState.hasProperty(ConstructionYardCoreBlock.FACING)
                ? coreState.getValue(ConstructionYardCoreBlock.FACING)
                : Direction.NORTH;

        BlockPos origin = getOriginPos(corePos, facing);

        for (int v = 0; v <= 4; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 6; w++) {
                    BlockPos target = getRelativePos(origin, facing, u, v, w);
                    BlockState targetState = level.getBlockState(target);
                    if (v == 0) {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())) {
                            level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    } else {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())
                                || targetState.is(CSBlocks.CONSTRUCTION_YARD_CORE.get())
                                || targetState.is(CSBlocks.PNEUMATIC_DOOR.get())) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Drop any stored items
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, stack.copy());
                level.addFreshEntity(drop);
            }
        }

        // Refund all resource units stored in silos so player never loses anything
        refundResourceUnits(level, origin);

        ItemStack mcvStack = new ItemStack(CSItems.MEOWBILE_CONSTRUCTION_VEHICLE.get());
        if (player != null && !player.addItem(mcvStack)) {
            ItemEntity drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, mcvStack);
            level.addFreshEntity(drop);
        }

        level.playSound(null, origin, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.9F, 1.2F);
        return true;
    }

    private void refundResourceUnits(Level level, BlockPos origin) {
        // Iron
        dropRefund(level, origin, Items.IRON_BLOCK, ironBuffer / 81);
        dropRefund(level, origin, Items.IRON_INGOT, (ironBuffer % 81) / 9);
        dropRefund(level, origin, Items.IRON_NUGGET, ironBuffer % 9);
        ironBuffer = 0;

        // Gold
        dropRefund(level, origin, Items.GOLD_BLOCK, goldBuffer / 81);
        dropRefund(level, origin, Items.GOLD_INGOT, (goldBuffer % 81) / 9);
        dropRefund(level, origin, Items.GOLD_NUGGET, goldBuffer % 9);
        goldBuffer = 0;

        // Diamond
        dropRefund(level, origin, Items.DIAMOND_BLOCK, diamondBuffer / 81);
        dropRefund(level, origin, Items.DIAMOND, (diamondBuffer % 81) / 9);
        diamondBuffer = 0;

        // Trinitite
        dropRefund(level, origin, BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("obesecat", "trinitite")), trinititeBuffer / 81);
        trinititeBuffer = 0;

        // Tabbyrium
        dropRefund(level, origin, BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("obesecat", "tabbyrium_crystal")), tabbyriumBuffer / 4);
        dropRefund(level, origin, BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("obesecat", "tabbyrium_shard")), tabbyriumBuffer % 4);
        tabbyriumBuffer = 0;
    }

    private void dropRefund(Level level, BlockPos origin, net.minecraft.world.level.ItemLike item, int count) {
        if (count <= 0 || item == null) return;
        int remaining = count;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, 64);
            ItemEntity drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, new ItemStack(item, stackSize));
            level.addFreshEntity(drop);
            remaining -= stackSize;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel sl) {
            BasePowerManager.get(sl).unregisterConsumer(worldPosition);
        }
    }

    @Override
    public String getStructureId() {
        return "meowbile_construction_yard";
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Feline Construction Yard");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ConstructionYardMenu(id, playerInventory, getBlockPos(), data, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        if (currentRecipe != null) {
            tag.putString("CurrentRecipe", currentRecipe);
        }
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
        tag.putInt("IronTank", ironBuffer);
        tag.putInt("GoldTank", goldBuffer);
        tag.putInt("DiamondTank", diamondBuffer);
        tag.putInt("TrinititeTank", trinititeBuffer);
        tag.putInt("TabbyriumTank", tabbyriumBuffer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        }
        if (tag.contains("CurrentRecipe")) {
            currentRecipe = tag.getString("CurrentRecipe");
        } else {
            currentRecipe = null;
        }
        progress = tag.getInt("Progress");
        maxProgress = tag.getInt("MaxProgress");
        ironBuffer = tag.getInt("IronTank");
        goldBuffer = tag.getInt("GoldTank");
        diamondBuffer = tag.getInt("DiamondTank");
        trinititeBuffer = tag.getInt("TrinititeTank");
        tabbyriumBuffer = tag.getInt("TabbyriumTank");
    }
}
