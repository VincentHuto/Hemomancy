package com.vincenthuto.hemomancy.common.tile;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.registry.BlockEntityInit;
import com.vincenthuto.hutoslib.common.block.entity.TileSimpleInventory;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class MorphlingIncubatorBlockEntity extends TileSimpleInventory {

	public MorphlingIncubatorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.morphling_incubator.get(), pos, state);
	}

	public boolean addItem(@Nullable Player player, ItemStack stack, @Nullable InteractionHand hand) {

		boolean did = false;

		for (int i = 0; i < inventorySize(); i++) {
			if (getItemHandler().getItem(i).isEmpty()) {
				did = true;
				ItemStack stackToAdd = stack.copy();
				stackToAdd.setCount(1);
				getItemHandler().setItem(i, stackToAdd);

				if (player == null || !player.getAbilities().instabuild) {
					stack.shrink(1);
				}

				break;
			}
		}

		if (did) {
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}

		return true;
	}

	@Override
	protected SimpleContainer createItemHandler() {
		return new SimpleContainer(5) {
			@Override
			public int getMaxStackSize() {
				return 1;
			}
		};
	}

	public boolean isEmpty() {
		for (int i = 0; i < inventorySize(); i++) {
			if (!getItemHandler().getItem(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void readPacketNBT(CompoundTag tag) {
		super.readPacketNBT(tag);

	}

	@Override
	public void writePacketNBT(CompoundTag tag) {
		super.writePacketNBT(tag);

	}

	/*
	 * public void onActivated(Player player, ItemStack wand) { RecipeWandMaker
	 * recipe = null; if (currentRecipe != null) recipe = currentRecipe; else for
	 * (RecipeWandMaker recipe_ : ModWandRecipies.wandMakerRecipies) {
	 *
	 * if (recipe_.matches(itemHandler)) { recipe = recipe_; break; } }
	 *
	 * if (recipe != null) { float manaCost = recipe.getVibeUsage() / this.level; if
	 * (vibes.getVibes() >= manaCost) { ItemStack output =
	 * recipe.getOutput().copy(); ItemEntity outputItem = new ItemEntity(world,
	 * pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, output); if
	 * (world.isRemote) { world.addParticle(ParticleTypes.PORTAL, pos.getX(),
	 * pos.getY(), pos.getZ(), 0.0D, 0.0D, 0.0D); } world.addEntity(outputItem);
	 * vibes.setVibes(vibes.getVibes() - manaCost); currentRecipe = null;
	 * world.addBlockEvent(getPos(), BlockInit.wand_maker.get(), SET_COOLDOWN_EVENT,
	 * 60); world.addBlockEvent(getPos(), BlockInit.wand_maker.get(),
	 * CRAFT_EFFECT_EVENT, 0);
	 *
	 * for (int i = 0; i < getSizeInventory(); i++) { ItemStack stack =
	 * itemHandler.getStackInSlot(i); if (!stack.isEmpty()) { } this.sendUpdates();
	 * itemHandler.setStackInSlot(i, ItemStack.EMPTY); } } } }
	 */

}
