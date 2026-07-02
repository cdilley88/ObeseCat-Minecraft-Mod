package com.fende.obesecat.gametest;

import com.fende.obesecat.ObeseCatMod;
import com.fende.obesecat.item.HolySwordItem;
import com.fende.obesecat.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ObeseCatMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SkillSwordGameTests {
    private static final String TEMPLATE = "village/plains/houses/manhattan_bunker";

    @GameTest(template = TEMPLATE)
    public static void holySwordUsesTheReusableSkillSwordShell(GameTestHelper helper) {
        ItemStack holySword = new ItemStack(ModItems.HOLY_SWORD.get());

        helper.assertTrue(
                holySword.getItem() instanceof HolySwordItem,
                "Holy Sword should be registered through HolySwordItem instead of a plain item"
        );
        helper.assertTrue(
                holySword.getItem().getUseAnimation(holySword) == UseAnim.NONE,
                "Holy Sword should use the shared sword shell and stay out of the held-use animation state"
        );
        helper.assertTrue(
                holySword.getItem().getEnchantmentValue() == 10,
                "Holy Sword should keep the shared sword shell enchantment value"
        );
        helper.assertTrue(
                holySword.getItem().canPerformAction(holySword, ItemAbilities.SWORD_SWEEP),
                "Holy Sword should keep the shared sword shell sword actions"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void holySwordRightClickIsCurrentlyInert(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack holySword = new ItemStack(ModItems.HOLY_SWORD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, holySword);

        InteractionResultHolder<ItemStack> result = holySword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result.getResult() == InteractionResult.PASS,
                "Holy Sword should inherit the base no-op cast behavior for now"
        );
        helper.assertTrue(
                result.getObject() == holySword,
                "Holy Sword should return the original stack when right click stays inert"
        );
        helper.assertTrue(
                !player.isUsingItem(),
                "Holy Sword should not enter the using state while it remains inert"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void holySwordUsePassesWhileOnCooldown(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ItemStack holySword = new ItemStack(ModItems.HOLY_SWORD.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, holySword);
        player.getCooldowns().addCooldown(holySword.getItem(), 20);

        InteractionResultHolder<ItemStack> result = holySword.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(
                result.getResult() == InteractionResult.PASS,
                "Skill Sword base use should return pass while the item is on cooldown"
        );
        helper.assertTrue(
                result.getObject() == holySword,
                "Skill Sword base use should return the original stack while cooldown blocks the cast"
        );
        helper.succeed();
    }

    private SkillSwordGameTests() {
    }
}
