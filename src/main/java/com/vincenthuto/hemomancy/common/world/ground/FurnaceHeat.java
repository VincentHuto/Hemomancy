package com.vincenthuto.hemomancy.common.world.ground;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import com.vincenthuto.hemomancy.mixin.core.FurnaceFuelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Burning a furnace, smoker or blast furnace from outside, with no fuel in its slot: Furnace Veins' held stream (core-loop
 * round two P4) and a hot gouge run beside one (P8). Both go through {@link #stoke}, which lights the furnace as an edit
 * for a player and then raises its own burn time, so the furnace's own tick does the cooking at coal speed. A furnace
 * with nothing it can cook is never lit, and a longer burn already in it is never shortened.
 */
public final class FurnaceHeat {
	private FurnaceHeat() {
	}

	/** Whether {@code furnace} has an input its recipe type can cook and room for the result in its output. */
	public static boolean cooks(Level level, AbstractFurnaceBlockEntity furnace) {
		ItemStack input = furnace.getItem(0);
		if (input.isEmpty()) return false;
		RecipeType<? extends AbstractCookingRecipe> type = ((FurnaceFuelAccessor) furnace).hemomancy$getRecipeType();
		@SuppressWarnings("unchecked")
		Optional<? extends RecipeHolder<? extends AbstractCookingRecipe>> recipe = level.getRecipeManager()
				.getRecipeFor((RecipeType<AbstractCookingRecipe>) type, new SingleRecipeInput(input), level);
		if (recipe.isEmpty()) return false;
		ItemStack result = recipe.get().value().getResultItem(level.registryAccess());
		if (result.isEmpty()) return false;
		ItemStack output = furnace.getItem(2);
		if (output.isEmpty()) return true;
		if (!ItemStack.isSameItemSameComponents(output, result)) return false;
		int room = Math.min(furnace.getMaxStackSize(), output.getMaxStackSize());
		return output.getCount() + result.getCount() <= room;
	}

	/**
	 * Keeps the furnace at {@code pos} burning for at least {@code burn} more ticks, as {@code editor}'s edit. False, and
	 * nothing touched, when there is no furnace there, it has nothing to cook, or {@code editor} may not alter the block.
	 * An unlit furnace is lit through {@link PlayerBlockEdits#replace} before its burn time is raised (its own tick would
	 * otherwise light it from the burn time alone, past the refusal); a lit one is checked with the same rules without an
	 * edit.
	 */
	public static boolean stoke(ServerPlayer editor, ServerLevel level, BlockPos pos, int burn) {
		if (!(level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) || !cooks(level, furnace)) return false;
		BlockState state = level.getBlockState(pos);
		if (!state.hasProperty(AbstractFurnaceBlock.LIT)) return false;
		if (!state.getValue(AbstractFurnaceBlock.LIT)) {
			if (!PlayerBlockEdits.replace(editor, pos, state.setValue(AbstractFurnaceBlock.LIT, true))) return false;
		} else if (!ManipulationCombatHelper.mayAlter(editor, pos)) {
			return false;
		}
		FurnaceFuelAccessor fuel = (FurnaceFuelAccessor) furnace;
		if (fuel.hemomancy$getLitTime() < burn) {
			fuel.hemomancy$setLitTime(burn);
			fuel.hemomancy$setLitDuration(burn);
		}
		furnace.setChanged();
		return true;
	}
}
