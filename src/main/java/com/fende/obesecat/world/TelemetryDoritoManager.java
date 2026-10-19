package com.fende.obesecat.world;

import com.fende.obesecat.item.TelemetryDoritoItem;
import com.fende.obesecat.network.TelemetryOverlayPayload;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class TelemetryDoritoManager {
    private static final Map<UUID, Boolean> LAST_ACTIVE_STATE = new HashMap<>();

    private TelemetryDoritoManager() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            boolean active = hasActiveTelemetryDorito(player);
            syncOverlayState(player, active);
        }
    }

    private static void syncOverlayState(ServerPlayer player, boolean active) {
        Boolean previous = LAST_ACTIVE_STATE.get(player.getUUID());
        if (previous == null || previous != active) {
            LAST_ACTIVE_STATE.put(player.getUUID(), active);
            PacketDistributor.sendToPlayer(player, new TelemetryOverlayPayload(active));
        }
    }

    private static boolean hasActiveTelemetryDorito(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isActiveTelemetry(inventory.getItem(slot))) {
                return true;
            }
        }

        return hasActiveCurioTelemetry(player);
    }

    private static boolean isActiveTelemetry(ItemStack stack) {
        return (stack.getItem() instanceof TelemetryDoritoItem && TelemetryDoritoItem.isActive(stack))
                || (stack.getItem() instanceof com.fende.obesecat.item.OmniVisorDoritoItem && com.fende.obesecat.item.OmniVisorDoritoItem.isActive(stack));
    }

    private static boolean hasActiveCurioTelemetry(ServerPlayer player) {
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
            if (stack instanceof ItemStack itemStack && isActiveTelemetry(itemStack)) {
                return true;
            }
        }

        return false;
    }
}
