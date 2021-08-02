package com.huto.hemomancy.entity.drudge;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.hutoslib.common.block.BlockUtils;
import com.mojang.math.Vector3d;

import net.minecraft.block.CropsBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ILevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class DrudgeHarvestCropGoal extends RemoveBlockGoal {
	public DrudgeHarvestCropGoal(PathfinderMob creatureIn, double speed, int yMax) {
		super(Blocks.AIR, creatureIn, speed, yMax);
		this.entity = mob;
	}

	private final Mob entity;
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
	private BlockPos findTarget(BlockPos pos, BlockGetter worldIn) {
		BlockPos[] ablockpos = new BlockPos[] { pos, pos.above(), pos.above().above(), pos.south(), pos.south().south(),
				pos.south().above(), pos.south().south().above(), pos.south().below(), pos.south().south().below(),
				pos.east(), pos.east().east(), pos.east().above(), pos.east().east().above(), pos.east().below(),
				pos.east().east().below(), pos.west(), pos.west().west(), pos.west().above(), pos.west().west().above(),
				pos.west().below(), pos.west().west().below(), pos.north(), pos.north().north(), pos.north().above(),
				pos.north().north().above(), pos.north().below(), pos.north().north().below() };

		for (BlockPos blockpos : ablockpos) {
			for (Block harv : harvestableBlocks) {
				if (worldIn.getBlockState(blockpos).is(harv)) {
					return blockpos;
				}
			}
		}

		return null;
	}

	@Override
	public void tick() {
		super.tick();
		Level world = entity.level;
		BlockPos blockpos = this.entity.blockPosition();
		BlockPos blockpos1 = this.findTarget(blockpos, world);
		Random random = this.entity.getRandom();
		if ((blockpos1 != null)) {
			if (BlockUtils.isCrop(world, blockpos1)) {
				CropsBlock crop = (CropsBlock) world.getBlockState(blockpos1).getBlock();
				if (!(crop instanceof BeetrootBlock)) {
					if (BlockUtils.isCropFullyGrown(world, blockpos1)) {
						if (this.breakingTime > 0) {
							Vector3d vector3d = this.entity.getDeltaMovement();
							this.entity.setDeltaMovement(vector3d.x, 0.3D, vector3d.z);
							if (!world.isClientSide) {
								((ServerLevel) world)
										.sendParticles(
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
							Vector3d vector3d1 = this.entity.getDeltaMovement();
							this.entity.setDeltaMovement(vector3d1.x, -0.3D, vector3d1.z);
							if (this.breakingTime % 6 == 0) {
								this.playDestroyProgressSound(world, this.blockPos);
							}
						}
						if (this.breakingTime > 60) {
							world.destroyBlock(blockpos1, true);
							if (!world.isClientSide) {
								for (int i = 0; i < 20; ++i) {
									double d3 = random.nextGaussian() * 0.02D;
									double d1 = random.nextGaussian() * 0.02D;
									double d2 = random.nextGaussian() * 0.02D;
									((ServerLevel) world).sendParticles(ParticleTypes.POOF,
											(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY(),
											(double) blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double) 0.15F);
								}

								this.playBreakSound(world, blockpos1);
							}
						}

						++this.breakingTime;
					}
				} else {
					if (BlockUtils.isBeetFullyGrown(world, blockpos1)) {
						if (this.breakingTime > 0) {
							Vector3d vector3d = this.entity.getDeltaMovement();
							this.entity.setDeltaMovement(vector3d.x, 0.3D, vector3d.z);
							if (!world.isClientSide) {
								((ServerLevel) world)
										.sendParticles(
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
							Vector3d vector3d1 = this.entity.getDeltaMovement();
							this.entity.setDeltaMovement(vector3d1.x, -0.3D, vector3d1.z);
							if (this.breakingTime % 6 == 0) {
								this.playDestroyProgressSound(world, this.blockPos);
							}
						}
						if (this.breakingTime > 60) {
							world.destroyBlock(blockpos1, true);
							if (!world.isClientSide) {
								for (int i = 0; i < 20; ++i) {
									double d3 = random.nextGaussian() * 0.02D;
									double d1 = random.nextGaussian() * 0.02D;
									double d2 = random.nextGaussian() * 0.02D;
									((ServerLevel) world).sendParticles(ParticleTypes.POOF,
											(double) blockpos1.getX() + 0.5D, (double) blockpos1.getY(),
											(double) blockpos1.getZ() + 0.5D, 1, d3, d1, d2, (double) 0.15F);
								}
								this.playBreakSound(world, blockpos1);
							}
						}
						++this.breakingTime;
					}
				}
			}
		}

	}

	@Override
	protected boolean isValidTarget(LevelReader worldIn, BlockPos pos) {
		pos = pos.offset(0.5, 0, 0.5);
		if (this.mob instanceof EntityDrudge) {
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
	public boolean canUse() {
		if (mob instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) mob;
			if (drudge.getDrudgeRole() == 3) {
				if (this.nextStartTick > 0) {
					--this.nextStartTick;
					return false;
				} else if (this.tryFindBlock()) {
					this.nextStartTick = 20;
					return true;
				} else {
					this.nextStartTick = this.nextStartTick(this.mob);
					return false;
				}
			}
		}
		return false;

	}

	private boolean tryFindBlock() {
		return this.blockPos != null && this.isValidTarget(this.mob.level, this.blockPos) ? true
				: this.findNearestBlock();
	}

	public void playDestroyProgressSound(ILevel worldIn, BlockPos pos) {
		worldIn.playSound((Player) null, pos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F,
				0.9F + worldIn.getRandom().nextFloat() * 0.2F);
	}

	public void playBreakSound(Level worldIn, BlockPos pos) {
		worldIn.playSound((Player) null, pos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F,
				0.9F + worldIn.random.nextFloat() * 0.2F);
	}

	public double acceptedDistance() {
		return 1.14D;
	}
}
