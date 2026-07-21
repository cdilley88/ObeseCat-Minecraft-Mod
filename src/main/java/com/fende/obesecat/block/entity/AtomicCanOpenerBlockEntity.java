package com.fende.obesecat.block.entity;

import com.fende.obesecat.energy.CatFoodEnergy;
import com.fende.obesecat.inventory.CanOpenerMenu;
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

public class AtomicCanOpenerBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int CAPACITY = CatFoodEnergy.toFe(CatFoodEnergy.CAN_OPENER_CAPACITY_POINTS);
    public static final int MAX_OUTPUT_PER_TICK = 1_000;
    public static final int PROCESS_TIME = 40;
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final boolean[] itemInputSides = new boolean[6];
    private final boolean[] powerOutputSides = new boolean[6];
    private final CanOpenerEnergyStorage energy = new CanOpenerEnergyStorage();
    private final CanOpenerItemHandler itemHandler = new CanOpenerItemHandler();
    private int progress;
    private long outputBudgetGameTime = Long.MIN_VALUE;
    private int outputUsedThisTick;

    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> energy.getEnergyStored();
                case 1 -> CAPACITY;
                case 2 -> progress;
                case 3 -> PROCESS_TIME;
                default -> {
                    if (index >= 4 && index < 10) yield itemInputSides[index - 4] ? 1 : 0;
                    if (index >= 10 && index < 16) yield powerOutputSides[index - 10] ? 1 : 0;
                    yield 0;
                }
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) energy.setEnergy(value);
            else if (index == 2) progress = value;
            else if (index >= 4 && index < 10) itemInputSides[index - 4] = value != 0;
            else if (index >= 10 && index < 16) powerOutputSides[index - 10] = value != 0;
        }
        @Override public int getCount() { return 16; }
    };

    public AtomicCanOpenerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CAN_OPENER.get(), pos, state);
        Arrays.fill(powerOutputSides, true);
        itemInputSides[Direction.UP.ordinal()] = true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AtomicCanOpenerBlockEntity opener) {
        opener.processFuel();
        opener.pushEnergy(level, pos);
    }

    private void processFuel() {
        ItemStack fuel = items.getFirst();
        int points = CatFoodEnergy.getFuelPoints(fuel);
        int produced = CatFoodEnergy.toFe(points);
        if (points <= 0 || energy.getEnergyStored() + produced > CAPACITY) {
            if (progress != 0) {
                progress = 0;
                setChanged();
            }
            return;
        }

        progress++;
        if (progress >= PROCESS_TIME) {
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

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("Energy", energy.getEnergyStored());
        tag.putInt("Progress", progress);
        tag.putByteArray("ItemInputSides", toByteArray(itemInputSides));
        tag.putByteArray("PowerOutputSides", toByteArray(powerOutputSides));
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        energy.setEnergy(tag.getInt("Energy"));
        progress = Math.max(0, tag.getInt("Progress"));
        byte[] savedItemSides = tag.getByteArray("ItemInputSides");
        byte[] savedPowerSides = tag.getByteArray("PowerOutputSides");
        if (savedItemSides.length == itemInputSides.length) fromByteArray(savedItemSides, itemInputSides);
        if (savedPowerSides.length == powerOutputSides.length) fromByteArray(savedPowerSides, powerOutputSides);

        // Migrate the first proof-of-concept's mutually exclusive side modes.
        int[] legacyModes = tag.getIntArray("SideModes");
        if (savedItemSides.length != itemInputSides.length && legacyModes.length == itemInputSides.length) {
            Arrays.fill(itemInputSides, false);
            Arrays.fill(powerOutputSides, false);
            for (int i = 0; i < legacyModes.length; i++) {
                itemInputSides[i] = legacyModes[i] == 1;
                powerOutputSides[i] = legacyModes[i] == 2;
            }
        }
    }

    private static byte[] toByteArray(boolean[] values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) result[i] = (byte) (values[i] ? 1 : 0);
        return result;
    }

    private static void fromByteArray(byte[] source, boolean[] destination) {
        for (int i = 0; i < destination.length; i++) destination[i] = source[i] != 0;
    }

    @Override public Component getDisplayName() { return Component.translatable("block.obesecat.can_opener"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new CanOpenerMenu(id, inventory, this, data, this); }
    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return items.getFirst().isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == 0 && CatFoodEnergy.getFuelPoints(stack) > 0; }
    @Override public void clearContent() { items.clear(); setChanged(); }

    private final class CanOpenerEnergyStorage implements IEnergyStorage {
        private int stored;
        @Override public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }
        @Override public int extractEnergy(int maxExtract, boolean simulate) {
            long gameTime = level == null ? 0L : level.getGameTime();
            if (outputBudgetGameTime != gameTime) {
                outputBudgetGameTime = gameTime;
                outputUsedThisTick = 0;
            }
            int remainingBudget = Math.max(0, MAX_OUTPUT_PER_TICK - outputUsedThisTick);
            int extracted = Math.min(Math.max(maxExtract, 0), Math.min(stored, remainingBudget));
            if (!simulate && extracted > 0) {
                stored -= extracted;
                outputUsedThisTick += extracted;
                setChanged();
            }
            return extracted;
        }
        @Override public int getEnergyStored() { return stored; }
        @Override public int getMaxEnergyStored() { return CAPACITY; }
        @Override public boolean canExtract() { return true; }
        @Override public boolean canReceive() { return false; }
        private void addEnergy(int amount) { stored = Math.min(CAPACITY, stored + Math.max(amount, 0)); setChanged(); }
        private void setEnergy(int amount) { stored = Math.max(0, Math.min(CAPACITY, amount)); }
    }

    private final class CanOpenerItemHandler implements IItemHandler {
        @Override public int getSlots() { return 1; }
        @Override public ItemStack getStackInSlot(int slot) { return slot == 0 ? items.getFirst() : ItemStack.EMPTY; }
        @Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0 || !canPlaceItem(slot, stack)) return stack;
            ItemStack existing = items.getFirst();
            if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) return stack;
            int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            int space = existing.isEmpty() ? limit : limit - existing.getCount();
            if (space <= 0) return stack;
            int inserted = Math.min(space, stack.getCount());
            if (!simulate) {
                if (existing.isEmpty()) items.set(0, stack.copyWithCount(inserted));
                else existing.grow(inserted);
                setChanged();
            }
            ItemStack remainder = stack.copy();
            remainder.shrink(inserted);
            return remainder;
        }
        @Override public ItemStack extractItem(int slot, int amount, boolean simulate) { return ItemStack.EMPTY; }
        @Override public int getSlotLimit(int slot) { return 64; }
        @Override public boolean isItemValid(int slot, ItemStack stack) { return slot == 0 && CatFoodEnergy.getFuelPoints(stack) > 0; }
    }
}
