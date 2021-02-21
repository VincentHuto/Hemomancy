package com.huto.hemomancy.entity.drudge;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

public class DrudgeReplantCropGoal extends MoveToBlockGoal {

	EntityDrudge creature;
	@Nullable
	private final List<BlockPos> farmableBlocks = Lists.newArrayList();

	public DrudgeReplantCropGoal(EntityDrudge creature, double speedIn, int length) {
		super(creature, speedIn, length);
		this.creature = creature;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick() {
		super.tick();
		if (this.destinationBlock == null || this.destinationBlock.withinDistance(creature.getPositionVec(), 1.0D)) {
			if (creature.world instanceof ServerWorld) {
				ServerWorld worldIn = (ServerWorld) creature.world;
				if (this.destinationBlock != null) {
					BlockState blockstate = worldIn.getBlockState(this.destinationBlock);
					Block block = blockstate.getBlock();
					Block block1 = worldIn.getBlockState(this.destinationBlock.down()).getBlock();
					if (block instanceof CropsBlock && ((CropsBlock) block).isMaxAge(blockstate)) {
						worldIn.destroyBlock(this.destinationBlock, true, creature);
					}

					if (blockstate.isAir() && block1 instanceof FarmlandBlock && creature.isFarmItemInInventory()) {
						Inventory inventory = creature.getDrudgeInventory();

						for (int i = 0; i < inventory.getSizeInventory(); ++i) {
							ItemStack itemstack = inventory.getStackInSlot(i);
							boolean flag = false;
							if (!itemstack.isEmpty()) {
								if (itemstack.getItem() == Items.WHEAT_SEEDS) {
									worldIn.setBlockState(this.destinationBlock, Blocks.WHEAT.getDefaultState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.POTATO) {
									worldIn.setBlockState(this.destinationBlock, Blocks.POTATOES.getDefaultState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.CARROT) {
									worldIn.setBlockState(this.destinationBlock, Blocks.CARROTS.getDefaultState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.BEETROOT_SEEDS) {
									worldIn.setBlockState(this.destinationBlock, Blocks.BEETROOTS.getDefaultState(), 3);
									flag = true;
								} else if (itemstack.getItem() instanceof net.minecraftforge.common.IPlantable) {
									if (((net.minecraftforge.common.IPlantable) itemstack.getItem()).getPlantType(
											worldIn, destinationBlock) == net.minecraftforge.common.PlantType.CROP) {
										worldIn.setBlockState(destinationBlock,
												((net.minecraftforge.common.IPlantable) itemstack.getItem())
														.getPlant(worldIn, destinationBlock),
												3);
										flag = true;
									}
								}
							}

							if (flag) {
								worldIn.playSound((PlayerEntity) null, (double) this.destinationBlock.getX(),
										(double) this.destinationBlock.getY(), (double) this.destinationBlock.getZ(),
										SoundEvents.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 1.0F, 1.0F);
								itemstack.shrink(1);
								if (itemstack.isEmpty()) {
									inventory.setInventorySlotContents(i, ItemStack.EMPTY);
								}
								break;
							}
						}
					}

					if (block instanceof CropsBlock && !((CropsBlock) block).isMaxAge(blockstate)) {
						this.farmableBlocks.remove(this.destinationBlock);
						this.destinationBlock = this.getNextPosForFarming(worldIn);
						if (this.destinationBlock != null) {
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isValidPosForFarming(BlockPos pos, ServerWorld serverWorldIn) {
		BlockState blockstate = serverWorldIn.getBlockState(pos);
		Block block = blockstate.getBlock();
		Block block1 = serverWorldIn.getBlockState(pos.down()).getBlock();
		return block instanceof CropsBlock && ((CropsBlock) block).isMaxAge(blockstate)
				|| blockstate.isAir() && block1 instanceof FarmlandBlock;
	}

	@Override
	public boolean shouldExecute() {
		if (creature instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) creature;
			if (creature.getRoleTitle() != EnumDrudgeRoles.PLACER) {
				return false;
			} else {
				BlockPos.Mutable blockpos$mutable = drudge.getPosition().toMutable();
				this.farmableBlocks.clear();

				for (int i = -1; i <= 1; ++i) {
					for (int j = -1; j <= 1; ++j) {
						for (int k = -1; k <= 1; ++k) {
							blockpos$mutable.setPos(drudge.getPosX() + (double) i, drudge.getPosY() + (double) j,
									drudge.getPosZ() + (double) k);
							if (drudge.getEntityWorld() instanceof ServerWorld) {
								if (this.isValidPosForFarming(blockpos$mutable,
										(ServerWorld) drudge.getEntityWorld())) {
									this.farmableBlocks.add(new BlockPos(blockpos$mutable));
								}
							}
						}
					}
				}
				if (drudge.getEntityWorld() instanceof ServerWorld) {
					this.destinationBlock = this.getNextPosForFarming((ServerWorld) drudge.world);
					return this.destinationBlock != null;
				}
			}
		}
		return false;

	}

	@Nullable
	private BlockPos getNextPosForFarming(ServerWorld serverWorldIn) {
		return this.farmableBlocks.isEmpty() ? null
				: this.farmableBlocks.get(serverWorldIn.getRandom().nextInt(this.farmableBlocks.size()));
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
		if (worldIn instanceof ServerWorld) {
			if (this.creature instanceof EntityDrudge) {
				BlockState blockstate = worldIn.getBlockState(this.destinationBlock);
				Block block1 = worldIn.getBlockState(this.destinationBlock.down()).getBlock();
				if (blockstate.isAir() && block1 instanceof FarmlandBlock && creature.isFarmItemInInventory()) {
					return true;
				}
			}
		}
		return false;
	}

}