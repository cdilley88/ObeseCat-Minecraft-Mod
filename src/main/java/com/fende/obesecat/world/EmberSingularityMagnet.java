package com.fende.obesecat.world;

import com.fende.obesecat.item.EmberSingularityItem;
import java.lang.reflect.Method;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class EmberSingularityMagnet {
    private static final double RADIUS = 5.0D;
    private static final double PICKUP_DISTANCE = 1.15D;
    private static final double MIN_PULL = 0.08D;
    private static final double MAX_PULL = 0.42D;
    private static final int PARTICLE_INTERVAL = 2;

    private EmberSingularityMagnet() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            if (hasActiveSingularity(player)) {
                attractItems(level, player);
            }
        }
    }

    private static boolean hasActiveSingularity(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isActiveSingularity(inventory.getItem(slot))) {
                return true;
            }
        }

        return hasActiveCurioSingularity(player);
    }

    private static boolean isActiveSingularity(ItemStack stack) {
        return stack.getItem() instanceof EmberSingularityItem && EmberSingularityItem.isActive(stack);
    }

    private static void attractItems(ServerLevel level, ServerPlayer player) {
        Vec3 target = player.position().add(0.0D, 0.9D, 0.0D);
        AABB box = player.getBoundingBox().inflate(RADIUS);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive)) {
            Vec3 offset = target.subtract(item.position());
            double distance = offset.length();
            if (distance <= 0.01D || distance > RADIUS) {
                continue;
            }

            if (distance <= PICKUP_DISTANCE) {
                item.playerTouch(player);
                continue;
            }

            double pullStrength = MIN_PULL + ((RADIUS - distance) / RADIUS) * (MAX_PULL - MIN_PULL);
            Vec3 pull = offset.normalize().scale(pullStrength);
            item.setDeltaMovement(item.getDeltaMovement().scale(0.55D).add(pull));

            if (level.getGameTime() % PARTICLE_INTERVAL == 0L) {
                level.sendParticles(ParticleTypes.FLAME, item.getX(), item.getY() + 0.15D, item.getZ(), 2, 0.12D, 0.12D, 0.12D, 0.01D);
                level.sendParticles(ParticleTypes.SMOKE, item.getX(), item.getY() + 0.15D, item.getZ(), 1, 0.10D, 0.10D, 0.10D, 0.005D);
            }
        }
    }

    private static boolean hasActiveCurioSingularity(ServerPlayer player) {
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
            if (stack instanceof ItemStack itemStack && isActiveSingularity(itemStack)) {
                return true;
            }
        }

        return false;
    }
}
