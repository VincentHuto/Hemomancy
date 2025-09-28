package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedBloodCrystalBlockEntity extends BlockEntity {

	public float timeOffset;

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
	}

	public SuspendedBloodCrystalBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_blood_crystal.get(), pos, state);
	}

	@Override
	public void onLoad() {
		if(level !=null){
			this.timeOffset = Mth.randomBetween(level.random,-3.0f,3.0f);
		}
	}
}
