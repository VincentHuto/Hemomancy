package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Pyretic Forge — a T2 (MEDIOCRITAS) quick manipulation that smelts
 * items in the player's hand using blood-borne heat.
 * <p>
 * The number of items smelted per use scales with the Crimson Mastery
 * skill: base 8 items, increasing by ~15% per level up to the full
 * stack size at max mastery.
 * <p>
 * Looks up the smelting recipe for the held item and replaces the
 * smelted portion with the result. Un-smelted items remain in hand.
 * <p>
 * Perfect mid-game utility for instant field smelting of ores and food.
 */
public class PyreticForgeManip extends BloodManipulation {

	/** Base items smelted per cast with no skill investment. */
	private static final int BASE_SMELT_COUNT = 8;

	public PyreticForgeManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	protected boolean canPerformAction(Player player, ItemStack heldItem, float ticks) {
		return !heldItem.isEmpty() && player.level().getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(heldItem), player.level())
				.filter(recipe -> !recipe.value().getResultItem(player.level().registryAccess()).isEmpty()).isPresent()
				&& super.canPerformAction(player, heldItem, ticks);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (heldItemMainhand.isEmpty()) {
			return;
		}

		Optional<RecipeHolder<SmeltingRecipe>> recipe = world.getRecipeManager()
				.getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(heldItemMainhand), world);

		if (recipe.isEmpty()) {
			return;
		}

		ItemStack result = recipe.get().value().getResultItem(world.registryAccess());
		if (result.isEmpty()) {
			return;
		}

		int held = heldItemMainhand.getCount();
		int smeltCount = (int) Math.min(held,
				Math.ceil(BASE_SMELT_COUNT * SkillPointHelper.getCrimsonMasteryMultiplier(player)));

		int outputCount = smeltCount * result.getCount();
		heldItemMainhand.shrink(smeltCount);
		boolean replaceHand = heldItemMainhand.isEmpty();
		while (outputCount > 0) {
			int count = Math.min(outputCount, result.getMaxStackSize());
			ItemStack smeltedStack = result.copyWithCount(count);
			outputCount -= count;
			if (replaceHand) {
				player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, smeltedStack);
				replaceHand = false;
			} else if (!player.getInventory().add(smeltedStack)) {
				player.drop(smeltedStack, false);
			}
		}

		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.8f, 1.0f);
		world.playSound(null, player.blockPosition(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 0.6f, 1.2f);

		if (world instanceof ServerLevel sLevel) {
			ManipulationVisuals.burst(sLevel, ManipulationVisuals.Form.FORGE, player.getEyePosition().add(player.getLookAngle()).add(0,-1,0), player.position(), 1, 24);
            BlockPos pos = player.blockPosition();
			RandomSource random = world.random;


		}
	}
}
