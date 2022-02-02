package com.vincenthuto.hemomancy.entity.drudge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DrudgeExtractFromChestGoal extends MoveToBlockGoal {

	EntityDrudge creature;

	public DrudgeExtractFromChestGoal(EntityDrudge creature, double speedIn, int length) {
		super(creature, speedIn, length);
		this.creature = creature;
	}

	@Override
	protected boolean isValidTarget(LevelReader worldIn, BlockPos pos) {
		if (creature.getDrudgeInventory().isEmpty()) {
			if (worldIn.getBlockEntity(pos) != null) {
				if (worldIn.getBlockEntity(pos) instanceof ChestBlockEntity) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean canContinueToUse() {
		if (creature.getDrudgeInventory().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void tick() {
		super.tick();
		System.out.println("fe");
		if (isReachedTarget()) {
			if (creature.level.getBlockEntity(blockPos) != null) {
				if (creature.level.getBlockEntity(blockPos) instanceof ChestBlockEntity) {
					if (creature.getDrudgeInventory().isEmpty()) {
						ChestBlockEntity chest = (ChestBlockEntity) creature.level.getBlockEntity(blockPos);
						IItemHandler handler = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						for (int j = 0; j < creature.getDrudgeInventory().getContainerSize(); j++) {
							for (int i = 0; i < handler.getSlots(); i++) {
								if (creature.getDrudgeInventory().getItem(j).getItem() == Items.AIR) {
									ItemStack extractStack = handler.getStackInSlot(i).copy();
									if (handler.getStackInSlot(i).getItem() != Items.AIR) {
										creature.getDrudgeInventory().addItem(extractStack);
										handler.extractItem(i, extractStack.getCount(), false);
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
			if (drudge.getRoleTitle() == EnumDrudgeRoles.PLACER && drudge.getDrudgeInventory().isEmpty()) {
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