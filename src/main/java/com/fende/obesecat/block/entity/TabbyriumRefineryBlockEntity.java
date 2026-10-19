package com.fende.obesecat.block.entity;

import com.fende.obesecat.energy.TabbyriumEnergy;
import com.fende.obesecat.inventory.TabbyriumRefineryMenu;
import com.fende.obesecat.registry.ModBlockEntities;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class TabbyriumRefineryBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CAPACITY = TabbyriumEnergy.REFINERY_CAPACITY;
    public static final int MAX_OUTPUT_PER_TICK = TabbyriumEnergy.REFINERY_MAX_OUTPUT;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final boolean[] itemInputSides = new boolean[6];
    private final boolean[] powerOutputSides = new boolean[6];
    private final RefineryEnergyStorage energy = new RefineryEnergyStorage();
    private final RefineryItemHandler itemHandler = new RefineryItemHandler();
    private int progress;
    private int currentFuelProcessTime = TabbyriumEnergy.SHARD_PROCESS_TIME;

    public final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> CAPACITY;
                case 2 -> progress;
                case 3 -> currentFuelProcessTime;
                default -> {
                    if (index >= 4 && index < 10) yield itemInputSides[index - 4] ? 1 : 0;
                    if (index >= 10 && index < 16) yield powerOutputSides[index - 10] ? 1 : 0;
                    yield 0;
                }
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) energy.setEnergy(value);
            else if (index == 2) progress = value;
            else if (index == 3) currentFuelProcessTime = value;
            else if (index >= 4 && index < 10) itemInputSides[index - 4] = value != 0;
            else if (index >= 10 && index < 16) powerOutputSides[index - 10] = value != 0;
        }

        @Override
        public int getCount() {
            return 16;
        }
    };

    public TabbyriumRefineryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TABBYRIUM_REFINERY.get(), pos, state);
        Arrays.fill(powerOutputSides, true);
        itemInputSides[Direction.UP.ordinal()] = true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TabbyriumRefineryBlockEntity refinery) {
        refinery.processFuel();
        refinery.pushEnergy(level, pos);
    }

    private void processFuel() {
        ItemStack fuel = items.getFirst();
        int produced = TabbyriumEnergy.getFuelEnergy(fuel);
        int fuelTime = TabbyriumEnergy.getProcessTime(fuel);
        if (produced <= 0 || energy.getEnergyStored() + produced > CAPACITY) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        this.currentFuelProcessTime = fuelTime;
        progress++;
        if (progress >= currentFuelProcessTime) {
            fuel.shrink(1);
            energy.addEnergy(produced);
            progress = 0;
        }
        setChanged();
    }

    private void pushEnergy(Level level, BlockPos pos) {
        int remainingOutput = Math.min(MAX_OUTPUT_PER_TICK, energy.getEnergyStored());
        for (Direction direction : Direction.values()) {
            if (remainingOutput <= 0) break;
            if (!powerOutputSides[direction.ordinal()]) continue;

            IEnergyStorage neighbor = level.getCapability(
                    Capabilities.EnergyStorage.BLOCK,
                    pos.relative(direction),
                    direction.getOpposite()
            );
            if (neighbor == null || !neighbor.canReceive()) continue;
            int accepted = neighbor.receiveEnergy(remainingOutput, true);
            if (accepted <= 0) continue;
            int extracted = energy.extractEnergy(accepted, false);
            neighbor.receiveEnergy(extracted, false);
            remainingOutput -= extracted;
        }
    }

    public void toggleItemInput(int directionIndex) {
        if (directionIndex < 0 || directionIndex >= itemInputSides.length) return;
        itemInputSides[directionIndex] = !itemInputSides[directionIndex];
        setChanged();
        if (level != null) level.invalidateCapabilities(worldPosition);
    }

    public void togglePowerOutput(int directionIndex) {
        if (directionIndex < 0 || directionIndex >= powerOutputSides.length) return;
        powerOutputSides[directionIndex] = !powerOutputSides[directionIndex];
        setChanged();
        if (level != null) level.invalidateCapabilities(worldPosition);
    }

    @Nullable
    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return direction == null || powerOutputSides[direction.ordinal()] ? energy : null;
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction direction) {
        return direction == null || itemInputSides[direction.ordinal()] ? itemHandler : null;
    }

    public void dropContents(Level level, BlockPos pos) {
        net.minecraft.world.Containers.dropContents(level, pos, new SimpleContainer(items.toArray(ItemStack[]::new)));
        clearContent();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putInt("ProcessTime", currentFuelProcessTime);
        for (int i = 0; i < 6; i++) {
            tag.putBoolean("ItemInputSide" + i, itemInputSides[i]);
            tag.putBoolean("PowerOutputSide" + i, powerOutputSides[i]);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.setEnergy(Math.max(0, Math.min(CAPACITY, tag.getInt("Energy"))));
        progress = Math.max(0, tag.getInt("Progress"));
        currentFuelProcessTime = Math.max(1, tag.getInt("ProcessTime"));
        for (int i = 0; i < 6; i++) {
            itemInputSides[i] = tag.contains("ItemInputSide" + i) && tag.getBoolean("ItemInputSide" + i);
            powerOutputSides[i] = !tag.contains("PowerOutputSide" + i) || tag.getBoolean("PowerOutputSide" + i);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.obesecat.tabbyrium_refinery");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TabbyriumRefineryMenu(id, inventory, this, data, this);
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.getFirst().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize(stack));
        setChanged();
    }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && TabbyriumEnergy.getFuelEnergy(stack) > 0;
    }
    @Override public void clearContent() { items.clear(); setChanged(); }

    private final class RefineryEnergyStorage implements IEnergyStorage {
        private int stored;
        public void setEnergy(int value) { stored = Math.max(0, Math.min(CAPACITY, value)); setChanged(); }
        public void addEnergy(int value) { stored = Math.max(0, Math.min(CAPACITY, stored + value)); setChanged(); }

        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(Math.max(maxExtract, 0), Math.min(stored, MAX_OUTPUT_PER_TICK));
            if (!simulate && extracted > 0) {
                stored -= extracted;
                setChanged();
            }
            return extracted;
        }
        @Override public int getEnergyStored() { return stored; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
    }

    private final class RefineryItemHandler implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return getItem(slot); }
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || stack.isEmpty() || !canPlaceItem(slot, stack)) return stack;
            ItemStack current = getItem(slot);
            if (current.isEmpty()) {
                int accepted = Math.min(stack.getCount(), stack.getMaxStackSize());
                if (!simulate) {
                    ItemStack inserted = stack.copyWithCount(accepted);
                    setItem(slot, inserted);
                }
                return stack.copyWithCount(stack.getCount() - accepted);
            }
            if (!ItemStack.isSameItemSameComponents(current, stack)) return stack;
            int available = Math.min(stack.getMaxStackSize() - current.getCount(), stack.getCount());
            if (available <= 0) return stack;
            if (!simulate) {
                current.grow(available);
                setChanged();
            }
            return stack.copyWithCount(stack.getCount() - available);
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return canPlaceItem(slot, stack); }
    }
}
