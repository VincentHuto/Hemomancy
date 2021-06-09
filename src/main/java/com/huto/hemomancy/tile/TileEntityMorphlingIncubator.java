package com.huto.hemomancy.tile;

import java.util.List;

import javax.annotation.Nullable;

import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.init.TileEntityInit;
import com.huto.hemomancy.item.ItemTome;
import com.huto.hemomancy.item.tool.living.ItemLivingStaff;
import com.hutoslib.common.VanillaPacketDispatcher;
import com.hutoslib.common.tile.TileSimpleInventory;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

public class TileEntityMorphlingIncubator extends TileSimpleInventory implements ITickableTileEntity {

	private int cooldown = 0;
	private static final int SET_COOLDOWN_EVENT = 1;
	private static final int CRAFT_EFFECT_EVENT = 2;

	public TileEntityMorphlingIncubator() {
		super(TileEntityInit.morphling_incubator.get());
	}


	@Override
	public void tick() {
		if (world.isRemote) {

			if (!world.isRemote) {
				List<ItemEntity> items = world.getEntitiesWithinAABB(ItemEntity.class,
						new AxisAlignedBB(pos, pos.add(1, 1, 1)));
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
	public boolean receiveClientEvent(int id, int param) {
		switch (id) {
		case SET_COOLDOWN_EVENT:
			cooldown = param;
			return true;
		case CRAFT_EFFECT_EVENT: {
			if (world.isRemote) {
			}
			return true;
		}
		default:
			return super.receiveClientEvent(id, param);
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
	 * public void onActivated(PlayerEntity player, ItemStack wand) {
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
