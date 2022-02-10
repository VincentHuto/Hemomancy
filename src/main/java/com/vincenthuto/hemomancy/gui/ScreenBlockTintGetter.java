package com.vincenthuto.hemomancy.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

public class ScreenBlockTintGetter implements BlockAndTintGetter {

	@Override
	public float getShade(Direction p_45522_, boolean p_45523_) {
		return 50f;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return null;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver resolver) {
		return resolver.getColor(Minecraft.getInstance().level.getBiome(pos), pos.getX(), pos.getZ());
	}

	@Nullable
	@Override
	public BlockEntity getBlockEntity(BlockPos pos) {
		return Minecraft.getInstance().level.getBlockEntity(pos);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return Minecraft.getInstance().level.getBlockState(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos p_45569_) {
		return null;
	}

	@Override
	public int getHeight() {
		return 256;
	}

	@Override
	public int getMinBuildHeight() {
		return 0;
	}

	@Override
	public int getRawBrightness(BlockPos p_45525_, int p_45526_) {
		return 15 - p_45526_;
	}

	@Override
	public int getBrightness(LightLayer p_45518_, BlockPos p_45519_) {
		return 15;
	}
}