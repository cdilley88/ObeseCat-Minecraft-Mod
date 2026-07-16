package com.fende.obesecat.entity;

import com.fende.obesecat.inventory.TargetDummyMenu;
import com.fende.obesecat.registry.ModItems;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TargetDummy extends PathfinderMob implements MenuProvider {
    private static final EntityDataAccessor<Float> LAST_DAMAGE =
            SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DAMAGE_TICKS =
            SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INFO_CARD_ENABLED =
            SynchedEntityData.defineId(TargetDummy.class, EntityDataSerializers.BOOLEAN);

    public TargetDummy(EntityType<? extends TargetDummy> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ARMOR, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override protected void registerGoals() {}
    @Override public boolean isPushable() { return false; }
    @Override protected void doPush(net.minecraft.world.entity.Entity entity) {}
    @Override public void push(double x, double y, double z) {}

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LAST_DAMAGE, 0.0F);
        builder.define(DAMAGE_TICKS, 0);
        builder.define(INFO_CARD_ENABLED, true);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        if (!level().isClientSide() && entityData.get(DAMAGE_TICKS) > 0) {
            entityData.set(DAMAGE_TICKS, entityData.get(DAMAGE_TICKS) - 1);
        }
    }

    @Override
    protected void actuallyHurt(DamageSource source, float amount) {
        float before = getHealth();
        super.actuallyHurt(source, amount);
        float dealt = Math.max(0.0F, before - getHealth());
        entityData.set(LAST_DAMAGE, dealt);
        entityData.set(DAMAGE_TICKS, 30);
        if (getHealth() <= 0.0F) {
            setHealth(getMaxHealth());
        }
    }

    @Override
    public void die(DamageSource source) {
        setHealth(getMaxHealth());
        entityData.set(DAMAGE_TICKS, 30);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide()) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS,
                    EquipmentSlot.FEET, EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
                ItemStack stack = getItemBySlot(slot);
                if (!stack.isEmpty()) spawnAtLocation(stack.copy());
                setItemSlot(slot, ItemStack.EMPTY);
            }
            spawnAtLocation(new ItemStack(ModItems.TARGET_DUMMY.get()));
            discard();
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(this, buffer -> buffer.writeVarInt(getId()));
        }
        return InteractionResult.CONSUME;
    }

    public float getLastDamage() { return entityData.get(LAST_DAMAGE); }
    public int getDamageTicks() { return entityData.get(DAMAGE_TICKS); }
    public boolean isInfoCardEnabled() { return entityData.get(INFO_CARD_ENABLED); }
    public void setInfoCardEnabled(boolean enabled) { entityData.set(INFO_CARD_ENABLED, enabled); }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("InfoCardEnabled", isInfoCardEnabled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("InfoCardEnabled")) {
            setInfoCardEnabled(tag.getBoolean("InfoCardEnabled"));
        }
    }

    @Override public Component getDisplayName() { return Component.translatable("entity.obesecat.target_dummy"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new TargetDummyMenu(id, inventory, this);
    }

}



