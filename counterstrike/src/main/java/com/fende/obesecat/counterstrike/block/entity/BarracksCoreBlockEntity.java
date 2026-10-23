package com.fende.obesecat.counterstrike.block.entity;

import com.fende.obesecat.counterstrike.block.ArmoryLockerBlock;
import com.fende.obesecat.counterstrike.block.BarracksCoreBlock;
import com.fende.obesecat.counterstrike.block.PneumaticDoorBlock;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.menu.BarracksMenu;
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
import java.util.Set;
import java.util.UUID;

public class BarracksCoreBlockEntity extends BlockEntity implements IDeployedCore, MenuProvider {
    private boolean isDisassembling = false;
    private @Nullable UUID owner = null;

    // Slots 0..3: Hopper Inputs, Slot 4: Completed Weapon Output Bay
    private final ItemStackHandler inventory = new ItemStackHandler(5) {
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
                case 0 -> getBlockState().hasProperty(BarracksCoreBlock.FACING)
                        ? getBlockState().getValue(BarracksCoreBlock.FACING).get2DDataValue()
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
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 1 -> progress = value;
                case 2 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public BarracksCoreBlockEntity(BlockPos pos, BlockState state) {
        super(CSBlockEntities.BARRACKS_CORE.get(), pos, state);
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
        return corePos.relative(facing, 3);
    }

    public static void buildStructure(Level level, BlockPos origin, Direction facing, BlockPos corePos, @Nullable UUID owner) {
        // 1. Floor at v = 0 (u: -2..2, w: 0..4) - 25 hull blocks replacing ground
        for (int u = -2; u <= 2; u++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 0, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 2. Roof at v = 3 (u: -2..2, w: 0..4) - 25 hull blocks
        for (int u = -2; u <= 2; u++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, u, 3, w);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 3. Left Wall at u = -2 (v: 1..2, w: 0..4)
        for (int v = 1; v <= 2; v++) {
            for (int w = 0; w < 5; w++) {
                BlockPos target = getRelativePos(origin, facing, -2, v, w);
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

        // 5. Back Wall at w = 4 (u: -1..1, v: 1..2)
        for (int v = 1; v <= 2; v++) {
            for (int u = -1; u <= 1; u++) {
                BlockPos target = getRelativePos(origin, facing, u, v, 4);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
            }
        }

        // 6. Front Wall at w = 0 (u: -2..-1, 1..2, v: 1..2)
        for (int v = 1; v <= 2; v++) {
            for (int u : new int[]{-2, -1, 1, 2}) {
                BlockPos target = getRelativePos(origin, facing, u, v, 0);
                level.setBlock(target, CSBlocks.CONSTRUCTION_YARD_HULL.get().defaultBlockState(), 3);
                if (level.getBlockEntity(target) instanceof ConstructionYardHullBlockEntity hull) {
                    hull.setCorePos(corePos);
                }
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
        // Clear interior air corridor and entrance
        for (int v = 1; v <= 2; v++) {
            for (int u = -1; u <= 1; u++) {
                for (int w = 1; w <= 3; w++) {
                    BlockPos target = getRelativePos(origin, facing, u, v, w);
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // Left Armory Lockers at u = -1, v = 1, w = 2..3 (facing inward to the right)
        Direction leftLockerFacing = facing.getClockWise();
        for (int w = 2; w <= 3; w++) {
            BlockPos target = getRelativePos(origin, facing, -1, 1, w);
            level.setBlock(target, CSBlocks.ARMORY_LOCKER.get().defaultBlockState().setValue(ArmoryLockerBlock.FACING, leftLockerFacing), 3);
        }

        // Right Armory Lockers at u = 1, v = 1, w = 2..3 (facing inward to the left)
        Direction rightLockerFacing = facing.getCounterClockWise();
        for (int w = 2; w <= 3; w++) {
            BlockPos target = getRelativePos(origin, facing, 1, 1, w);
            level.setBlock(target, CSBlocks.ARMORY_LOCKER.get().defaultBlockState().setValue(ArmoryLockerBlock.FACING, rightLockerFacing), 3);
        }

        // Central Master Procurement Console at u = 0, v = 1, w = 3
        BlockState coreState = CSBlocks.BARRACKS_CORE.get().defaultBlockState()
                .setValue(BarracksCoreBlock.FACING, facing);
        level.setBlock(corePos, coreState, 3);
        if (level.getBlockEntity(corePos) instanceof BarracksCoreBlockEntity coreBE) {
            if (owner != null) {
                coreBE.setOwner(owner);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BarracksCoreBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Maintain consumer registration on BasePowerManager (-20 GW)
        if (blockEntity.owner != null) {
            BasePowerManager.get(serverLevel).registerConsumer(pos, blockEntity.owner, BasePowerManager.BARRACKS_DRAIN);
        }

        if (blockEntity.currentRecipe != null) {
            BasePowerGrid grid = blockEntity.owner != null
                    ? BasePowerManager.get(serverLevel).getGrid(blockEntity.owner)
                    : null;
            boolean lowPower = grid != null && grid.isLowPower();

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
                ItemStack result = switch (blockEntity.currentRecipe) {
                    case "automatic_rifle" -> new ItemStack(CSItems.AUTOMATIC_RIFLE.get());
                    case "rocket_launcher" -> new ItemStack(CSItems.ROCKET_LAUNCHER.get());
                    case "frag_grenade" -> new ItemStack(CSItems.FRAG_GRENADE.get(), 4);
                    case "flamethrower" -> new ItemStack(CSItems.FLAMETHROWER.get());
                    case "commando_rifle" -> new ItemStack(CSItems.COMMANDO_RIFLE.get());
                    default -> ItemStack.EMPTY;
                };

                if (!result.isEmpty()) {
                    ItemStack leftover = blockEntity.inventory.insertItem(4, result, false);
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

    public boolean startConstruction(String recipeId, Player player) {
        if (level == null || level.isClientSide) {
            return false;
        }

        if (currentRecipe != null) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§c[EVA] §fArmory busy. Weapon currently in fabrication."),
                        true
                );
            }
            return false;
        }

        // Recipe costs: (Tabbyrium, Iron, Redstone, Aux/Gunpowder/TNT)
        int tabbyReq = 0, ironReq = 0, redstoneReq = 0, auxReq = 0;
        int duration = 160;

        switch (recipeId) {
            case "automatic_rifle" -> {
                tabbyReq = 2; ironReq = 4; redstoneReq = 1; duration = 160;
            }
            case "rocket_launcher" -> {
                tabbyReq = 4; ironReq = 6; redstoneReq = 2; auxReq = 2; duration = 200;
            }
            case "frag_grenade" -> {
                tabbyReq = 1; ironReq = 2; auxReq = 2; duration = 120;
            }
            case "flamethrower" -> {
                tabbyReq = 3; ironReq = 4; auxReq = 2; duration = 180;
            }
            case "commando_rifle" -> {
                tabbyReq = 6; ironReq = 8; redstoneReq = 4; auxReq = 2; duration = 300;
            }
            default -> {
                return false;
            }
        }

        // Check output slot
        ItemStack outputSlot = inventory.getStackInSlot(4);
        if (!outputSlot.isEmpty() && outputSlot.getCount() >= outputSlot.getMaxStackSize()) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§c[EVA] §fWeapon Output Bay obstructed! Clear slot before initiating fabrication."),
                        true
                );
            }
            return false;
        }

        if (!hasIngredients(tabbyReq, ironReq, redstoneReq, auxReq)) {
            if (player != null) {
                player.displayClientMessage(
                        Component.literal("§c[EVA] §fInsufficient resources! Check required Tabbyrium, Iron, Redstone, and Munitions."),
                        true
                );
            }
            level.playSound(null, getBlockPos(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
            return false;
        }

        consumeIngredients(tabbyReq, ironReq, redstoneReq, auxReq);
        currentRecipe = recipeId;
        progress = 0;
        maxProgress = duration;
        setChanged();

        level.playSound(null, getBlockPos(), SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.4F);
        if (player != null) {
            player.displayClientMessage(
                    Component.literal("§e[EVA] §fFabricating: " + formatRecipeName(recipeId) + "..."),
                    true
            );
        }
        return true;
    }

    private String formatRecipeName(String id) {
        return switch (id) {
            case "automatic_rifle" -> "Automatic Rifle";
            case "rocket_launcher" -> "Rocket Launcher";
            case "frag_grenade" -> "Frag Grenades (x4)";
            case "flamethrower" -> "Flamethrower";
            case "commando_rifle" -> "Commando Assault Rifle";
            default -> id;
        };
    }

    private boolean hasIngredients(int tabbyriumReq, int ironReq, int redstoneReq, int auxReq) {
        int tabbyCount = 0, ironCount = 0, redstoneCount = 0, auxCount = 0;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id.getNamespace().equals("obesecat")) {
                if (id.getPath().equals("tabbyrium_shard")) {
                    tabbyCount += stack.getCount();
                } else if (id.getPath().equals("tabbyrium_crystal")) {
                    tabbyCount += stack.getCount() * 4;
                } else if (id.getPath().equals("trinitite")) {
                    auxCount += stack.getCount() * 2;
                }
            } else if (stack.is(Items.IRON_INGOT)) {
                ironCount += stack.getCount();
            } else if (stack.is(Items.IRON_BLOCK)) {
                ironCount += stack.getCount() * 9;
            } else if (stack.is(Items.REDSTONE)) {
                redstoneCount += stack.getCount();
            } else if (stack.is(Items.REDSTONE_BLOCK)) {
                redstoneCount += stack.getCount() * 9;
            } else if (stack.is(Items.GUNPOWDER) || stack.is(Items.BLAZE_POWDER) || stack.is(Items.TNT)) {
                auxCount += stack.getCount();
            }
        }

        return tabbyCount >= tabbyriumReq && ironCount >= ironReq && redstoneCount >= redstoneReq && auxCount >= auxReq;
    }

    private void consumeIngredients(int tabbyriumReq, int ironReq, int redstoneReq, int auxReq) {
        int remTabby = tabbyriumReq;
        int remIron = ironReq;
        int remRedstone = redstoneReq;
        int remAux = auxReq;

        for (int i = 0; i < 4; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (remTabby > 0 && id.getNamespace().equals("obesecat")) {
                if (id.getPath().equals("tabbyrium_shard")) {
                    int take = Math.min(stack.getCount(), remTabby);
                    stack.shrink(take);
                    remTabby -= take;
                } else if (id.getPath().equals("tabbyrium_crystal") && remTabby >= 4) {
                    stack.shrink(1);
                    remTabby -= 4;
                }
            } else if (remIron > 0 && stack.is(Items.IRON_INGOT)) {
                int take = Math.min(stack.getCount(), remIron);
                stack.shrink(take);
                remIron -= take;
            } else if (remRedstone > 0 && stack.is(Items.REDSTONE)) {
                int take = Math.min(stack.getCount(), remRedstone);
                stack.shrink(take);
                remRedstone -= take;
            } else if (remAux > 0 && (stack.is(Items.GUNPOWDER) || stack.is(Items.BLAZE_POWDER) || stack.is(Items.TNT))) {
                int take = Math.min(stack.getCount(), remAux);
                stack.shrink(take);
                remAux -= take;
            }
        }
        setChanged();
    }

    public boolean rotateStructure(Level level, BlockPos corePos, Player player) {
        if (isDisassembling || level.isClientSide) {
            return false;
        }

        BlockState coreState = level.getBlockState(corePos);
        if (!coreState.hasProperty(BarracksCoreBlock.FACING)) {
            return false;
        }

        Direction currentFacing = coreState.getValue(BarracksCoreBlock.FACING);
        Direction nextFacing = currentFacing.getClockWise();

        BlockPos oldOrigin = getOriginPos(corePos, currentFacing);
        BlockPos newOrigin = getOriginPos(corePos, nextFacing);

        // Track old positions
        Set<BlockPos> oldPositions = new HashSet<>();
        for (int v = 0; v <= 3; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    oldPositions.add(getRelativePos(oldOrigin, currentFacing, u, v, w));
                }
            }
        }

        // Clearance check
        for (int v = 1; v <= 3; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    BlockPos target = getRelativePos(newOrigin, nextFacing, u, v, w);
                    if (target.equals(corePos) || oldPositions.contains(target)) {
                        continue;
                    }
                    BlockState targetState = level.getBlockState(target);
                    if (!targetState.isAir() && !targetState.canBeReplaced()) {
                        if (player != null) {
                            player.displayClientMessage(
                                    Component.literal("§c[EVA] §fCannot rotate Barracks! Obstructed at " + target.toShortString()),
                                    true
                            );
                        }
                        level.playSound(null, corePos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                        return false;
                    }
                }
            }
        }

        // Clear old blocks not in new footprint
        Set<BlockPos> newPositions = new HashSet<>();
        for (int v = 0; v <= 3; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    newPositions.add(getRelativePos(newOrigin, nextFacing, u, v, w));
                }
            }
        }

        for (BlockPos oldPos : oldPositions) {
            if (!newPositions.contains(oldPos)) {
                BlockState bs = level.getBlockState(oldPos);
                if (bs.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())
                        || bs.is(CSBlocks.PNEUMATIC_DOOR.get())
                        || bs.is(CSBlocks.ARMORY_LOCKER.get())) {
                    if (oldPos.getY() == oldOrigin.getY() - 1) {
                        level.setBlock(oldPos, Blocks.DIRT.defaultBlockState(), 3);
                    } else {
                        level.setBlock(oldPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Preserve state
        UUID savedOwner = this.owner;
        String savedRecipe = this.currentRecipe;
        int savedProgress = this.progress;
        int savedMaxProgress = this.maxProgress;
        ItemStack[] savedInventory = new ItemStack[inventory.getSlots()];
        for (int i = 0; i < inventory.getSlots(); i++) {
            savedInventory[i] = inventory.getStackInSlot(i).copy();
        }

        // Rebuild structure in new orientation
        buildStructure(level, newOrigin, nextFacing, corePos, savedOwner);

        if (level.getBlockEntity(corePos) instanceof BarracksCoreBlockEntity newBE) {
            newBE.setOwner(savedOwner);
            newBE.currentRecipe = savedRecipe;
            newBE.progress = savedProgress;
            newBE.maxProgress = savedMaxProgress;
            for (int i = 0; i < savedInventory.length; i++) {
                newBE.inventory.setStackInSlot(i, savedInventory[i]);
            }
            newBE.setChanged();
        }

        level.playSound(null, corePos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.playSound(null, corePos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 1.4F);

        if (player != null) {
            player.displayClientMessage(
                    Component.literal("§a[EVA] §fBarracks re-aligned to face §e" + nextFacing.getName().toUpperCase() + "§f."),
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
        Direction facing = coreState.hasProperty(BarracksCoreBlock.FACING)
                ? coreState.getValue(BarracksCoreBlock.FACING)
                : Direction.NORTH;

        BlockPos origin = getOriginPos(corePos, facing);

        for (int v = 0; v <= 3; v++) {
            for (int u = -2; u <= 2; u++) {
                for (int w = 0; w < 5; w++) {
                    BlockPos target = getRelativePos(origin, facing, u, v, w);
                    BlockState targetState = level.getBlockState(target);
                    if (v == 0) {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())) {
                            level.setBlock(target, Blocks.DIRT.defaultBlockState(), 3);
                        }
                    } else {
                        if (targetState.is(CSBlocks.CONSTRUCTION_YARD_HULL.get())
                                || targetState.is(CSBlocks.BARRACKS_CORE.get())
                                || targetState.is(CSBlocks.ARMORY_LOCKER.get())
                                || targetState.is(CSBlocks.PNEUMATIC_DOOR.get())) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }

        // Drop stored items
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(level, origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5, stack.copy());
                level.addFreshEntity(drop);
            }
        }

        ItemStack crateStack = new ItemStack(CSItems.BARRACKS_CRATE.get());
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
            BasePowerManager.get(sl).unregisterConsumer(worldPosition);
        }
    }

    @Override
    public String getStructureId() {
        return "barracks";
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Infantry Barracks");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BarracksMenu(id, playerInventory, getBlockPos(), data, inventory);
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
    }
}
