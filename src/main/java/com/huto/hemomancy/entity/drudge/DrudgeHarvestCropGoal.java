package com.huto.hemomancy.entity.drudge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.hutoslib.common.block.BlockUtils;

import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DrudgeHarvestCropGoal extends RemoveBlockGoal {
	public DrudgeHarvestCropGoal(PathfinderMob creatureIn, double speed, int yMax) {
		super(Blocks.AIR, creatureIn, speed, yMax);
		this.entity = creature;
	}

	private final MobEntity entity;
	private int breakingTime;
	@SuppressWarnings("serial")
	List<Block> harvestableBlocks = new ArrayList<Block>() {
		{
			add(Blocks.BEETROOTS);
			add(Blocks.POTATOES);
			add(Blocks.WHEAT);
			add(Blocks.CARROTS);
		}
	};

	@Nullable
	private BlockPos findTarget(BlockPos pos, IBlockReader worldIn) {
		BlockPos[] ablockpos = new BlockPos[] { pos, pos.up(), pos.up().up(), pos.south(), pos.south().south(),
				pos.south().up(), pos.south().south().up(), pos.south().down(), pos.south().south().down(), pos.east(),
				pos.east().east(), pos.east().up(), pos.east().east().up(), pos.east().down(), pos.east().east().down(),
				pos.west(), pos.west().west(), pos.west().up(), pos.west().west().up(), pos.west().down(),
				pos.west().west().down(), pos.north(), pos.north().north(), pos.north().up(), pos.north().north().up(),
				pos.north().down(), pos.north().north().down() };

		for (BlockPos blockpos : ablockpos) {
			for (Block harv : harvestableBlocks) {
				if (worldIn.getBlockState(blockpos).matchesBlock(harv)) {
					return blockpos;
				}
			}
		}

		return null;
	}

	@Override
	public void tick() {
		super.tick();
		World world = entity.world;
		BlockPos blockpos = this.entity.getPosition();
		BlockPos blockpos1 = this.findTarget(blockpos, world);
		Random random = this.entity.getRNG();
		if ((blockpos1 != null)) {
			if (BlockUtils.isCrop(world, blockpos1)) {
				CropsBlock crop = (CropsBlock) world.getBlockState(blockpos1).getBlock();
				if (!(crop instanceof BeetrootBlock)) {
					if (BlockUtils.isCropFullyGrown(world, blockpos1)) {
						if (this.breakingTime > 0) {
							Vector3d vector3d = this.entity.getMotion();
							this.entity.setMotion(vector3d.x, 0.3D, vector3d.z);
							if (!world.isRemote) {
								((ServerWorld) world)
										.spawnParticle(
												new ItemParticleData(ParticleTypes.ITEM,
														new ItemStack(
																world.getBlockState(blockpos1).getBlock().asItem())),
												(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.7D,
												(double) blockpos1.getZ() + 0.5D, 3,
												((double) random.nextFloat() - 0.5D) * 0.08D,
												((double) random.nextFloat() - 0.5D) * 0.08D,
												((double) random.nextFloat() - 0.5D) * 0.08D, (double) 0.15F);
							}
						}

						if (this.breakingTime % 2 == 0) {
							Vector3d vector3d1 = this.entity.getMotion();
							this.entity.setMotion(vector3d1.x, -0.3D, vector3d1.z);
							if (this.breakingTime % 6 == 0) {
								this.playBreakingSound(world, this.destinationBlock);
							}
						}
						if (this.breakingTime > 60) {
							world.destroyBlock(blockpos1, true);
							if (!world.isRemote) {
								for (int i = 0; i < 20; ++i) {
									double d3 = random.nextGaussian() * 0.02D;
									double d1 = random.nextGaussian() * 0.02D;
									double d2 = random.nextGaussian() * 0.02D;
									((ServerWorld) world).spawnParticle(ParticleTypes.POOF,
											(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY(),
											(double) blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double) 0.15F);
								}

								this.playBrokenSound(world, blockpos1);
							}
						}

						++this.breakingTime;
					}
				} else {
					if (BlockUtils.isBeetFullyGrown(world, blockpos1)) {
						if (this.breakingTime > 0) {
							Vector3d vector3d = this.entity.getMotion();
							this.entity.setMotion(vector3d.x, 0.3D, vector3d.z);
							if (!world.isRemote) {
								((ServerWorld) world)
										.spawnParticle(
												new ItemParticleData(ParticleTypes.ITEM,
														new ItemStack(
																world.getBlockState(blockpos1).getBlock().asItem())),
												(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.7D,
												(double) blockpos1.getZ() + 0.5D, 3,
												((double) random.nextFloat() - 0.5D) * 0.08D,
												((double) random.nextFloat() - 0.5D) * 0.08D,
												((double) random.nextFloat() - 0.5D) * 0.08D, (double) 0.15F);
							}
						}
						if (this.breakingTime % 2 == 0) {
							Vector3d vector3d1 = this.entity.getMotion();
							this.entity.setMotion(vector3d1.x, -0.3D, vector3d1.z);
							if (this.breakingTime % 6 == 0) {
								this.playBreakingSound(world, this.destinationBlock);
							}
						}
						if (this.breakingTime > 60) {
							world.destroyBlock(blockpos1, true);
							if (!world.isRemote) {
								for (int i = 0; i < 20; ++i) {
									double d3 = random.nextGaussian() * 0.02D;
									double d1 = random.nextGaussian() * 0.02D;
									double d2 = random.nextGaussian() * 0.02D;
									((ServerWorld) world).spawnParticle(ParticleTypes.POOF,
											(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY(),
											(double) blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double) 0.15F);
								}
								this.playBrokenSound(world, blockpos1);
							}
						}
						++this.breakingTime;
					}
				}
			}
		}

	}

	@Override
	protected boolean shouldMoveTo(IWorldReader worldIn, BlockPos pos) {
		pos = pos.add(0.5, 0, 0.5);
		if (this.creature instanceof EntityDrudge) {
			if (BlockUtils.isCrop(worldIn, pos)) {
				CropsBlock crop = (CropsBlock) worldIn.getBlockState(pos).getBlock();
				// This is because APPERENTLY beets have their own growth property
				if (!(crop instanceof BeetrootBlock)) {
					return BlockUtils.isCropFullyGrown(worldIn, pos);
				} else {
					return BlockUtils.isBeetFullyGrown(worldIn, pos);
				}

			}
		}
		return false;

	}

	@Override
	public boolean shouldExecute() {
		if (creature instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) creature;
			if (drudge.getDrudgeRole() == 3) {
				if (this.runDelay > 0) {
					--this.runDelay;
					return false;
				} else if (this.func_220729_m()) {
					this.runDelay = 20;
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

	public void playBreakingSound(IWorld worldIn, BlockPos pos) {
		worldIn.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5F,
				0.9F + worldIn.getRandom().nextFloat() * 0.2F);
	}

	public void playBrokenSound(World worldIn, BlockPos pos) {
		worldIn.playSound((PlayerEntity) null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F,
				0.9F + worldIn.rand.nextFloat() * 0.2F);
	}

	public double getTargetDistanceSq() {
		return 1.14D;
	}
}
