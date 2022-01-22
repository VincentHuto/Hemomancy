package com.vincenthuto.hemomancy.entity.drudge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DrudgeEmptyToChestGoal extends MoveToBlockGoal {

	EntityDrudge creature;

	public DrudgeEmptyToChestGoal(EntityDrudge creature, double speedIn, int length) {
		super(creature, speedIn, length);
		this.creature = creature;
	}

	@Override
	protected boolean isValidTarget(LevelReader worldIn, BlockPos pos) {
		if (!creature.getDrudgeInventory().isEmpty()) {
			if (worldIn.getBlockEntity(pos) != null) {
				if (worldIn.getBlockEntity(pos) instanceof ChestBlockEntity) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (isReachedTarget()) {
			if (creature.level.getBlockEntity(blockPos) != null) {
				if (creature.level.getBlockEntity(blockPos) instanceof ChestBlockEntity) {
					if (!creature.getDrudgeInventory().isEmpty()) {
						ChestBlockEntity chest = (ChestBlockEntity) creature.level.getBlockEntity(blockPos);
						IItemHandler handler = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						for (int j = 0; j < creature.getDrudgeInventory().getContainerSize(); j++) {
							for (int i = 0; i < handler.getSlots(); i++) {
								if (creature.getDrudgeInventory().getItem(j).getItem() != Items.AIR) {
									ItemStack insertStack = creature.getDrudgeInventory().getItem(j).copy();
									if (handler.getStackInSlot(i).getItem() == insertStack.getItem()) {
										handler.getStackInSlot(i).grow(insertStack.getCount());
										creature.getDrudgeInventory().setItem(j, ItemStack.EMPTY);

									} else if (handler.getStackInSlot(i).getItem() == Items.AIR) {
										handler.insertItem(i, insertStack, false);
										creature.getDrudgeInventory().setItem(j, ItemStack.EMPTY);

									}

								}

							}

						}
					}

				}
			}
		}

	}

	@Override
	public boolean canUse() {
		if (creature instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) creature;
			if (drudge.getRoleTitle() == EnumDrudgeRoles.COLLECTOR) {
				if (this.tryFindBlock()) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;

	}

	private boolean tryFindBlock() {
		return this.blockPos != null && this.isValidTarget(this.creature.level, this.blockPos) ? true
				: this.findNearestBlock();
	}
}