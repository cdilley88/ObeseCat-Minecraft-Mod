package com.fende.obesecat.item;

import com.fende.obesecat.world.ManhattanPhysicistSpawner;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ManhattanPhysicistSpawnEggItem extends SpawnEggItem {
    public ManhattanPhysicistSpawnEggItem(Properties properties) {
        super(EntityType.VILLAGER, 0x7C7B6A, 0xD9D1B8, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        var clickedState = level.getBlockState(clickedPos);

        BlockPos spawnPos = clickedState.getCollisionShape(level, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(clickedFace);

        Entity entity = EntityType.VILLAGER.spawn(
                serverLevel,
                stack,
                context.getPlayer(),
                spawnPos,
                MobSpawnType.SPAWN_EGG,
                true,
                !Objects.equals(clickedPos, spawnPos) && clickedFace == Direction.UP
        );

        if (entity instanceof Villager villager) {
            configureVillager(villager);
            stack.shrink(1);
            level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, clickedPos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        } else if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        } else {
            BlockPos blockPos = hitResult.getBlockPos();
            if (!level.mayInteract(player, blockPos) || !player.mayUseItemAt(blockPos, hitResult.getDirection(), stack)) {
                return InteractionResultHolder.fail(stack);
            }

            BlockPos spawnPos = level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty()
                    ? blockPos
                    : blockPos.relative(hitResult.getDirection());

            Entity entity = EntityType.VILLAGER.spawn(serverLevel, stack, player, spawnPos, MobSpawnType.SPAWN_EGG, true, true);
            if (entity instanceof Villager villager) {
                configureVillager(villager);
                stack.consume(1, player);
                player.awardStat(Stats.ITEM_USED.get(this));
                level.gameEvent(player, GameEvent.ENTITY_PLACE, entity.position());
                return InteractionResultHolder.consume(stack);
            }

            return InteractionResultHolder.pass(stack);
        }
    }

    private void configureVillager(Villager villager) {
        ManhattanPhysicistSpawner.configureVillager(villager);
    }
}
