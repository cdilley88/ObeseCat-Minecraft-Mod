package com.fende.obesecat.block.entity;

import com.fende.obesecat.block.EchoingBlastChamberBlock;
import com.fende.obesecat.inventory.EchoingBlastChamberMenu;
import com.fende.obesecat.registry.ModBlockEntities;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EchoingBlastChamberBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int BLAZE_SLOT = 0;
    public static final int BREEZE_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int PROCESS_TIME = 200;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;

    public final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? progress : PROCESS_TIME; }
        @Override public void set(int index, int value) { if (index == 0) progress = value; }
        @Override public int getCount() { return 2; }
    };

    public EchoingBlastChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ECHOING_BLAST_CHAMBER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EchoingBlastChamberBlockEntity chamber) {
        boolean canProcess = chamber.canProcess();
        boolean changed = false;
        if (canProcess) {
            chamber.progress++;
            changed = true;
            if (chamber.progress >= PROCESS_TIME) {
                chamber.items.get(BLAZE_SLOT).shrink(1);
                chamber.items.get(BREEZE_SLOT).shrink(1);
                ItemStack output = chamber.items.get(OUTPUT_SLOT);
                if (output.isEmpty()) chamber.items.set(OUTPUT_SLOT, new ItemStack(ModItems.OVERDRIVE_ROD.get()));
                else output.grow(1);
                chamber.progress = 0;
            }
        } else if (chamber.progress != 0) {
            chamber.progress = 0;
            changed = true;
        }

        if (state.getValue(EchoingBlastChamberBlock.LIT) != canProcess) {
            level.setBlock(pos, state.setValue(EchoingBlastChamberBlock.LIT, canProcess), 3);
            changed = true;
        }
        if (changed) chamber.setChanged();
    }

    private boolean canProcess() {
        if (!items.get(BLAZE_SLOT).is(Items.BLAZE_ROD) || !items.get(BREEZE_SLOT).is(Items.BREEZE_ROD)) return false;
        ItemStack output = items.get(OUTPUT_SLOT);
        return output.isEmpty() || (output.is(ModItems.OVERDRIVE_ROD.get()) && output.getCount() < output.getMaxStackSize());
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer drops = new SimpleContainer(items.toArray(ItemStack[]::new));
        net.minecraft.world.Containers.dropContents(level, pos, drops);
        clearContent();
    }

    @Override protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt("progress", progress);
    }

    @Override protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
        progress = tag.getInt("progress");
    }

    @Override public Component getDisplayName() { return Component.translatable("container.obesecat.echoing_blast_chamber"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) { return new EchoingBlastChamberMenu(id, inventory, this, data); }
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack stack = ContainerHelper.removeItem(items, slot, amount); if (!stack.isEmpty()) setChanged(); return stack; }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }
    @Override public void setItem(int slot, ItemStack stack) { items.set(slot, stack); stack.limitSize(getMaxStackSize(stack)); setChanged(); }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) { return slot == BLAZE_SLOT ? stack.is(Items.BLAZE_ROD) : slot == BREEZE_SLOT && stack.is(Items.BREEZE_ROD); }
    @Override public void clearContent() { items.clear(); setChanged(); }
}
