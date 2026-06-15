package com.fende.obesecat.world;

import com.fende.obesecat.item.NightVisionMrKittyItem;
import com.fende.obesecat.network.NightVisionOverlayPayload;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NightVisionMrKittyManager {
    private static final int EFFECT_TICKS = 260;
    private static final float EXTRA_EXHAUSTION_PER_TICK = 0.005F;
    private static final Map<UUID, Boolean> LAST_ACTIVE_STATE = new HashMap<>();

    private NightVisionMrKittyManager() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            boolean active = hasActiveNightVisionKitty(player);
            if (active) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_TICKS, 0, true, false, false));
                if (!player.getAbilities().instabuild) {
                    player.causeFoodExhaustion(EXTRA_EXHAUSTION_PER_TICK);
                }
            }

            syncOverlayState(player, active);
        }
    }

    private static boolean hasActiveNightVisionKitty(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isActiveNightVisionKitty(inventory.getItem(slot))) {
                return true;
            }
        }

        return hasActiveCurioNightVisionKitty(player);
    }

    private static boolean isActiveNightVisionKitty(ItemStack stack) {
        return stack.getItem() instanceof NightVisionMrKittyItem && NightVisionMrKittyItem.isActive(stack);
    }

    private static void syncOverlayState(ServerPlayer player, boolean active) {
        UUID playerId = player.getUUID();
        Boolean previous = LAST_ACTIVE_STATE.put(playerId, active);
        if (previous == null || previous != active) {
            if (!active) {
                player.removeEffect(MobEffects.NIGHT_VISION);
            }
            PacketDistributor.sendToPlayer(player, new NightVisionOverlayPayload(active));
        }
    }

    private static boolean hasActiveCurioNightVisionKitty(ServerPlayer player) {
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
            if (stack instanceof ItemStack itemStack && isActiveNightVisionKitty(itemStack)) {
                return true;
            }
        }

        return false;
    }
}
