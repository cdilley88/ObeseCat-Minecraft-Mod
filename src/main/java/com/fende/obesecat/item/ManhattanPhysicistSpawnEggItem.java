package com.fende.obesecat.item;

import com.fende.obesecat.world.ManhattanPhysicistSpawner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos spawnPos = serverLevel.getBlockState(clickedPos).getCollisionShape(serverLevel, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(clickedFace);
        Villager villager = createPhysicist(serverLevel, spawnPos);
        if (villager == null || !serverLevel.addFreshEntity(villager)) {
            return InteractionResult.FAIL;
        }

        context.getItemInHand().shrink(1);
        serverLevel.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, villager.position());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.success(stack);
        }

        BlockPos clickedPos = hitResult.getBlockPos();
        if (!level.mayInteract(player, clickedPos)
                || !player.mayUseItemAt(clickedPos, hitResult.getDirection(), stack)) {
            return InteractionResultHolder.fail(stack);
        }

        BlockPos spawnPos = level.getBlockState(clickedPos).getCollisionShape(level, clickedPos).isEmpty()
                ? clickedPos
                : clickedPos.relative(hitResult.getDirection());
        Villager villager = createPhysicist(serverLevel, spawnPos);
        if (villager == null || !serverLevel.addFreshEntity(villager)) {
            return InteractionResultHolder.fail(stack);
        }

        stack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, villager.position());
        return InteractionResultHolder.consume(stack);
    }

    private static Villager createPhysicist(ServerLevel level, BlockPos spawnPos) {
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return null;
        }

        villager.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, 0.0F, 0.0F);
        villager.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.SPAWN_EGG, null);
        ManhattanPhysicistSpawner.initializeEggVillager(villager);
        return villager;
    }
}
