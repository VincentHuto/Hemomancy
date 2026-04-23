package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MnemonicReliquaryBlockEntity extends BlockEntity {

	private int openCount;
	private float lidAngle;
	private float prevLidAngle;

	public MnemonicReliquaryBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mnemonic_reliquary.get(), pos, state);
	}

	public void startOpen() {
		openCount++;
		if (level != null && !level.isClientSide) {
			level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
		}
	}

	public void stopOpen() {
		openCount = Math.max(0, openCount - 1);
		if (level != null && !level.isClientSide) {
			level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
		}
	}

	@Override
	public boolean triggerEvent(int id, int param) {
		if (id == 1) {
			this.openCount = param;
			return true;
		}
		return super.triggerEvent(id, param);
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, MnemonicReliquaryBlockEntity be) {
		be.prevLidAngle = be.lidAngle;
		if (be.openCount > 0 && be.lidAngle < 1.0F) {
			be.lidAngle = Math.min(1.0F, be.lidAngle + 0.1F);
		} else if (be.openCount == 0 && be.lidAngle > 0.0F) {
			be.lidAngle = Math.max(0.0F, be.lidAngle - 0.1F);
		}
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MnemonicReliquaryBlockEntity be) {
		// Server doesn't need to animate, but we keep openCount in sync
	}

	/**
	 * Returns the interpolated lid angle (0 = closed, 1 = fully open).
	 */
	public float getLidAngle(float partialTick) {
		return Mth.lerp(partialTick, prevLidAngle, lidAngle);
	}

	public int getOpenCount() {
		return openCount;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);
		tag.putInt("OpenCount", openCount);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);
		openCount = tag.getInt("OpenCount");
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag tag = super.getUpdateTag(provider);
		tag.putInt("OpenCount", openCount);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
