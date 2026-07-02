package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.SkillSwordItem;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import com.fende.obesecat.world.StasisSwordManager;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StasisSwordGameTests {
    private static final String TEMPLATE = "village/plains/houses/manhattan_bunker";

    @GameTest(template = TEMPLATE)
    public static void stasisSwordStillUsesSharedSwordShell(GameTestHelper helper) {
        ItemStack sword = new ItemStack(ModItems.STASIS_SWORD.get());

        helper.assertTrue(
                sword.getItem().getUseAnimation(sword) == UseAnim.NONE,
                "Stasis Sword should keep the instant sword-cast use animation"
        );
        helper.assertTrue(
                sword.getItem() instanceof SkillSwordItem,
                "Stasis Sword should move onto the shared Skill Sword shell"
        );
        helper.assertTrue(
                sword.getItem().canPerformAction(sword, ItemAbilities.SWORD_SWEEP),
                "Stasis Sword should still expose default sword actions after the refactor"
        );
        helper.assertTrue(
                sword.getItem().isFoil(sword),
                "Stasis Sword should stay visually enchanted"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void stasisSwordPlacesAndShattersIceFormation(GameTestHelper helper) {
        ItemStack sword = new ItemStack(ModItems.STASIS_SWORD.get());
        helper.assertTrue(
                sword.getItem().getUseAnimation(sword) == UseAnim.NONE,
                "Stasis Sword should not use a held-use animation on right click"
        );

        BlockPos origin = new BlockPos(5, 5, 5);
        for (int dx = -4; dx <= 4; dx++) {
            for (int dy = -4; dy <= 4; dy++) {
                for (int dz = -4; dz <= 4; dz++) {
                    helper.setBlock(origin.offset(dx, dy, dz), Blocks.AIR);
                }
            }
        }

        StasisSwordManager.FrozenFormation formation = StasisSwordManager.place(helper.getLevel(), origin)
                .orElseThrow(() -> new AssertionError("Stasis Sword must place the stasis ice structure"));
        helper.assertTrue(
                helper.getLevel().getBlockState(origin).isAir(),
                "The Stasis Sword should not replace the targeted block itself"
        );
        BlockPos firstBlock = formation.placedBlocks().get(0);
        helper.assertTrue(
                helper.getLevel().getBlockState(firstBlock).is(Blocks.ICE),
                "The stasis ice structure should place ice blocks"
        );

        var player = helper.makeMockServerPlayerInLevel();
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        player.teleportTo(origin.getX() + 0.5D, origin.getY() + 1.0D, origin.getZ() - 3.5D);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin));

        InteractionResultHolder<ItemStack> result = sword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result.getResult() == InteractionResult.CONSUME,
                "A valid Stasis Sword cast should keep the old server-side consume semantics after the shared-shell refactor"
        );
        helper.assertTrue(
                result.getObject() == sword,
                "Stasis Sword should keep the held stack when cast"
        );
        helper.assertTrue(
                player.getCooldowns().isOnCooldown(ModItems.STASIS_SWORD.get()),
                "A successful stasis cast should still apply cooldown"
        );

        helper.runAfterDelay(StasisSwordManager.SHATTER_DELAY_TICKS + 1L, () -> {
            helper.assertTrue(
                    helper.getLevel().getBlockState(firstBlock).isAir(),
                    "The stasis ice structure should shatter after 40 ticks"
            );
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void stasisSwordNeverReplacesExistingTerrain(GameTestHelper helper) {
        BlockPos origin = new BlockPos(5, 5, 5);
        helper.setBlock(origin, Blocks.STONE);

        StasisSwordManager.place(helper.getLevel(), origin)
                .orElseThrow(() -> new AssertionError("Stasis Sword must place the stasis ice structure"));

        helper.assertTrue(
                helper.getLevel().getBlockState(origin).is(Blocks.STONE),
                "The Stasis Sword must not overwrite existing blocks"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void stasisSwordSnapsNearbyLivingEntitiesIntoTheCenter(GameTestHelper helper) {
        BlockPos origin = new BlockPos(5, 5, 5);
        Mob target = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 6, 5, 5);

        StasisSwordManager.place(helper.getLevel(), origin)
                .orElseThrow(() -> new AssertionError("Stasis Sword must place the stasis ice structure"));

        Vec3 expectedCenter = Vec3.atCenterOf(origin.offset(0, 2, 0));
        helper.assertTrue(
                target.position().distanceTo(expectedCenter) < 0.01D,
                "The Stasis Sword should snap nearby living entities into the air pocket before the ice closes"
        );
        helper.assertTrue(
                target.getHealth() == target.getMaxHealth() - 8.0F,
                "The Stasis Sword should deal 8 damage after the snap"
        );
        helper.succeed();
    }

    private StasisSwordGameTests() {
    }
}
