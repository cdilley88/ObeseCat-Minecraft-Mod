package com.fende.obesecat.world;

import com.fende.obesecat.item.MrKittysPawsItem;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

public final class MrKittysPawsManager {
    private MrKittysPawsManager() {
    }

    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        if (!hasActivePaws(player)) {
            return;
        }
        if (!player.getAbilities().instabuild && player.getFoodData().getFoodLevel() <= 0) {
            return;
        }

        int hungerCost = calculateHungerCost(player, event.getDistance(), event.getDamageMultiplier());
        if (!player.getAbilities().instabuild) {
            player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - hungerCost));
            player.getFoodData().setSaturation(0.0F);
        }

        Level level = player.level();
        level.playSound(null, player.blockPosition(), SoundEvents.WOOL_STEP, SoundSource.PLAYERS, 1.0F, 1.35F);
        event.setCanceled(true);
    }

    private static int calculateHungerCost(Player player, float fallDistance, float damageMultiplier) {
        double safeFallDistance = player.getAttributeValue(Attributes.SAFE_FALL_DISTANCE);
        double fallDamageMultiplier = player.getAttributeValue(Attributes.FALL_DAMAGE_MULTIPLIER);
        int wouldTakeDamage = (int) Math.ceil(Math.max(0.0D, (fallDistance - safeFallDistance) * damageMultiplier * fallDamageMultiplier));
        int hungerCost = Math.max(1, (int) Math.ceil(wouldTakeDamage / 2.0D));
        return Math.min(player.getFoodData().getFoodLevel(), hungerCost);
    }

    private static boolean hasActivePaws(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isActivePaws(inventory.getItem(slot))) {
                return true;
            }
        }

        return hasActiveCurioPaws(player);
    }

    private static boolean isActivePaws(ItemStack stack) {
        return stack.getItem() instanceof MrKittysPawsItem && MrKittysPawsItem.isActive(stack);
    }

    private static boolean hasActiveCurioPaws(Player player) {
        if (!ModList.get().isLoaded("curios")) {
            return false;
        }

        try {
            Class<?> curiosApi = Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Method getCuriosInventory = curiosApi.getMethod("getCuriosInventory", LivingEntity.class);
            Object result = getCuriosInventory.invoke(null, player);
            Object curiosHandler = unwrapOptionalLike(result);
            if (curiosHandler == null) {
                return false;
            }

            Method getEquippedCurios = curiosHandler.getClass().getMethod("getEquippedCurios");
            Object equippedCurios = getEquippedCurios.invoke(curiosHandler);
            return hasActiveStackInItemHandler(equippedCurios);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static Object unwrapOptionalLike(Object value) throws ReflectiveOperationException {
        if (value instanceof Optional<?> optional) {
            return optional.orElse(null);
        }
        if (value == null) {
            return null;
        }

        Method resolve = value.getClass().getMethod("resolve");
        Object resolved = resolve.invoke(value);
        if (resolved instanceof Optional<?> optional) {
            return optional.orElse(null);
        }

        return null;
    }

    private static boolean hasActiveStackInItemHandler(Object itemHandler) throws ReflectiveOperationException {
        if (itemHandler == null) {
            return false;
        }

        Method getSlots = itemHandler.getClass().getMethod("getSlots");
        Method getStackInSlot = itemHandler.getClass().getMethod("getStackInSlot", int.class);
        int slots = (Integer) getSlots.invoke(itemHandler);
        for (int slot = 0; slot < slots; slot++) {
            Object stack = getStackInSlot.invoke(itemHandler, slot);
            if (stack instanceof ItemStack itemStack && isActivePaws(itemStack)) {
                return true;
            }
        }

        return false;
    }
}
