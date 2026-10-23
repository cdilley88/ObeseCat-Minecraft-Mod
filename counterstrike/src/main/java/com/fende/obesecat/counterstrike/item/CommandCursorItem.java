package com.fende.obesecat.counterstrike.item;

import com.fende.obesecat.counterstrike.deploy.IDeployableStructure;
import com.fende.obesecat.counterstrike.deploy.IDeployedCore;
import com.fende.obesecat.counterstrike.deploy.StructureBounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The Command Cursor: An iconic 90s RTS-inspired tactical mouse pointer item.
 * Used to deploy structure crates into multiblocks and pack them back up.
 */
public class CommandCursorItem extends Item {

    public CommandCursorItem(Properties properties) {
        super(properties);
    }

    public CommandCursorItem() {
        this(new Item.Properties()
                .stacksTo(1)
                .fireResistant()
                .rarity(Rarity.RARE));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);

        // Shift + Right-Click: Disassemble / Pack-up deployed structure
        if (player != null && player.isShiftKeyDown()) {
            if (blockEntity instanceof IDeployedCore core) {
                if (!level.isClientSide) {
                    boolean success = core.packUp(level, pos, player);
                    if (success) {
                        player.displayClientMessage(
                                Component.literal("§a[EVA] §fStructure disassembled. Transport module secured."),
                                true
                        );
                        level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.8F, 1.2F);
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        // Standard Right-Click: Deploy placed structure crate
        if (state.getBlock() instanceof IDeployableStructure deployable) {
            if (!deployable.canDeploy(level, pos, player)) {
                if (!level.isClientSide && player != null) {
                    player.displayClientMessage(
                            Component.literal("§c[EVA] §fCannot deploy! Sector obstructed. Clear terrain."),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0F, 0.5F);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }

            if (!level.isClientSide) {
                boolean deployed = deployable.deploy(level, pos, player);
                if (deployed && player != null) {
                    player.displayClientMessage(
                            Component.literal("§a[EVA] §f" + deployable.getStructureName() + " deployed. Building operational."),
                            true
                    );
                    level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.5F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§6EVA TACTICAL COMMAND CURSOR").withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("§7\"Battlefield Control Online.\"").withStyle(ChatFormatting.ITALIC));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("§e[Right-Click] §fDeploy targeted structure crate"));
        tooltipComponents.add(Component.literal("§c[Shift + Right-Click] §fPack deployed core back into module"));
        tooltipComponents.add(Component.empty());
        tooltipComponents.add(Component.literal("§8Standard-issue C&C feline commander pointer.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
