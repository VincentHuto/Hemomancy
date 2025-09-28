package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.block.vein.EarthenVeinLocProvider;
import com.vincenthuto.hemomancy.common.capability.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedVivianiteBlockEntity extends BlockEntity {

	public int time;

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
	}

	public SuspendedVivianiteBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_vivianite.get(), pos, state);
	}
}
