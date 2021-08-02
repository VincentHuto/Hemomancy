package com.huto.hemomancy.tile;

import java.util.List;

import com.huto.hemomancy.init.BlockEntityInit;
import com.huto.hemomancy.init.ItemInit;
import com.hutoslib.common.block.entity.TileSimpleInventory;

import net.minecraft.tileentity.ITickableBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class BlockEntityMorphlingIncubator extends TileSimpleInventory implements ITickableBlockEntity {

	private int cooldown = 0;
	private static final int SET_COOLDOWN_EVENT = 1;
	private static final int CRAFT_EFFECT_EVENT = 2;

	public BlockEntityMorphlingIncubator() {
		super(BlockEntityInit.morphling_incubator.get());
	}

	@Override
	public void tick() {
		if (level.isClientSide) {

			if (!level.isClientSide) {
				List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class,
						new AABB(worldPosition, worldPosition.offset(1, 1, 1)));
				for (ItemEntity item : items) {
					if (item.isAlive() && !item.getItem().isEmpty()
							&& item.getItem().getItem() != ItemInit.morphling_polyp.get()) {
						ItemStack stack = item.getItem();
						addItem(null, stack, null);
					}
				}

			} else {

				if (cooldown > 0) {
				}
			}

			if (cooldown > 0) {
				cooldown--;
			}

		}

	}

	@Override
	public boolean triggerEvent(int id, int param) {
		switch (id) {
		case SET_COOLDOWN_EVENT:
			cooldown = param;
			return true;
		case CRAFT_EFFECT_EVENT: {
			if (level.isClientSide) {
			}
			return true;
		}
		default:
			return super.triggerEvent(id, param);
		}
	}

	public boolean isEmpty() {
		for (int i = 0; i < getSizeInventory(); i++) {
			if (!getItemHandler().getStackInSlot(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int getSizeInventory() {
		return 3;
	}

	/*
	 * public void onActivated(Player player, ItemStack wand) {
	 * RecipeWandMaker recipe = null; if (currentRecipe != null) recipe =
	 * currentRecipe; else for (RecipeWandMaker recipe_ :
	 * ModWandRecipies.wandMakerRecipies) {
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
