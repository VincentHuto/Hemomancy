package com.huto.hemomancy.entity.drudge;

import net.minecraft.core.BlockPos;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DrudgeExtractFromChestGoal extends MoveToBlockGoal {

	EntityDrudge creature;

	public DrudgeExtractFromChestGoal(EntityDrudge creature, double speedIn, int length) {
		super(creature, speedIn, length);
		this.creature = creature;
	}

	@Override
	protected boolean shouldMoveTo(LevelReader worldIn, BlockPos pos) {
		if (creature.getDrudgeInventory().isEmpty()) {
			if (worldIn.getTileEntity(pos) != null) {
				if (worldIn.getTileEntity(pos) instanceof ChestTileEntity) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (getIsAboveDestination()) {
			if (creature.world.getTileEntity(destinationBlock) != null) {
				if (creature.world.getTileEntity(destinationBlock) instanceof ChestTileEntity) {
					if (creature.getDrudgeInventory().isEmpty()) {
						ChestTileEntity chest = (ChestTileEntity) creature.world.getTileEntity(destinationBlock);
						IItemHandler handler = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						for (int j = 0; j < creature.getDrudgeInventory().getSizeInventory(); j++) {
							for (int i = 0; i < handler.getSlots(); i++) {
								if (creature.getDrudgeInventory().getStackInSlot(j).getItem() == Items.AIR) {
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
	public boolean shouldExecute() {
		if (creature instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) creature;
			if (drudge.getRoleTitle() == EnumDrudgeRoles.PLACER && drudge.getDrudgeInventory().isEmpty()) {
				if (this.func_220729_m()) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;

	}

	private boolean func_220729_m() {
		return this.destinationBlock != null && this.shouldMoveTo(this.creature.world, this.destinationBlock) ? true
				: this.searchForDestination();
	}
}