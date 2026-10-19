package com.fende.obesecat.item;

import com.fende.obesecat.registry.ModSounds;
import com.fende.obesecat.world.IonStormManager;
import com.fende.obesecat.world.LocalSoundHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChronosphericDominoItem extends DominoItem {
    private static final String MODE_KEY = "ChronosphereMode";
    private static final int DEFAULT_COOLDOWN_TICKS = 30;
    private static final int OVERDRIVE_COOLDOWN_TICKS = 100;

    public enum ChronoPreset {
        DAWN("Chrono Shift: Dawn [06:00]", "Morning Sortie", 1000L, false, false, true, false, false),
        NOON("Chrono Shift: High Noon [12:00]", "High Visibility", 6000L, false, false, true, false, false),
        DUSK("Chrono Shift: Dusk [19:00]", "Twilight Operation", 13000L, false, false, true, false, false),
        MIDNIGHT("Chrono Shift: Midnight [00:00]", "Cover of Darkness", 18000L, false, false, true, false, false),
        CLEAR_SKIES("Weather Control: Clear Skies", "Skies Secured", -1L, false, false, false, true, false),
        DOWNPOUR("Weather Control: Downpour", "Torrential Infiltration", -1L, true, false, false, true, false),
        LIGHTNING_STORM("Weather Control: Lightning Storm", "Warning: Lightning Storm Created!", -1L, true, true, false, true, false),
        CHRONOSPHERIC_OVERDRIVE("Chronospheric Overdrive", "Warning: Chronosphere Activated!", 18000L, true, true, true, true, false),
        ION_STORM("Weather Control: Ion Storm", "Warning: Ion Storm Detected!", -1L, true, true, false, true, true);

        private final String label;
        private final String alertMessage;
        private final long time;
        private final boolean raining;
        private final boolean thundering;
        private final boolean modifiesTime;
        private final boolean modifiesWeather;
        private final boolean triggersIonStorm;

        ChronoPreset(String label, String alertMessage, long time, boolean raining, boolean thundering, boolean modifiesTime, boolean modifiesWeather, boolean triggersIonStorm) {
            this.label = label;
            this.alertMessage = alertMessage;
            this.time = time;
            this.raining = raining;
            this.thundering = thundering;
            this.modifiesTime = modifiesTime;
            this.modifiesWeather = modifiesWeather;
            this.triggersIonStorm = triggersIonStorm;
        }

        public String getLabel() {
            return label;
        }

        public String getAlertMessage() {
            return alertMessage;
        }

        public long getTime() {
            return time;
        }

        public boolean isRaining() {
            return raining;
        }

        public boolean isThundering() {
            return thundering;
        }

        public boolean modifiesTime() {
            return modifiesTime;
        }

        public boolean modifiesWeather() {
            return modifiesWeather;
        }

        public boolean triggersIonStorm() {
            return triggersIonStorm;
        }
    }

    public ChronosphericDominoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.isShiftKeyDown()) {
            int nextMode = (getMode(stack) + 1) % ChronoPreset.values().length;
            setMode(stack, nextMode);
            ChronoPreset preset = ChronoPreset.values()[nextMode];

            if (level.isClientSide()) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.8F, 1.5F);
            } else {
                player.displayClientMessage(
                        Component.literal("§c§l[CHRONOSPHERE] §ePreset §6[" + (nextMode + 1) + "/" + ChronoPreset.values().length + "]§e: §f" + preset.getLabel()),
                        true
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        ChronoPreset preset = ChronoPreset.values()[getMode(stack)];

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Apply Time Shift if configured
            if (preset.modifiesTime() && preset.getTime() >= 0) {
                serverLevel.setDayTime(preset.getTime());
            }

            // Apply Weather Shift if configured
            if (preset.modifiesWeather()) {
                serverLevel.setWeatherParameters(0, 7200, preset.isRaining(), preset.isThundering());
            }

            // 1. Primary Domino meow
            SoundEvent meow = getBarkSound(level);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), meow, 48.0D, 1.3F, 1.0F);

            // 2. Colossal sub-bass punch & kinetic shockwave
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.WARDEN_SONIC_BOOM, 48.0D, 1.2F, 0.85F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.GENERIC_EXPLODE.value(), 48.0D, 1.0F, 0.60F);

            // 3. Heavy dimensional cathedral reverb tail & spatial echo
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.BELL_RESONATE, 48.0D, 1.8F, 0.42F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), 48.0D, 1.2F, 0.55F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.BEACON_ACTIVATE, 48.0D, 1.0F, 1.25F);

            // Particle vortex: Electric sparks, portal rifts, and flash
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.8, 0.8, 0.8, 0.25);
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.7, 0.7, 0.7, 0.2);
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.6, 0.6, 0.5);
            serverLevel.sendParticles(ParticleTypes.FLASH, player.getX(), player.getY() + 1.0, player.getZ(), 2, 0.1, 0.1, 0.1, 0.0);

            int cooldown = DEFAULT_COOLDOWN_TICKS;

            // Ultimate Mode: Chronospheric Overdrive
            if (preset == ChronoPreset.CHRONOSPHERIC_OVERDRIVE) {
                cooldown = OVERDRIVE_COOLDOWN_TICKS;

                // Chronofield Distortion Buffs
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 200, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120, 1, false, true));

                // Tactical Chrono Displacement: Warp 12 blocks forward if clear
                Vec3 look = player.getViewVector(1.0F);
                Vec3 destination = player.position().add(look.scale(12.0D));
                BlockPos destPos = BlockPos.containing(destination);
                if (serverLevel.getBlockState(destPos).isAir() && serverLevel.getBlockState(destPos.above()).isAir()) {
                    player.teleportTo(destination.x, destination.y, destination.z);
                    LocalSoundHelper.playLocalized(serverLevel, destination, SoundEvents.ENDERMAN_TELEPORT, 36.0D, 1.2F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.PORTAL, destination.x, destination.y + 1.0, destination.z, 40, 0.5, 0.5, 0.5, 0.2);
                }
            }

            // Ion Storm Mode: Activates the full atmospheric Ion Storm weather effect with warning klaxon
            if (preset.triggersIonStorm()) {
                cooldown = OVERDRIVE_COOLDOWN_TICKS;
                IonStormManager.start(serverLevel, player.blockPosition());
            }

            // Red Alert Style Command Notification
            player.displayClientMessage(
                    Component.literal("§c§l[CHRONOSPHERE] §e§l" + preset.getAlertMessage()),
                    true
            );

            player.getCooldowns().addCooldown(this, cooldown);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int mode = tag.getInt(MODE_KEY);
        return Math.clamp(mode, 0, ChronoPreset.values().length - 1);
    }

    public static void setMode(ItemStack stack, int mode) {
        int clamped = Math.clamp(mode, 0, ChronoPreset.values().length - 1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(MODE_KEY, clamped));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ChronoPreset preset = ChronoPreset.values()[getMode(stack)];
        tooltipComponents.add(Component.literal("§8Current Preset: §c" + preset.getLabel()));
        tooltipComponents.add(Component.literal("§7Shift + Right-Click: §eCycle Preset"));
        tooltipComponents.add(Component.literal("§7Right-Click: §cEngage Chronospheric Shift"));
        tooltipComponents.add(Component.literal("§8\"Warning: Chronosphere activated.\"").withStyle(ChatFormatting.ITALIC));
    }
}
