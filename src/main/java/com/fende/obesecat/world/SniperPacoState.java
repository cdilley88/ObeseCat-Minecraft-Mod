package com.fende.obesecat.world;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class SniperPacoState {
    public static final int CLIP_SIZE = 10;
    public static final int SHOT_DELAY_TICKS = 30;
    public static final int RELOAD_TICKS = 200;
    private static final String AMMO_KEY = "SniperPacoAmmo";
    private static final String READY_AT_KEY = "SniperPacoReadyAt";

    private SniperPacoState() {
    }

    public static int consumeShot(ItemStack stack, long gameTime) {
        final int[] cooldown = new int[1];
        cooldown[0] = -1;
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> cooldown[0] = consumeShot(tag, gameTime));
        return cooldown[0];
    }

    public static boolean tryConsumeShot(ItemStack stack, long gameTime) {
        return consumeShot(stack, gameTime) != -1;
    }

    static int consumeShot(CompoundTag tag, long gameTime) {
        refreshReload(tag, gameTime);
        if (gameTime < getReadyAt(tag) || getAmmo(tag) <= 0) {
            return -1;
        }

        int remainingAmmo = getAmmo(tag) - 1;
        setAmmo(tag, remainingAmmo);
        int cooldown = remainingAmmo <= 0 ? RELOAD_TICKS : SHOT_DELAY_TICKS;
        setReadyAt(tag, gameTime + cooldown);
        return cooldown;
    }

    private static void refreshReload(CompoundTag tag, long gameTime) {
        if (getAmmo(tag) <= 0 && gameTime >= getReadyAt(tag)) {
            setAmmo(tag, CLIP_SIZE);
        }
    }

    private static int getAmmo(CompoundTag tag) {
        if (!tag.contains(AMMO_KEY)) {
            return CLIP_SIZE;
        }
        return Math.clamp(tag.getInt(AMMO_KEY), 0, CLIP_SIZE);
    }

    public static int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return getAmmo(tag);
    }

    private static void setAmmo(CompoundTag tag, int ammo) {
        int clampedAmmo = Math.clamp(ammo, 0, CLIP_SIZE);
        if (clampedAmmo >= CLIP_SIZE) {
            tag.remove(AMMO_KEY);
        } else {
            tag.putInt(AMMO_KEY, clampedAmmo);
        }
    }

    private static long getReadyAt(CompoundTag tag) {
        return tag.contains(READY_AT_KEY) ? tag.getLong(READY_AT_KEY) : 0L;
    }

    private static void setReadyAt(CompoundTag tag, long readyAt) {
        if (readyAt <= 0L) {
            tag.remove(READY_AT_KEY);
        } else {
            tag.putLong(READY_AT_KEY, readyAt);
        }
    }
}
