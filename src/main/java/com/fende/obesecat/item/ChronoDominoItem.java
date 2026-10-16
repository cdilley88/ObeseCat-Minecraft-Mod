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

public class ChronoDominoItem extends DominoItem {
    private static final String MODE_KEY = "ChronoMode";
    private static final int COOLDOWN_TICKS = 20;

    public enum TimePreset {
        DAWN("Dawn [06:00]", "Breakfast Time!", 1000L),
        MIDDAY("Midday [12:00]", "Lunch Time!", 6000L),
        DUSK("Dusk [19:00]", "Treat Time?", 13000L),
        MIDNIGHT("Midnight [00:00]", "Still Hungry...", 18000L);

        private final String label;
        private final String caption;
        private final long time;

        TimePreset(String label, String caption, long time) {
            this.label = label;
            this.caption = caption;
            this.time = time;
        }

        public String getLabel() {
            return label;
        }

        public String getCaption() {
            return caption;
        }

        public long getTime() {
            return time;
        }
    }

    public ChronoDominoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);

        if (player.isShiftKeyDown()) {
            int nextMode = (getMode(stack) + 1) % TimePreset.values().length;
            setMode(stack, nextMode);
            TimePreset preset = TimePreset.values()[nextMode];

            if (level.isClientSide()) {
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.7F, 1.4F);
            } else {
                player.displayClientMessage(
                        Component.literal("§6§l[Chrono Domino] §eTime Target: §f" + preset.getLabel() + " §8- §e" + preset.getCaption()),
                        true
                );
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        TimePreset preset = TimePreset.values()[getMode(stack)];

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Apply temporal shift
            serverLevel.setDayTime(preset.getTime());

            // Primary meow
            SoundEvent meow = getBarkSound(level);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), meow, 36.0D, 1.3F, 1.0F);

            // Short subtle temporal reverb & shimmer (doesn't overpower her meow)
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.AMETHYST_BLOCK_RESONATE, 36.0D, 0.8F, 1.25F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.CHORUS_FRUIT_TELEPORT, 36.0D, 0.45F, 1.45F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.ENCHANTMENT_TABLE_USE, 36.0D, 0.6F, 1.3F);
            LocalSoundHelper.playLocalized(serverLevel, player.position(), SoundEvents.BEACON_AMBIENT, 36.0D, 0.4F, 1.6F);

            // Temporal portal swirls & enchantment glyphs
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.5, 0.5, 0.5, 0.15);
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.4, 0.4, 0.4, 0.5);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.6, 0.6, 0.8);

            player.displayClientMessage(
                    Component.literal("§6§l[Chrono Domino] §eTemporal Shift Engaged: §f" + preset.getLabel()),
                    true
            );

            player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static int getMode(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        int mode = tag.getInt(MODE_KEY);
        return Math.clamp(mode, 0, TimePreset.values().length - 1);
    }

    public static void setMode(ItemStack stack, int mode) {
        int clamped = Math.clamp(mode, 0, TimePreset.values().length - 1);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(MODE_KEY, clamped));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        TimePreset preset = TimePreset.values()[getMode(stack)];
        tooltipComponents.add(Component.literal("§8Current Target: §e" + preset.getLabel() + " §7(" + preset.getCaption() + ")"));
        tooltipComponents.add(Component.literal("§7Sneak + Right-Click: §eCycle Time Preset"));
        tooltipComponents.add(Component.literal("§7Right-Click: §6Shift World Time"));
    }
}
