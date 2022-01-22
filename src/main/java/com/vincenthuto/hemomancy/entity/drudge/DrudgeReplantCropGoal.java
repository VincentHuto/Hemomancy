package com.vincenthuto.hemomancy.entity.drudge;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

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
		if (this.blockPos == null || this.blockPos.closerThan(creature.position(), 1.0D)) {
			if (creature.level instanceof ServerLevel) {
				ServerLevel worldIn = (ServerLevel) creature.level;
				if (this.blockPos != null) {
					BlockState blockstate = worldIn.getBlockState(this.blockPos);
					Block block = blockstate.getBlock();
					Block block1 = worldIn.getBlockState(this.blockPos.below()).getBlock();
					if (block instanceof CropBlock && ((CropBlock) block).isMaxAge(blockstate)) {
						worldIn.destroyBlock(this.blockPos, true, creature);
					}

					if (blockstate.isAir() && block1 instanceof FarmBlock && creature.isFarmItemInInventory()) {
						SimpleContainer inventory = creature.getDrudgeInventory();

						for (int i = 0; i < inventory.getContainerSize(); ++i) {
							ItemStack itemstack = inventory.getItem(i);
							boolean flag = false;
							if (!itemstack.isEmpty()) {
								if (itemstack.getItem() == Items.WHEAT_SEEDS) {
									worldIn.setBlock(this.blockPos, Blocks.WHEAT.defaultBlockState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.POTATO) {
									worldIn.setBlock(this.blockPos, Blocks.POTATOES.defaultBlockState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.CARROT) {
									worldIn.setBlock(this.blockPos, Blocks.CARROTS.defaultBlockState(), 3);
									flag = true;
								} else if (itemstack.getItem() == Items.BEETROOT_SEEDS) {
									worldIn.setBlock(this.blockPos, Blocks.BEETROOTS.defaultBlockState(), 3);
									flag = true;
								} else if (itemstack.getItem() instanceof net.minecraftforge.common.IPlantable) {
									if (((net.minecraftforge.common.IPlantable) itemstack.getItem()).getPlantType(
											worldIn, blockPos) == net.minecraftforge.common.PlantType.CROP) {
										worldIn.setBlock(blockPos,
												((net.minecraftforge.common.IPlantable) itemstack.getItem())
														.getPlant(worldIn, blockPos),
												3);
										flag = true;
									}
								}
							}

							if (flag) {
								worldIn.playSound((Player) null, (double) this.blockPos.getX(),
										(double) this.blockPos.getY(), (double) this.blockPos.getZ(),
										SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
								itemstack.shrink(1);
								if (itemstack.isEmpty()) {
									inventory.setItem(i, ItemStack.EMPTY);
								}
								break;
							}
						}
					}

					if (block instanceof CropBlock && !((CropBlock) block).isMaxAge(blockstate)) {
						this.farmableBlocks.remove(this.blockPos);
						this.blockPos = this.getNextPosForFarming(worldIn);
						if (this.blockPos != null) {
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isValidPosForFarming(BlockPos pos, ServerLevel serverLevelIn) {
		BlockState blockstate = serverLevelIn.getBlockState(pos);
		Block block = blockstate.getBlock();
		Block block1 = serverLevelIn.getBlockState(pos.below()).getBlock();
		return block instanceof CropBlock && ((CropBlock) block).isMaxAge(blockstate)
				|| blockstate.isAir() && block1 instanceof FarmBlock;
	}

	@Override
	public boolean canUse() {
		if (creature instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) creature;
			if (creature.getRoleTitle() != EnumDrudgeRoles.PLACER) {
				return false;
			} else {
				BlockPos.MutableBlockPos blockpos$mutable = drudge.blockPosition().mutable();
				this.farmableBlocks.clear();

				for (int i = -1; i <= 1; ++i) {
					for (int j = -1; j <= 1; ++j) {
						for (int k = -1; k <= 1; ++k) {
							blockpos$mutable.set(drudge.getX() + (double) i, drudge.getY() + (double) j,
									drudge.getZ() + (double) k);
							if (drudge.getCommandSenderWorld() instanceof ServerLevel) {
								if (this.isValidPosForFarming(blockpos$mutable,
										(ServerLevel) drudge.getCommandSenderWorld())) {
									this.farmableBlocks.add(new BlockPos(blockpos$mutable));
								}
							}
						}
					}
				}
				if (drudge.getCommandSenderWorld() instanceof ServerLevel) {
					this.blockPos = this.getNextPosForFarming((ServerLevel) drudge.level);
					return this.blockPos != null;
				}
			}
		}
		return false;

	}

	@Nullable
	private BlockPos getNextPosForFarming(ServerLevel serverLevelIn) {
		return this.farmableBlocks.isEmpty() ? null
				: this.farmableBlocks.get(serverLevelIn.getRandom().nextInt(this.farmableBlocks.size()));
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean isValidTarget(LevelReader worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel) {
			if (this.creature instanceof EntityDrudge) {
				BlockState blockstate = worldIn.getBlockState(this.blockPos);
				Block block1 = worldIn.getBlockState(this.blockPos.below()).getBlock();
				if (blockstate.isAir() && block1 instanceof FarmBlock && creature.isFarmItemInInventory()) {
					return true;
				}
			}
		}
		return false;
	}

}