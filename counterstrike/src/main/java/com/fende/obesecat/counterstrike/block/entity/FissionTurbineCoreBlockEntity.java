package com.fende.obesecat.counterstrike.block.entity;

import com.fende.obesecat.counterstrike.block.FissionTurbineCoreBlock;
import com.fende.obesecat.counterstrike.block.PneumaticDoorBlock;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.menu.FissionTurbineMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FissionTurbineCoreBlockEntity extends BlockEntity implements IDeployedCore, MenuProvider {
    private boolean isDisassembling = false;
    private @Nullable UUID owner = null;

    private int burnTime = 0;
    private int maxBurnTime = 0;

    private final ItemStackHandler fuelHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return getFuelBurnTime(stack) > 0;
        }
    };

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> maxBurnTime;
                case 2 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).getGenerated();
                    }
                    yield 0;
                }
                case 3 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).getConsumed();
                    }
                    yield 0;
                }
                case 4 -> {
                    if (owner != null && level instanceof ServerLevel sl) {
                        yield BasePowerManager.get(sl).getGrid(owner).isLowPower() ? 1 : 0;
                    }
                    yield 0;
                }
                case 5 -> getBlockState().hasProperty(FissionTurbineCoreBlock.FACING)
                        ? getBlockState().getValue(FissionTurbineCoreBlock.FACING).get2DDataValue()
                        : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> burnTime = value;
                case 1 -> maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public FissionTurbineCoreBlockEntity(BlockPos pos, BlockState state) {
        super(CSBlockEntities.FISSION_TURBINE_CORE.get(), pos, state);
    }

    public void setOwner(@Nullable UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public @Nullable UUID getOwner() {
        return owner;
    }

    public ItemStackHandler getFuelHandler() {
        return fuelHandler;
    }

    public static int getFuelBurnTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id.getNamespace().equals("obesecat")) {
            return switch (id.getPath()) {
                case "tabbyrium_shard" -> 1200;   // 1 minute
                case "tabbyrium_crystal" -> 6000; // 5 minutes
                case "tabbyrium_block" -> 60000;  // 50 minutes
                default -> 0;
            };
        }
        return 0;
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
        return corePos.relative(facing, 3);
    }

    public static void buildStructure(Level level, BlockPos origin, Direction facing, BlockPos corePos, @Nullable UUID owner) {
        // 1. Floor at v = 0 (u: -1..2, w: 0..4) - 20 hull blocks replacing ground
        for (int u = -1; u <= 2; u++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 0, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 2. Roof at v = 3 (u: -1..2, w: 0..4) - 20 hull blocks
        for (int u = -1; u <= 2; u++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 3, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 3. Left Wall at u = -1 (v: 1..2, w: 0..4)
        for (int v = 1; v <= 2; v++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, -1, v, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 4. Right Wall at u = 2 (v: 1..2, w: 0..4)
        for (int v = 1; v <= 2; v++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, 2, v, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 5. Back Wall at w = 4 (u: 0..1, v: 1..2)
        for (int v = 1; v <= 2; v++) {
            for (int u = 0; u <= 1; u++) {
                BlockPos target = getRelativePos(origin, facing, u, v, 4);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 6. Front Wall at w = 0 (u = 1, v = 1..2)
        for (int v = 1; v <= 2; v++) {
            BlockPos target = getRelativePos(origin, facing, 1, v, 0);
            level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
            if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                hull.setCorePos(corePos);
            }
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

        // 7. Interior room:
        // Walkway Corridor at u = 0, w = 1..2, v = 1..2 -> AIR
        for (int v = 1; v <= 2; v++) {
            for (int w = 1; w <= 2; w++) {
                BlockPos target = getRelativePos(origin, facing, 0, v, w);
                level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
            }
        }

        // Reactor Rod column at u = 1, w = 2, v = 1..2
        BlockPos rodLower = getRelativePos(origin, facing, 1, 1, 2);
        BlockPos rodUpper = getRelativePos(origin, facing, 1, 2, 2);
        level.setBlock(rodLower, CSBlocks.TURBINE_REACTOR_CORE.get().defaultBlockState(), 3);
        level.setBlock(rodUpper, CSBlocks.TURBINE_REACTOR_CORE.get().defaultBlockState(), 3);

        // Machinery side casing at u = 1, w = 1, v = 1..2
        for (int v = 1; v <= 2; v++) {
            BlockPos target = getRelativePos(origin, facing, 1, v, 1);
            level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
            if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                hull.setCorePos(corePos);
            }
        }

        // Machinery side casing at u = 1, w = 3, v = 1..2
        for (int v = 1; v <= 2; v++) {
            BlockPos target = getRelativePos(origin, facing, 1, v, 3);
            level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
            if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                hull.setCorePos(corePos);
            }
        }

        // Master Console at u = 0, v = 1, w = 3
        BlockState coreState = CSBlocks.FISSION_TURBINE_CORE.get().defaultBlockState()
                .setValue(FissionTurbineCoreBlock.FACING, facing);
        level.setBlock(corePos, coreState, 3);
        if (level.getBlockEntity(corePos) instanceof FissionTurbineCoreBlockEntity coreBE) {
            if (owner != null) {
                coreBE.setOwner(owner);
            }
        }

        // Space above console: air (v = 2, u = 0, w = 3)
        BlockPos aboveConsole = getRelativePos(origin, facing, 0, 2, 3);
        level.setBlock(aboveConsole, Blocks.AIR.defaultBlockState(), 3);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FissionTurbineCoreBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--;

            // Ensure registered as an active generator
            if (blockEntity.owner != null) {
                BasePowerManager.get(serverLevel).registerGenerator(pos, blockEntity.owner, BasePowerManager.TURBINE_OUTPUT);
            }

            // Exhaust steam particle from roof vent at (u = 1, v = 3, w = 2)
            if (blockEntity.burnTime % 8 == 0 && state.hasProperty(FissionTurbineCoreBlock.FACING)) {
                Direction facing = state.getValue(FissionTurbineCoreBlock.FACING);
                BlockPos origin = getOriginPos(pos, facing);
                BlockPos vent = getRelativePos(origin, facing, 1, 3, 2);
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        vent.getX() + 0.5, vent.getY() + 1.1, vent.getZ() + 0.5,
                        2, 0.1, 0.25, 0.1, 0.02);
            }
        } else {
            // Check for fuel in inventory
            ItemStack fuelStack = blockEntity.fuelHandler.getStackInSlot(0);
            int fuelValue = getFuelBurnTime(fuelStack);
            if (fuelValue > 0) {
                fuelStack.shrink(1);
                blockEntity.burnTime = fuelValue;
                blockEntity.maxBurnTime = fuelValue;
                blockEntity.setChanged();
                if (blockEntity.owner != null) {
                    BasePowerManager.get(serverLevel).registerGenerator(pos, blockEntity.owner, BasePowerManager.TURBINE_OUTPUT);
                }
                serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.6F);
            } else {
                // Out of fuel: unregister generator from BasePowerManager
                BasePowerManager.get(serverLevel).unregisterGenerator(pos);
            }
        }
    }

    @Override
    public boolean rotateStructure(Level level, BlockPos corePos, Player player) {
        if (isDisassembling || level.isClientSide) {
            return false;
        }

        BlockState coreState = level.getBlockState(corePos);
        if (!coreState.hasProperty(FissionTurbineCoreBlock.FACING)) {
            return false;
        }

        Direction currentFacing = coreState.getValue(FissionTurbineCoreBlock.FACING);
        Direction nextFacing = currentFacing.getClockWise();

        BlockPos oldOrigin = getOriginPos(corePos, currentFacing);
        BlockPos newOrigin = getOriginPos(corePos, nextFacing);

        // Collect old positions
        Set<BlockPos> oldPositions = new HashSet<>();
        for (int v = 0; v <= 3; v++) {
            for (int u = -1; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    oldPositions.add(getRelativePos(oldOrigin, currentFacing, u, v, w));
                }
            }
        }

        // Clearance check for new footprint
        for (int v = 1; v <= 3; v++) {
            for (int u = -1; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    BlockPos target = getRelativePos(newOrigin, nextFacing, u, v, w);
                    if (target.equals(corePos) || oldPositions.contains(target)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(target);
                    if (!targetState.isAir() && !targetState.canBeReplaced()) {
                        if (player != null) {
                            player.displayClientMessage(
                                    Component.literal("§c[EVA] §fCannot rotate Fission Turbine! Obstructed at " + target.toShortString()),
                                    true
                            );
                        }
                        level.playSound(null, corePos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                        return false;
                    }
                }
            }
        }

        // Collect new positions
        Set<BlockPos> newPositions = new HashSet<>();
        for (int v = 0; v <= 3; v++) {
            for (int u = -1; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    newPositions.add(getRelativePos(newOrigin, nextFacing, u, v, w));
                }
            }
        }

        // Clear old blocks not in new footprint
        for (BlockPos oldPos : oldPositions) {
            if (!newPositions.contains(oldPos)) {
                BlockState bs = level.getBlockState(oldPos);
                if (bs.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())
                        || bs.is(CSBlocks.PNEUMATIC_DOOR.get())
                        || bs.is(CSBlocks.TURBINE_REACTOR_CORE.get())) {
                    if (oldPos.getY() == oldOrigin.getY() - 1) {
                        level.setBlock(oldPos, Blocks.DIRT.defaultBlockState(), 3);
                    } else {
                        level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Build structure in new orientation
        buildStructure(level, newOrigin, nextFacing, corePos, owner);

        level.playSound(null, corePos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.playSound(null, corePos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 1.4F);

        if (player != null) {
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fFission Turbine re-aligned to face §e" + nextFacing.getName().toUpperCase() + "§f."),
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
            BasePowerManager.get(sl).unregisterGenerator(corePos);
        }

        BlockState coreState = level.getBlockState(corePos);
        Direction facing = coreState.hasProperty(FissionTurbineCoreBlock.FACING)
                ? coreState.getValue(FissionTurbineCoreBlock.FACING)
                : Direction.NORTH;

        BlockPos origin = getOriginPos(corePos, facing);

        for (int v = 0; v <= 3; v++) {
            for (int u = -1; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    BlockPos target = getRelativePos(origin, facing, u, v, w);
                    BlockState targetState = level.getBlockState(target);
                    if (v == 0) {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())) {
                            level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    } else {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())
                                || targetState.is(CSBlocks.FISSION_TURBINE_CORE.get())
                                || targetState.is(CSBlocks.TURBINE_REACTOR_CORE.get())
                                || targetState.is(CSBlocks.PNEUMATIC_DOOR.get())) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        ItemStack crateStack = new ItemStack(CSItems.FISSION_TURBINE_CRATE.get());
        if (player != null && !player.addItem(crateStack)) {
            ItemEntity drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, crateStack);
            level.addFreshEntity(drop);
        }

        level.playSound(null, origin, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.9F, 1.2F);
        return true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel sl) {
            BasePowerManager.get(sl).unregisterGenerator(worldPosition);
        }
    }

    @Override
    public String getStructureId() {
        return "fission_turbine";
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Tabbyrium Fission Turbine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FissionTurbineMenu(id, inventory, getBlockPos(), data, fuelHandler);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Fuel", fuelHandler.serializeNBT(registries));
        tag.putInt("BurnTime", burnTime);
        tag.putInt("MaxBurnTime", maxBurnTime);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Fuel")) {
            fuelHandler.deserializeNBT(registries, tag.getCompound("Fuel"));
        }
        burnTime = tag.getInt("BurnTime");
        maxBurnTime = tag.getInt("MaxBurnTime");
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        }
    }
}
