package com.fende.obesecat.item;

import com.fende.obesecat.world.LocalSoundHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

public class AtmosphericDominoItem extends DominoItem {
    private static final String MODE_KEY = "AtmosphericMode";
    private static final int COOLDOWN_TICKS = 20;

    public enum WeatherPreset {
        CLEAR("Clear Skies", "Calming Presence", false, false),
        DOWNPOUR("Downpour", "Warm Home", true, false),
        THUNDERSTORM("Thunderstorm", "Tempest", true, true);

        private final String label;
        private final String caption;
        private final boolean raining;
        private final boolean thundering;

        WeatherPreset(String label, String caption, boolean raining, boolean thundering) {
            this.label = label;
            this.caption = caption;
            this.raining = raining;
            this.thundering = thundering;
        }

        public String getLabel() {
            return label;
        }

        public String getCaption() {
            return caption;
        }

        public boolean isRaining() {
            return raining;
        }

        public boolean isThundering() {
            return thundering;
        }
    }

    public AtmosphericDominoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.isShiftKeyDown()) {
            int nextMode = (getMode(stack) + 1) % WeatherPreset.values().length;
            setMode(stack, nextMode);
            WeatherPreset preset = WeatherPreset.values()[nextMode];

            if (level.isClientSide()) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.4F);
            } else {
                player.displayClientMessage(
                        Component.literal("§b§l[Atmospheric Domino] §3Weather Target: §f" + preset.getLabel() + " §8- §b" + preset.getCaption()),
                        true
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        WeatherPreset preset = WeatherPreset.values()[getMode(stack)];

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Apply weather change (6000 ticks = 5 minutes)
            serverLevel.setWeatherParameters(0, 6000, preset.isRaining(), preset.isThundering());

            // Primary meow + crackle audio layering
            SoundEvent meow = getBarkSound(level);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), meow, 36.0D, 1.2F, 1.05F);

            // Crackle sound effects: popping campfire crackle, lightning rod electric snap, electric sparks, distant rumble
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.CAMPFIRE_CRACKLE, 36.0D, 2.0F, 1.3F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.LIGHTNING_BOLT_IMPACT, 36.0D, 0.7F, 1.6F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.FIRE_AMBIENT, 36.0D, 1.0F, 1.5F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.LIGHTNING_BOLT_THUNDER, 48.0D, 0.4F, 1.3F);

            // Atmospheric electric sparks, stormy clouds & water splash particles
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.6, 0.6, 0.6, 0.15);
            serverLevel.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 1.2, player.getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            serverLevel.sendParticles(ParticleTypes.SPLASH, player.getX(), player.getY() + 0.6, player.getZ(), 25, 0.5, 0.5, 0.5, 0.1);

            player.displayClientMessage(
                    Component.literal("§b§l[Atmospheric Domino] §3Atmospheric Shift Engaged: §f" + preset.getLabel()),
                    true
            );

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int mode = tag.getInt(MODE_KEY);
        return Math.clamp(mode, 0, WeatherPreset.values().length - 1);
    }

    public static void setMode(ItemStack stack, int mode) {
        int clamped = Math.clamp(mode, 0, WeatherPreset.values().length - 1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(MODE_KEY, clamped));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        WeatherPreset preset = WeatherPreset.values()[getMode(stack)];
        tooltipComponents.add(Component.literal("§8Current Target: §b" + preset.getLabel() + " §7(" + preset.getCaption() + ")"));
        tooltipComponents.add(Component.literal("§7Sneak + Right-Click: §bCycle Weather Preset"));
        tooltipComponents.add(Component.literal("§7Right-Click: §3Shift World Weather"));
    }
}
