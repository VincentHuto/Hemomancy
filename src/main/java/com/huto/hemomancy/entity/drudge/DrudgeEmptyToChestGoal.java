package com.huto.hemomancy.entity.drudge;

import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.item.Items;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DrudgeEmptyToChestGoal extends MoveToBlockGoal {

	EntityDrudge creature;

	public DrudgeEmptyToChestGoal(EntityDrudge creature, double speedIn, int length) {
		super(creature, speedIn, length);
		this.creature = creature;
	}

	@Override
	protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
		if (!creature.getDrudgeInventory().isEmpty()) {
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
					if (!creature.getDrudgeInventory().isEmpty()) {
						ChestTileEntity chest = (ChestTileEntity) creature.world.getTileEntity(destinationBlock);
						IItemHandler handler = chest.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
								.orElseThrow(NullPointerException::new);
						for (int i = 0; i < handler.getSlots(); i++) {
							if (handler.getStackInSlot(i).getItem() == Items.AIR) {
								for (int j = 0; j < creature.getDrudgeInventory().getSizeInventory(); j++) {
									if (creature.getDrudgeInventory().getStackInSlot(j).getItem() != Items.AIR) {
										if (handler.isItemValid(i, creature.getDrudgeInventory().getStackInSlot(j))) {
											handler.insertItem(i, creature.getDrudgeInventory().getStackInSlot(j),
													false);
										}
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
			if (drudge.getRoleTitle() == EnumDrudgeRoles.COLLECTOR) {
				if (this.runDelay > 0) {
					--this.runDelay;
					return false;
				} else if (this.func_220729_m()) {
					this.runDelay = 5;
					return true;
				} else {
					this.runDelay = this.getRunDelay(this.creature);
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